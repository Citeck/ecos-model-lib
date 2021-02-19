package ru.citeck.ecos.model.lib.status.service

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.status.dto.StatusDef
import ru.citeck.ecos.records2.RecordRef

class StatusService(services: ModelServiceFactory) {

    private val typeDefService = services.typeRefService
    private val typesRepo = services.typesRepo

    fun getStatusDefByType(typeRef: RecordRef?, statusId: String?): StatusDef? {
        statusId ?: return null
        return getStatusesByType(typeRef)[statusId]
    }

    fun getStatusesByDocument(documentRef: RecordRef?): Map<String, StatusDef> {
        return getStatusesByType(typeDefService.getTypeRef(documentRef))
    }

    fun getStatusesByType(typeRef: RecordRef?): Map<String, StatusDef> {

        typeRef ?: return emptyMap()

        val statuses = typesRepo.getModel(typeRef).statuses

        val result = LinkedHashMap<String, StatusDef>()
        for (status in statuses) {
            result[status.id] = status
        }

        return result
    }
}
