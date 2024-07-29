package ru.citeck.ecos.model.lib.type.service

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.records2.RecordConstants
import ru.citeck.ecos.webapp.api.entity.EntityRef

class TypeRefService(services: ModelServiceFactory) {

    companion object {
        private const val SUB_TYPE_MAX_ITERATIONS = 100
    }

    private val typesRepo = services.typesRepo
    private val recordsService = services.records.recordsService

    fun isSubType(type: EntityRef, ofType: EntityRef): Boolean {
        if (type == ofType) {
            return true
        }
        var idx = 0
        var parent: EntityRef = getParentRef(type)
        while (idx++ < SUB_TYPE_MAX_ITERATIONS && EntityRef.isNotEmpty(parent)) {
            if (parent.getLocalId() == ofType.getLocalId()) {
                return true
            }
            parent = getParentRef(parent)
        }
        return false
    }

    fun getTypeRef(record: Any?): EntityRef {

        record ?: return EntityRef.EMPTY

        if (record is EntityRef && EntityRef.isEmpty(record)) {
            return EntityRef.EMPTY
        }
        val typeStr = recordsService.getAtt(record, "${RecordConstants.ATT_TYPE}?id").asText()
        return EntityRef.valueOf(typeStr)
    }

    fun expandWithChildren(typeRef: EntityRef?): List<EntityRef> {

        if (typeRef == null || EntityRef.isEmpty(typeRef)) {
            return emptyList()
        }

        val result = ArrayList<EntityRef>()
        forEachDesc(typeRef) {
            result.add(it)
            null
        }

        return result
    }

    fun getParentRef(typeRef: EntityRef): EntityRef {
        return typesRepo.getTypeInfo(typeRef)?.parentRef ?: EntityRef.EMPTY
    }

    fun <T : Any> forEachAsc(typeRef: EntityRef, action: (EntityRef) -> T?): T? {

        val visited = LinkedHashSet<EntityRef>()

        var itTypeRef = typeRef
        while (EntityRef.isNotEmpty(itTypeRef)) {
            val res = action.invoke(itTypeRef)
            if (res != null) {
                return res
            }
            itTypeRef = getParentRef(itTypeRef)
            if (!visited.add(itTypeRef)) {
                error("Cyclic type references: $visited $itTypeRef")
            }
        }
        return null
    }

    fun <T : Any> forEachAscInv(typeRef: EntityRef, action: (EntityRef) -> T?): T? {

        val types = ArrayList<EntityRef>()
        forEachAsc(typeRef) {
            types.add(it)
            null
        }

        for (i in types.size - 1 downTo 0) {
            val type = types[i]
            val res = action.invoke(type)
            if (res != null) {
                return res
            }
        }
        return null
    }

    fun <T : Any> forEachDesc(typeRef: EntityRef, action: (EntityRef) -> T?): T? {
        return forEachDesc(typeRef, action, LinkedHashSet())
    }

    private fun <T : Any> forEachDesc(
        typeRef: EntityRef,
        action: (EntityRef) -> T?,
        visited: MutableSet<EntityRef>
    ): T? {

        if (!visited.add(typeRef)) {
            error("Cyclic type references: $visited $typeRef")
        }

        val typeRes = action.invoke(typeRef)
        if (typeRes != null) {
            return typeRes
        }

        val children = typesRepo.getChildren(typeRef)

        for (child in children) {
            val childRes = forEachDesc(child, action, visited)
            if (childRes != null) {
                return childRes
            }
        }

        return null
    }
}
