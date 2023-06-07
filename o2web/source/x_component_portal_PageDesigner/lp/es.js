MWF.xApplication.portal.PageDesigner = MWF.xApplication.portal.PageDesigner || {};
MWF.APPPOD = MWF.xApplication.portal.PageDesigner;
MWF.APPPOD.LP = {
    "title": "Edición de página",
    "newPage": "Nueva página",
    "property": "Propiedades",
    "tools": "Herramientas",
    "all": "Todo",

    "repetitionsId": "Identificador de elemento duplicado",
    "notNullId": "El identificador del elemento no puede estar vacío",
    "history": "Historial",
    "componentTree": "Árbol",

    "button":{
        "ok": "Aceptar",
        "cancel": "Cancelar"
    },

    "notice": {
        "save_success": "¡El formulario se ha guardado con éxito!",
		"saveTemplate_success": "¡Se ha guardado el modelo de formulario con éxito!",
		"saveTemplate_inputName": "Ingrese el título del modelo",
		"saveTemplate_inputCategory": "Seleccione la categoría del modelo",

		"deleteElementTitle": "Confirmación",
		"deleteElement": "¿Está seguro de que desea eliminar el elemento actual y sus subelementos?",

		"deleteRowTitle": "Confirmación",
		"deleteRow": "La eliminación de la fila actual eliminará el contenido de todas las celdas en esa fila. ¿Está seguro de que desea eliminar la fila seleccionada?",
		"deleteColTitle": "Confirmación",
		"deleteCol": "La eliminación de la columna actual eliminará el contenido de todas las celdas en esa columna. ¿Está seguro de que desea eliminar la columna seleccionada?",
		"deleteEventTitle": "Confirmación",
		"deleteEvent": "¿Está seguro de que desea eliminar el evento actual?",

		"deleteActionTitle": "Confirmación",
		"deleteAction": "¿Está seguro de que desea eliminar la acción actual?",

		"deleteButtonTitle": "Confirmación",
		"deleteButton": "¿Está seguro de que desea eliminar el botón de acción actual?",

		"notUseModuleInMobile": "Este componente no es compatible con dispositivos móviles",

		"changeToSequenceTitle": "Confirmación",
		"changeToSequence": "Esta acción eliminará los componentes agregados. ¿Está seguro de que desea cambiar a la columna 'N° de Orden'?",

        //"confiltNoPix" : "请填写前缀或后缀",
        "selectPage" : "Por favor seleccione una página",
        "selectPortal" : "Por favor seleccione un portal",
        "selectWidget" : "Por favor seleccione un widget",
        //"moduleConflitError" : "组件标识重复",
        //"moduleConflitErrorOnPix" : "添加了前缀或后缀后组件标识仍然重复"
        "widgetNameEmpty": "Por favor, ingrese el nombre del widget",
        "widgetNameConflict" : "Conflicto de nombres de widget",
        "widget_save_success" : "Widget guardado exitosamente"
    },

    "formAction": {
        "insertRow": "Insertar fila",
		"insertCol": "Insertar columna",
		"deleteRow": "Eliminar fila",
		"deleteCol": "Eliminar columna",
        "mergerCell": "Combinar celdas",
		"splitCell": "Dividir celda",
		"move": "Mover",
		"copy": "Copiar",
		"delete": "Eliminar",
		"add": "Agregar",
        "script": "Script",
        "makeWidget" : "Establecer como widget",
        "defaultWidgetName" : "Widget"
    },

    "actionbar": {
        "readhide": "Establecer si se muestra al leer",
		"edithide": "Establecer si se muestra al editar",
		"hideCondition": "Establecer condición de ocultación",
		"title": "Título",
		"img": "Icono",
		"action": "Acción",
		"condition": "Condición de visualización",
		"editScript": "Editar script de acción",
		"editCondition": "(retornar verdadero para ocultar acción)"
    },
    "isSave": "Guardando, por favor espere...",
    "validation": {
        "validation": "Validación",
		"anytime": "siempre",
		"decision": "Decisión",
		"decisionName": "<Nombre de decisión>",
		"value": "Valor",
		"length": "Longitud de valor",
		"valueInput": "<Valor>",
		"isnull": "Es nulo",
		"notnull": "No es nulo",
		"gt": "Mayor que",
		"lt": "Menor que",
		"equal": "Igual a",
		"neq": "No igual a",
		"contain": "Contiene",
		"notcontain": "No contiene",
		"prompt": "Indicación",
		"add": "añadir",
		"modify": "edit",
		"when": "Cuando",
		"as": "como",
		"inputDecisionName": "Ingrese el nombre de la decisión",
		"inputValue": "Ingrese el valor",
		"inputPrompt": "Ingrese el contenido de la indicación",
		"delete_title": "Confirmación",
		"delete_text": "¿Está seguro de que desea eliminar esta validación?"
    },
    "selectIcon": "Seleccionar icono",
    "selectImage": "Seleccionar imagen",
    "dutyInputTitle": "Agregar parámetros de puesto",
    "dutyInput": "Por favor, seleccione una organización para el puesto \"{duty}\"",
    "creatorCompany": "Empresa del autor",
    "creatorDepartment": "Departamento del autor",
    "currentCompany": "Empresa actual",
    "currentDepartment": "Departamento actual",
    "deleteDutyTitle": "Confirmación",
    "deleteDutyText": "¿Estás seguro de que deseas eliminar el puesto \"{duty}\"?",
    "select": "Seleccionar",
    "empty": "Vaciar",

    "saveTemplate": "Guardar como plantilla de formulario",
    "templateName": "Nombre de la plantilla",
    "templateCategory": "Categoría de la plantilla",
    "templateDescription": "Descripción de la plantilla",
    "save": "Guardar",
    "cancel": "Cancelar",
    "newCategory": "Nueva categoría",
    "filter": {
        "and": "Y",
        "or": "O",
        "equals": "Igual a",
        "notEquals": "No igual a",
        "greaterThan": "Mayor que",
        "greaterThanOrEqualTo": "Mayor o igual que",
        "lessThan": "Menor que",
        "lessThanOrEqualTo": "Menor o igual que",
        "like": "Coincide con",
        "notLike": "No coincide con",
        "from" : "Desde",
        "value" : "Valor"
    },
    "mastInputPath": "Ingrese la ruta de datos",
    "mastInputTitle": "Ingrese el título",
    "delete_filterItem_title": "Confirmación",
    "delete_filterItem": "¿Está seguro de que desea eliminar la condición de filtro actual?",
    "implodeError": "Error en el formato de los datos que se van a importar.",
    "implodeEmpty": "Por favor ingrese los datos que desea importar en el cuadro de edición.",
    "implodeConfirmTitle": "Confirmación de importación",
    "implodeConfirmText": "La importación de datos borrará la página actual y no se puede deshacer, ¿está seguro de que desea importar?",

    "subpageNameConflictTitle": "Conflicto de nombres de campo de subpágina",
    "subpageNameConflictInfor": "Los siguientes campos del subpágina tienen un nombre que entra en conflicto con el formulario existente:\n{name}",
    "subpageConflictTitle": "Error al incrustar la subpágina",
    "subpageConflictInfor": "No se puede incrustar dos subpáginas iguales.",

    "subpageNestedTitle": "Error al incrustar la subpágina",
    "subpageNestedInfor": "No se pueden anidar páginas secundarias entre sí.",
    "checkSubpageNestedError" : "Las subpáginas seleccionadas tienen una anidación mutua, ¡por favor revise!",

    "checkSubpageTitle": "Validación de guardado de página",
    "checkFormSaveError": "No se puede guardar la página debido a los siguientes motivos:<br>",
    "checkSubpagePcInfor": "En la página PC, los siguientes subcampos tienen conflictos de nombres:<br>{subform}<br>",
    "checkSubpageMobileInfor": "En la página móvil, los siguientes subcampos tienen conflictos de nombres:<br>{subform}",


    "design": "Diseño",
    "script": "Script",
    "html": "HTML",
    "css": "CSS",
    "byModule": "Por diseño",
    "byPath": "Por script",
    "events": "Eventos",
    "importO2": "Importar desde datos O2",
    "importHTML": "Importar desde HTML",
    "importOffice": "Importar desde WORD o EXCEL",
    "importO2_infor": "Copie los datos del formulario en formato O2 en el editor siguiente. (Use el botón 'Exportar' de la barra de herramientas del diseñador de formularios o páginas para obtener los datos del formulario) <br/>Presione Ctrl+Alt+I para dar formato a los datos.",
    "importHTML_infor": "Copie los datos HTML en el editor siguiente. Presione Ctrl+Alt+I para dar formato a los datos.",
    "importHTML_infor2": "Copie los datos CSS en el editor siguiente. Presione Ctrl+Alt+I para dar formato a los datos.",
    "importOffice_infor": "Seleccione un archivo de Word o Excel.",
    "import_ok": "Importar",
    "import_cancel": "Cancelar",
    "import_option1": "Agregar cuadros de texto en celdas vacías de la tabla",
    "import_option2": "Eliminar elementos vacíos",
    "implodeOfficeEmpty": "Seleccione primero el archivo de Word o Excel que desea importar.",
    "applicationNotFound": "Aplicación no encontrada",

    "scriptTitle": {
        "validationOpinion": "Validación de opiniones del formulario",
        "validationRoute": "Validación de rutas del formulario",
        "validationFormCustom": "Validación de formularios",
        "defaultValue": "Valor predeterminado",
        "validation": "Script de validación",
        "sectionByScript": "Sección según script",
        "itemScript": "Script de valores opcionales",
        "iframeScript": "Script de iframe",
        "labelScript": "Valor de texto",
        "rangeKey": "Alcance organizativo",
        "identityRangeKey": "Alcance organizativo de identidad",
        "unitRangeKey": "Alcance seleccionable de la organización",
        "rangeDutyKey": "Alcance de deberes",
        "exclude": "Script de exclusión de selección",
        "cookies": "Cookies de solicitud de fuente de datos",
        "requestBody": "Cuerpo de mensaje de solicitud de fuente de datos",
        "jsonText": "Texto de datos",
        "dataScript": "Script de control de árbol",
        "itemDynamic": "Script de opciones dinámicas",
        "defaultData": "Valor predeterminado de la cuadrícula de datos",
        "editableScript": "¿Es editable la cuadrícula de datos?",
        "config": "Configuración de HTML Editor",
        "filterScript": "Filtro de registro de proceso",
        "readScript": "Script de solo lectura de Office",
        "fileSite": "Script de sitio de archivo de Office",
        "subformScript": "Script de subformulario",
        "selectedScript": "Script de selección de vista",
        "action.tools": "Botones de acción"
    },
    "selectorButton" : {
        "ok" : "Aceptar",
        "cancel" : "Cancelar"
    },
    "modules": {
        "label": "Texto",
        "textfield": "Campo de texto",
        "number": "Numérico",
        "org": "Organización",
        "calendar": "Fecha y hora",
        "textarea": "Texto multilínea",
        "select": "Desplegable",
        "radio": "Botón de opción",
        "checkbox": "CheckBox",
        "combox": "ComboBox",
        "opinion": "OpinionBox",
        "button": "Botón",
        "Address": "Dirección",
        "Actionbar": "Barra de acción",
        "Sidebar": "Sidebar",
        "image": "Imagen",
        "imageclipper": "SelectImg",
        "attachment": "Adjunto",
        "div": "Contenedor",
        "table": "Tabla",
        "datagrid": "Grid(obsoleto)",
        "datatable": "Tablas de datos",
        "datatemplate": "DataTemplate",
        "subform": "Subformulario",
        "ViewSelector": "Seleccionar vista",
        "view": "Vista incrustada",
        "stat": "Estadística",
        "html": "HTML",
        "common": "Comunes",
        "tab": "Pestaña",
        "tree": "Árbol",
        "log": "Registro",
        "monitor": "Monitoreo",
        "iframe": "Iframe",
        "documenteditor": "DocEditor",
        "htmledit": "Editor de HTML",
        "office": "Office",
        "statementSelector": "StatementSelector",
        "statement": "Statement",
        "source": "Fuente",
		"subSource": "Subfuente",
		"sourceText": "Texto fuente",
		"widget": "Widget",
		"widgetmodules": "Elem. widget",
		"address": "Dirección",
		"importer": "Importador",
        "SmartBI":"Informe SmartBI",
        "application": "Aplicación",

        "group_all": "Todos",
        "group_form": "Formulario",
        "group_layout": "Diseño",
        "group_data": "Datos",
        "group_filetext": "FileEdit",
        "group_function": "Función",
        "group_element": "Element",
    },
    "filedConfigurator": {
        "sequence": "Secuencia",
        "fieldTitle": "Título del campo",
        "fieldId": "Identificación del campo",
        "action": "Acción",
        "moveup": "Mover a la fila anterior",
        "deleteRow": "Eliminar fila",
        "insertRow": "Insertar fila",
        "importFromForm": "Importar configuración de campos desde la interfaz"
    }
};
