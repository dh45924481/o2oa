package com.x.processplatform.service.processing.jaxrs.data;

import com.google.gson.JsonElement;
import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.project.exception.ExceptionEntityNotExist;
import com.x.base.core.project.executor.ProcessPlatformExecutorFactory;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.jaxrs.WoId;
import com.x.base.core.project.logger.Logger;
import com.x.base.core.project.logger.LoggerFactory;
import com.x.base.core.project.tools.ListTools;
import com.x.processplatform.core.entity.content.Work;
import com.x.processplatform.core.express.service.processing.jaxrs.data.DataWi;
import com.x.processplatform.service.processing.Business;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

class ActionCreateWithWorkPath extends BaseAction {

	private static final Logger LOGGER = LoggerFactory.getLogger(ActionCreateWithWorkPath.class);

	ActionResult<Wo> execute(EffectivePerson effectivePerson, String id, String path, JsonElement jsonElement)
			throws Exception {
		LOGGER.debug("execute:{}, id:{}, path:{}.", effectivePerson::getDistinguishedName, () -> id, () -> path);
		Wi wi = this.convertToWrapIn(jsonElement, Wi.class);

		String executorSeed = null;

		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			Work work = emc.fetch(id, Work.class, ListTools.toList(Work.job_FIELDNAME));
			if (null == work) {
				throw new ExceptionEntityNotExist(id, Work.class);
			}
			executorSeed = work.getJob();
		}

		CallableImpl impl = new CallableImpl(id, path, wi);
		return ProcessPlatformExecutorFactory.get(executorSeed).submit(impl).get(300, TimeUnit.SECONDS);
	}

	private class CallableImpl implements Callable<ActionResult<Wo>> {

		private DataWi wi;
		private String id;
		private String path;

		private CallableImpl(String id, String path, DataWi wi) {
			this.id = id;
			this.path = path;
			this.wi = wi;
		}

		@Override
		public ActionResult<Wo> call() throws Exception {
			ActionResult<Wo> result = new ActionResult<>();
			try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
				Business business = new Business(emc);
				Work work = emc.find(id, Work.class);
				if (null == work) {
					throw new ExceptionEntityNotExist(id, Work.class);
				}
				String[] paths = path.split(PATH_SPLIT);
				createData(business, work, wi.getJsonElement(), paths);

				wi.init(work);
				wi.setJsonElement(getData(business, wi.getJob(), paths[0]));
				createDataRecord(business, wi);

				Wo wo = new Wo();
				wo.setId(work.getId());
				result.setData(wo);
			}
			return result;
		}
	}

	public static class Wi extends DataWi {

		private static final long serialVersionUID = -5168423041298533452L;
	}

	public static class Wo extends WoId {

		private static final long serialVersionUID = 5105871346329462375L;

	}

}
