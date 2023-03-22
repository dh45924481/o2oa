package com.x.query.assemble.designer.jaxrs.statement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.entity.dynamic.DynamicBaseEntity;
import com.x.base.core.entity.dynamic.DynamicEntity;
import com.x.base.core.project.bean.tuple.Pair;
import com.x.base.core.project.exception.ExceptionAccessDenied;
import com.x.base.core.project.exception.ExceptionEntityNotExist;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.base.core.project.script.AbstractResources;
import com.x.base.core.project.scripting.JsonScriptingExecutor;
import com.x.base.core.project.scripting.ScriptingFactory;
import com.x.base.core.project.tools.NumberTools;
import com.x.base.core.project.webservices.WebservicesClient;
import com.x.organization.core.express.Organization;
import com.x.query.assemble.designer.Business;
import com.x.query.assemble.designer.ThisApplication;
import com.x.query.core.entity.schema.Statement;
import com.x.query.core.entity.schema.Table;
import com.x.query.core.express.statement.Runtime;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

class ActionExecuteV2 extends BaseAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionExecuteV2.class);

    public static final Pattern QM_PARAMETER_REGEX = Pattern.compile("(\\?\\d+)");
    public static final Pattern NAMED_PARAMETER_REGEX = Pattern.compile("(:(\\w+))");

    private static final String KEY_SELECT = "SELECT";
    private static final String KEY_COUNT = "COUNT";
    private static final String KEY_LEFT_PARENTHESIS = "(";
    private static final String KEY_RIGHT_PARENTHESIS = ")";
    private static final String KEY_COUNTSQL = "COUNT(*)";
    private static final String KEY_FROM = "FROM";
    private static final String KEY_WHERE = "WHERE";
    private static final String KEY_SPACE = " ";

    ActionResult<Object> execute(EffectivePerson effectivePerson, String flag, String mode, Integer page, Integer size,
            JsonElement jsonElement) throws Exception {

        LOGGER.debug("execute:{}, flag:{}, mode:{}, page:{}, size:{}, jsonElement:{}.",
                effectivePerson::getDistinguishedName,
                () -> flag,
                () -> mode, () -> page, () -> size, () -> jsonElement);
        ClassLoader classLoader = Business.getDynamicEntityClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        Statement statement = null;
        ActionResult<Object> result = new ActionResult<>();
        Runtime runtime = null;
        ExecuteTarget executeTarget = null;
        try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
            Business business = new Business(emc);
            statement = emc.flag(flag, Statement.class);
            if (null == statement) {
                throw new ExceptionEntityNotExist(flag, Statement.class);
            }
            if (!business.executable(effectivePerson, statement)) {
                throw new ExceptionAccessDenied(effectivePerson, statement);
            }
            runtime = this.runtime(effectivePerson, jsonElement, business, page, size);
            executeTarget = concreteDataExecuteTarget(effectivePerson, business, statement, runtime);
        }
        Pair<Optional<Object>, Optional<Long>> pair = execute(effectivePerson, statement, runtime, executeTarget);
        Optional<Object> data = pair.first();
        Optional<Long> count = pair.second();
        if (data.isPresent()) {
            result.setData(data.get());
        }
        if (count.isPresent()) {
            result.setCount(count.get());
        }
        return result;
    }

    private Pair<Optional<ExecuteTarget>, Optional<ExecuteTarget>> concreteExecuteTarget(
            EffectivePerson effectivePerson,
            Business business,
            Statement statement,
            Runtime runtime) throws Exception {
        Optional<ExecuteTarget> data = Optional.empty();
        Optional<ExecuteTarget> count = Optional.empty();
        if (StringUtils.equalsAnyIgnoreCase(statement.getFormat(), Statement.FORMAT_SQL,
                Statement.FORMAT_SQLSCRIPT)) {
            String sql = "";
            if (StringUtils.equals(statement.getFormat(), Statement.FORMAT_SQL)) {
                sql = statement.getSql();
            } else {
                sql = script(effectivePerson, runtime, statement.getSqlScriptText());
            }
            data = Optional.of(new ExecuteTarget(effectivePerson, business, sql, runtime));
            if (data.get().getParsedStatement() instanceof net.sf.jsqlparser.statement.select.Select) {
                if (StringUtils.equalsIgnoreCase(statement.getCountMethod(), Statement.COUNTMETHOD_IGNORE)) {
                    count = Optional.empty();
                }
                if (StringUtils.equalsIgnoreCase(statement.getCountMethod(), Statement.COUNTMETHOD_AUTO)) {
                    count = Optional.of(concreteSqlCountAutoExecuteTarget(effectivePerson, business,
                            statement,
                            runtime, data.get()));
                } else {
                    count = Optional
                            .of(concreteSqlCountAssignExecuteTarget(effectivePerson, business, statement, runtime));
                }
            }
        } else {
            String jpql = "";
            if (StringUtils.equals(statement.getFormat(), Statement.FORMAT_JPQL)) {
                jpql = statement.getData();
            } else {
                jpql = script(effectivePerson, runtime, statement.getScriptText());
            }
            data = Optional.of(new ExecuteTarget(effectivePerson, business, jpql, runtime));
            if (data.get().getParsedStatement() instanceof net.sf.jsqlparser.statement.select.Select) {
                if (StringUtils.equalsIgnoreCase(statement.getCountMethod(), Statement.COUNTMETHOD_IGNORE)) {
                    count = Optional.empty();
                }
                if (StringUtils.equalsIgnoreCase(statement.getCountMethod(), Statement.COUNTMETHOD_AUTO)) {
                    count = Optional.of(concreteJpqlCountAutoExecuteTarget(effectivePerson, business,
                            statement,
                            runtime, data.get()));
                } else {
                    count = Optional
                            .of(concreteJpqlCountAssignExecuteTarget(effectivePerson, business, statement, runtime));
                }
            }
        }
    }

    private ExecuteTarget concreteSqlCountAssignExecuteTarget(EffectivePerson effectivePerson, Business business,
            Statement statement,
            Runtime runtime) throws Exception {
        if (StringUtils.equalsAnyIgnoreCase(statement.getFormat(), Statement.FORMAT_SQL,
                Statement.FORMAT_SQLSCRIPT)) {
            String sql = "";
            if (StringUtils.equals(statement.getFormat(), Statement.FORMAT_SQL)) {
                sql = statement.getSqlCount();
            } else {
                sql = script(effectivePerson, runtime, statement.getSqlCountScriptText());
            }
            return new ExecuteTarget(effectivePerson, business, sql, runtime);
        } else {
            String jpql = "";
            if (StringUtils.equals(statement.getFormat(), Statement.FORMAT_JPQL)) {
                jpql = statement.getCountData();
            } else {
                jpql = script(effectivePerson, runtime, statement.getCountScriptText());
            }
            return new ExecuteTarget(effectivePerson, business, jpql, runtime);
        }
    }

    private ExecuteTarget concreteSqlCountAutoExecuteTarget(EffectivePerson effectivePerson, Business business,
            Runtime runtime, ExecuteTarget dataExecuteTarget)
            throws Exception {
        Select select = (Select) dataExecuteTarget.getParsedStatement();
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        net.sf.jsqlparser.schema.Table table = (net.sf.jsqlparser.schema.Table) plainSelect.getFromItem();
        StringBuilder builder = new StringBuilder();
        builder.append(KEY_SELECT).append(KEY_SPACE).append(KEY_COUNTSQL).append(KEY_SPACE)
                .append(KEY_FROM).append(KEY_SPACE).append(table.getFullyQualifiedName());
        String whereClause = plainSelect.getWhere().toString();
        if (StringUtils.isNotBlank(whereClause)) {
            builder.append(KEY_SPACE).append(KEY_WHERE).append(KEY_SPACE).append(whereClause);
        }
        return new ExecuteTarget(effectivePerson, business, builder.toString(), runtime);
    }

    private ExecuteTarget concreteJpqlCountAssignExecuteTarget(EffectivePerson effectivePerson, Business business,
            Statement statement,
            Runtime runtime) throws Exception {
        String jpql = "";
        if (StringUtils.equals(statement.getFormat(), Statement.FORMAT_JPQL)) {
            jpql = statement.getCountData();
        } else {
            jpql = script(effectivePerson, runtime, statement.getCountScriptText());
        }
        return new ExecuteTarget(effectivePerson, business, jpql, runtime);
    }

    private ExecuteTarget concreteJpqlCountAutoExecuteTarget(EffectivePerson effectivePerson, Business business,
            Runtime runtime, ExecuteTarget dataExecuteTarget)
            throws Exception {
        Select select = (Select) dataExecuteTarget.getParsedStatement();
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        net.sf.jsqlparser.schema.Table table = (net.sf.jsqlparser.schema.Table) plainSelect.getFromItem();
        StringBuilder builder = new StringBuilder();
        builder.append(KEY_SELECT).append(KEY_SPACE).append(KEY_COUNT).append(KEY_LEFT_PARENTHESIS)
                .append(table.getAlias())
                .append(KEY_RIGHT_PARENTHESIS).append(KEY_SPACE).append(KEY_FROM).append(KEY_SPACE)
                .append(table.getFullyQualifiedName()).append(KEY_SPACE).append(table.getAlias());
        Expression exp = plainSelect.getWhere();
        if (null != exp) {
            String whereClause = exp.toString();
            if (StringUtils.isNotBlank(whereClause)) {
                builder.append(KEY_SPACE).append(KEY_WHERE).append(KEY_SPACE).append(whereClause);
            }
        }
        return new ExecuteTarget(effectivePerson, business, builder.toString(), runtime);
    }

    private Optional<Long> executeCountJpqlAuto(Statement statement,
            net.sf.jsqlparser.statement.Statement stmt, Runtime runtime)
            throws Exception {
        Select select = (Select) stmt;
        try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
            Business business = new Business(emc);
            EntityManager em;
            if (StringUtils.equalsIgnoreCase(statement.getEntityCategory(), Statement.ENTITYCATEGORY_DYNAMIC)) {
                em = business.entityManagerContainer().get(DynamicBaseEntity.class);
            } else {
                Class<? extends JpaObject> cls = this.clazz(business, statement);
                em = business.entityManagerContainer().get(cls);
            }
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            net.sf.jsqlparser.schema.Table table = (net.sf.jsqlparser.schema.Table) plainSelect.getFromItem();
            StringBuilder builder = new StringBuilder();
            builder.append(KEY_SELECT).append(KEY_SPACE).append(KEY_COUNT).append(KEY_LEFT_PARENTHESIS)
                    .append(table.getAlias())
                    .append(KEY_RIGHT_PARENTHESIS).append(KEY_SPACE).append(KEY_FROM).append(KEY_SPACE)
                    .append(table.getFullyQualifiedName()).append(KEY_SPACE).append(table.getAlias());
            Expression exp = plainSelect.getWhere();
            if (null != exp) {
                String whereClause = exp.toString();
                if (StringUtils.isNotBlank(whereClause)) {
                    builder.append(KEY_SPACE).append(KEY_WHERE).append(KEY_SPACE).append(whereClause);
                }
            }
            LOGGER.debug("executeCountJpqlAuto：{}.", builder::toString);
            Query query = em.createQuery(builder.toString());
            for (Parameter<?> p : query.getParameters()) {
                if (runtime.hasParameter(p.getName())) {
                    query.setParameter(p.getName(), runtime.getParameter(p.getName()));
                } else {
                    throw new ExceptionRequiredParameterNotPassed(p.getName());
                }
            }
            return Optional.of((Long) query.getSingleResult());
        }
    }

    private Pair<Optional<Object>, Optional<Long>> execute(EffectivePerson effectivePerson, Statement statement,
            Runtime runtime,
            ExecuteTarget executeTarget) throws Exception {
        Optional<Object> data;
        Optional<Long> count;
        if (StringUtils.equalsAnyIgnoreCase(statement.getFormat(), Statement.FORMAT_SQL, Statement.FORMAT_SQLSCRIPT)) {
            data = executeSql(statement, runtime, executeTarget);
            count = executeSqlCount(effectivePerson, statement, runtime, executeTarget);
        } else {
            data = executeJpql(statement, runtime, executeTarget);
            count = executeJpqlCount(effectivePerson, statement, runtime, executeTarget);
        }
        return Pair.of(data, count);
    }

    private Optional<Object> executeSql(Statement statement, Runtime runtime, ExecuteTarget executeTarget) {
        try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
            Business business = new Business(emc);
            Class<? extends JpaObject> cls = this.clazz(business, statement);
            EntityManager em;
            if (StringUtils.equalsIgnoreCase(statement.getEntityCategory(), Statement.ENTITYCATEGORY_DYNAMIC)
                    && executeTarget.getParsedStatement() instanceof net.sf.jsqlparser.statement.select.Select) {
                em = business.entityManagerContainer().get(DynamicBaseEntity.class);
            } else {
                em = business.entityManagerContainer().get(cls);
            }
            LOGGER.debug("executeSql：{}.", executeTarget::getSql);
            Query query = em.createNativeQuery(executeTarget.getSql());
            for (Map.Entry<String, Object> entry : executeTarget.param.entrySet()) {
                int idx = Integer.parseInt(entry.getKey().substring(1));
                query.setParameter(idx, entry.getValue());
            }
            if (executeTarget.getParsedStatement() instanceof net.sf.jsqlparser.statement.select.Select) {
                if (NumberTools.greaterThan(runtime.page, 0) && NumberTools.greaterThan(runtime.size, 0)) {
                    query.setFirstResult((runtime.page - 1) * runtime.size);
                    query.setMaxResults(runtime.size);
                }
                return Optional.of(query.getResultList());
            } else {
                business.entityManagerContainer().beginTransaction(cls);
                Object data = Integer.valueOf(query.executeUpdate());
                business.entityManagerContainer().commit();
                return Optional.of(data);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return Optional.empty();
    }

    private Optional<Object> executeJpql(Statement statement, Runtime runtime, ExecuteTarget executeTarget) {
        try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
            Business business = new Business(emc);
            Class<? extends JpaObject> cls = this.clazz(business, statement);
            EntityManager em;
            if (StringUtils.equalsIgnoreCase(statement.getEntityCategory(), Statement.ENTITYCATEGORY_DYNAMIC)
                    && executeTarget.getParsedStatement() instanceof net.sf.jsqlparser.statement.select.Select) {
                em = business.entityManagerContainer().get(DynamicBaseEntity.class);
            } else {
                em = business.entityManagerContainer().get(cls);
            }
            LOGGER.debug("executeJpql：{}.", executeTarget::getSql);
            Query query = em.createQuery(executeTarget.getSql());
            for (Parameter<?> p : query.getParameters()) {
                query.setParameter(p.getName(), executeTarget.getParam().get(p.getName()));
            }
            if (executeTarget.getParsedStatement() instanceof net.sf.jsqlparser.statement.select.Select) {
                if (NumberTools.greaterThan(runtime.page, 0) && NumberTools.greaterThan(runtime.size, 0)) {
                    query.setFirstResult((runtime.page - 1) * runtime.size);
                    query.setMaxResults(runtime.size);
                }
                return Optional.of(query.getResultList());
            } else {
                business.entityManagerContainer().beginTransaction(cls);
                Object data = Integer.valueOf(query.executeUpdate());
                business.entityManagerContainer().commit();
                return Optional.of(data);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return Optional.empty();
    }

    private Optional<Object> executeSqlCountAssign(Statement statement, Runtime runtime, ExecuteTarget executeTarget) {
        try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
            Business business = new Business(emc);
            Class<? extends JpaObject> cls = this.clazz(business, statement);
            EntityManager em;
            if (StringUtils.equalsIgnoreCase(statement.getEntityCategory(), Statement.ENTITYCATEGORY_DYNAMIC)
                    && executeTarget.getParsedStatement() instanceof net.sf.jsqlparser.statement.select.Select) {
                em = business.entityManagerContainer().get(DynamicBaseEntity.class);
            } else {
                em = business.entityManagerContainer().get(cls);
            }
            LOGGER.debug("executeSqlCountAssign：{}.", executeTarget::getSql);
            Query query = em.createNativeQuery(executeTarget.getSql());
            for (Map.Entry<String, Object> entry : executeTarget.param.entrySet()) {
                int idx = Integer.parseInt(entry.getKey().substring(1));
                query.setParameter(idx, entry.getValue());
            }
            return Optional.of((Long) query.getSingleResult());

        } catch (Exception e) {
            LOGGER.error(e);
        }
        return Optional.empty();
    }

    private Optional<ExecuteTarget> executeSqlCount(EffectivePerson effectivePerson, Statement statement,
            Runtime runtime,
            ExecuteTarget executeTarget)
            throws Exception {
        if ((!(executeTarget.getParsedStatement() instanceof net.sf.jsqlparser.statement.select.Select))
                || (StringUtils.equalsIgnoreCase(statement.getCountMethod(), Statement.COUNTMETHOD_IGNORE))) {
            return Optional.empty();
        }
        if (StringUtils.equalsIgnoreCase(statement.getCountMethod(), Statement.COUNTMETHOD_AUTO)) {
            return Optional.of(executeCountSqlAuto(effectivePerson, business, statement, runtime, executeTarget));
        } else {
            return executeCountSqlAssign(effectivePerson, statement, runtime);
        }
    }

    private Optional<Long> executeJpqlCount(EffectivePerson effectivePerson, Statement statement,
            Runtime runtime, ExecuteTarget executeTarget) throws Exception {
        if ((!(executeTarget.getParsedStatement() instanceof net.sf.jsqlparser.statement.select.Select))
                || (StringUtils.equalsIgnoreCase(statement.getCountMethod(), Statement.COUNTMETHOD_IGNORE))) {
            return Optional.empty();
        }
        if (StringUtils.equalsIgnoreCase(statement.getCountMethod(), Statement.COUNTMETHOD_AUTO)) {
            return executeCountJpqlAuto(statement, executeTarget.getParsedStatement(), runtime);
        } else {
            return executeCountJpqlAssign(effectivePerson, statement, runtime);
        }
    }

    private Optional<Long> executeCountJpqlAuto(Statement statement,
            net.sf.jsqlparser.statement.Statement stmt, Runtime runtime)
            throws Exception {
        Select select = (Select) stmt;
        try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
            Business business = new Business(emc);
            EntityManager em;
            if (StringUtils.equalsIgnoreCase(statement.getEntityCategory(), Statement.ENTITYCATEGORY_DYNAMIC)) {
                em = business.entityManagerContainer().get(DynamicBaseEntity.class);
            } else {
                Class<? extends JpaObject> cls = this.clazz(business, statement);
                em = business.entityManagerContainer().get(cls);
            }
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            net.sf.jsqlparser.schema.Table table = (net.sf.jsqlparser.schema.Table) plainSelect.getFromItem();
            StringBuilder builder = new StringBuilder();
            builder.append(KEY_SELECT).append(KEY_SPACE).append(KEY_COUNT).append(KEY_LEFT_PARENTHESIS)
                    .append(table.getAlias())
                    .append(KEY_RIGHT_PARENTHESIS).append(KEY_SPACE).append(KEY_FROM).append(KEY_SPACE)
                    .append(table.getFullyQualifiedName()).append(KEY_SPACE).append(table.getAlias());
            Expression exp = plainSelect.getWhere();
            if (null != exp) {
                String whereClause = exp.toString();
                if (StringUtils.isNotBlank(whereClause)) {
                    builder.append(KEY_SPACE).append(KEY_WHERE).append(KEY_SPACE).append(whereClause);
                }
            }
            LOGGER.debug("executeCountJpqlAuto：{}.", builder::toString);
            Query query = em.createQuery(builder.toString());
            for (Parameter<?> p : query.getParameters()) {
                if (runtime.hasParameter(p.getName())) {
                    query.setParameter(p.getName(), runtime.getParameter(p.getName()));
                } else {
                    throw new ExceptionRequiredParameterNotPassed(p.getName());
                }
            }
            return Optional.of((Long) query.getSingleResult());
        }
    }

    private Optional<Long> executeCountJpqlAssign(EffectivePerson effectivePerson, Statement statement,
            Runtime runtime) {
        String jpql = "";
        if (StringUtils.equals(statement.getFormat(), Statement.FORMAT_JPQL)) {
            jpql = statement.getCountData();
        } else {
            jpql = script(effectivePerson, runtime, statement.getCountScriptText());
        }
        try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
            Business business = new Business(emc);
            EntityManager em;
            if (StringUtils.equalsIgnoreCase(statement.getEntityCategory(), Statement.ENTITYCATEGORY_DYNAMIC)) {
                em = business.entityManagerContainer().get(DynamicBaseEntity.class);
            } else {
                Class<? extends JpaObject> cls = this.clazz(business, statement);
                em = business.entityManagerContainer().get(cls);
            }
            LOGGER.debug("executeCountJpqlAssign：{}.", jpql::toString);
            Query query = em.createQuery(jpql);
            for (Parameter<?> p : query.getParameters()) {
                if (runtime.hasParameter(p.getName())) {
                    query.setParameter(p.getName(), runtime.getParameter(p.getName()));
                }
            }
            return Optional.of((Long) query.getSingleResult());
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return Optional.empty();
    }

    private String script(EffectivePerson effectivePerson, Runtime runtime, String scriptText) {
        String text = "";
        try {
            ScriptContext scriptContext = this.scriptContext(effectivePerson, runtime);
            CompiledScript cs = ScriptingFactory.functionalizationCompile(scriptText);
            text = JsonScriptingExecutor.evalString(cs, scriptContext);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return text;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends JpaObject> clazz(Business business, Statement statement) throws Exception {
        Class<? extends JpaObject> cls = null;
        if (StringUtils.equals(Statement.ENTITYCATEGORY_OFFICIAL, statement.getEntityCategory())
                || StringUtils.equals(Statement.ENTITYCATEGORY_CUSTOM, statement.getEntityCategory())) {
            cls = (Class<? extends JpaObject>) Thread.currentThread().getContextClassLoader()
                    .loadClass(statement.getEntityClassName());
        } else {
            Table table = business.entityManagerContainer().flag(statement.getTable(), Table.class);
            if (null == table) {
                throw new ExceptionEntityNotExist(statement.getTable(), Table.class);
            }
            DynamicEntity dynamicEntity = new DynamicEntity(table.getName());
            cls = (Class<? extends JpaObject>) Thread.currentThread().getContextClassLoader()
                    .loadClass(dynamicEntity.className());
        }
        return cls;
    }

    private ScriptContext scriptContext(EffectivePerson effectivePerson, Runtime runtime)
            throws Exception {
        ScriptContext scriptContext = ScriptingFactory.scriptContextEvalInitialServiceScript();
        Resources resources = new Resources();
        resources.setContext(ThisApplication.context());
        resources.setApplications(ThisApplication.context().applications());
        resources.setWebservicesClient(new WebservicesClient());
        resources.setOrganization(new Organization(ThisApplication.context()));
        Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put(ScriptingFactory.BINDING_NAME_SERVICE_RESOURCES, resources);
        bindings.put(ScriptingFactory.BINDING_NAME_SERVICE_EFFECTIVEPERSON, effectivePerson);
        bindings.put(ScriptingFactory.BINDING_NAME_SERVICE_PARAMETERS, gson.toJson(runtime.getParameters()));
        return scriptContext;
    }

    public static class Resources extends AbstractResources {

        private Organization organization;

        public Organization getOrganization() {
            return organization;
        }

        public void setOrganization(Organization organization) {
            this.organization = organization;
        }

    }

    public class ExecuteTarget {
        private String sql;
        private Map<String, Object> param = new LinkedHashMap<>();
        private net.sf.jsqlparser.statement.Statement parsedStatement;

        public String getSql() {
            return sql;
        }

        public Map<String, Object> getParam() {
            return param;
        }

        public net.sf.jsqlparser.statement.Statement getParsedStatement() {
            return parsedStatement;
        }

        public ExecuteTarget(EffectivePerson effectivePerson, Business business, String sql,
                Runtime runtime) throws Exception {
            Matcher matcher = QM_PARAMETER_REGEX.matcher(sql);
            while (matcher.find()) {
                String p = matcher.group(1);
                if (!runtime.hasParameter(p)) {
                    throw new ExceptionRequiredParameterNotPassed(p);
                }
                param.put(p, getParameter(effectivePerson, business.organization(), matcher.group(2), runtime));
            }
            matcher = NAMED_PARAMETER_REGEX.matcher(sql);
            while (matcher.find()) {
                String key = usableQuestionMark(param);
                param.put(key, getParameter(effectivePerson, business.organization(), matcher.group(2), runtime));
                sql = StringUtils.replaceOnce(sql, matcher.group(1), key);
            }
            this.sql = sql;
            parsedStatement = CCJSqlParserUtil.parse(sql);
        }

        private Object getParameter(EffectivePerson effectivePerson, Organization organization, String name,
                Runtime runtime) throws Exception {
            if (StringUtils.equalsIgnoreCase(name, Runtime.PARAMETER_PERSON)) {
                return effectivePerson.getDistinguishedName();
            }
            if (StringUtils.equalsIgnoreCase(name, Runtime.PARAMETER_IDENTITYLIST)) {
                return organization.identity().listWithPerson(effectivePerson);
            }
            if (StringUtils.equalsIgnoreCase(name, Runtime.PARAMETER_UNITLIST)) {
                return organization.unit().listWithPerson(effectivePerson);
            }
            if (StringUtils.equalsIgnoreCase(name, Runtime.PARAMETER_UNITALLLIST)) {
                return organization.unit().listWithPersonSupNested(effectivePerson);
            }
            if (StringUtils.equalsIgnoreCase(name, Runtime.PARAMETER_GROUPLIST)) {
                return organization.group().listWithPerson(effectivePerson);
            }
            if (StringUtils.equalsIgnoreCase(name, Runtime.PARAMETER_ROLELIST)) {
                return organization.role().listWithPerson(effectivePerson);
            }
            if (!runtime.hasParameter(name)) {
                throw new ExceptionRequiredParameterNotPassed(name);
            }
            return runtime.get(name);
        }

        private String usableQuestionMark(Map<String, Object> map) {
            int p = 1;
            while (map.keySet().contains("?" + p)) {
                p++;
            }
            return "?" + p;
        }
    }

}
