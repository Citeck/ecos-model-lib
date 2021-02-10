package ru.citeck.ecos.model.lib.status.service

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.status.dto.StatusDef
import ru.citeck.ecos.records2.RecordRef

class StatusService(services: ModelServiceFactory) {
    private val typeDefService = services.typeDefService

    fun getStatusDefByDocument(documentRef: RecordRef?, statusId: String?): StatusDef? {
        val typeRef = typeDefService.getTypeRef(documentRef)
        return getStatusDefByType(typeRef, statusId)
    }

    fun getStatusDefByType(typeRef: RecordRef?, statusId: String?): StatusDef? {
        statusId ?: return null
        return getStatusesByType(typeRef)[statusId]
    }

    fun getStatusesByDocument(documentRef: RecordRef?): Map<String, StatusDef> {
        val typeRef = typeDefService.getTypeRef(documentRef)
        return getStatusesByType(typeRef)
    }

    fun getStatusesByType(typeRef: RecordRef?): Map<String, StatusDef> {
        typeRef ?: return emptyMap()

        val statuses = ArrayList<StatusDef>()
        typeDefService.forEachAsc(typeRef) { typeDef ->
            statuses.addAll(typeDef.model.statuses)
            false
        }

        val result = LinkedHashMap<String, StatusDef>()
        for (idx in statuses.lastIndex downTo 0) {
            val att = statuses[idx]
            result[att.id] = att
        }

        return result
    }
}
