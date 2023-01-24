package ru.citeck.ecos.model.lib.num.service

import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.num.dto.NumTemplateDef
import ru.citeck.ecos.webapp.api.entity.EntityRef

class EcosNumService(services: ModelServiceFactory) {

    private val ecosModelAppApi = services.ecosModelAppApi
    private val numTemplatesRepo = services.numTemplatesRepo
    private val recordsService = services.records.recordsServiceV1

    fun getNumberTemplate(templateRef: EntityRef): NumTemplateDef? {
        return numTemplatesRepo.getNumTemplate(templateRef)
    }

    fun getNextNumberForRecord(record: Any, templateRef: EntityRef): Long {
        val templateDef = getNumberTemplate(templateRef) ?: error("Template is not found: $templateRef")
        val model = recordsService.getAtts(record, templateDef.modelAttributes).getAttributes()
        return getNextNumberForModel(model, templateRef)
    }

    fun getNextNumberForModel(model: ObjectData, templateRef: EntityRef): Long {
        return ecosModelAppApi.getNextNumberForModel(model, templateRef)
    }
}
