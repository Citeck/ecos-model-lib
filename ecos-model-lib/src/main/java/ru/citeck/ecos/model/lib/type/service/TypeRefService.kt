package ru.citeck.ecos.model.lib.type.service

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.records2.RecordConstants
import ru.citeck.ecos.records2.RecordRef

class TypeRefService(services: ModelServiceFactory) {

    companion object {
        private const val SUB_TYPE_MAX_ITERATIONS = 100
    }

    private val typesRepo = services.typesRepo
    private val recordsService = services.records.recordsServiceV1

    fun isSubType(type: RecordRef, ofType: RecordRef): Boolean {
        if (type == ofType) {
            return true
        }
        var idx = 0
        var parent: RecordRef = getParentRef(type)
        while (idx++ < SUB_TYPE_MAX_ITERATIONS && RecordRef.isNotEmpty(parent)) {
            if (parent.id == ofType.id) {
                return true
            }
            parent = getParentRef(parent)
        }
        return false
    }

    fun getTypeRef(record: Any?): RecordRef {

        record ?: return RecordRef.EMPTY

        if (record is RecordRef && RecordRef.isEmpty(record)) {
            return RecordRef.EMPTY
        }
        val typeStr = recordsService.getAtt(record, "${RecordConstants.ATT_TYPE}?id").asText()
        return RecordRef.valueOf(typeStr)
    }

    fun expandWithChildren(typeRef: RecordRef?): List<RecordRef> {

        if (typeRef == null || RecordRef.isEmpty(typeRef)) {
            return emptyList()
        }

        val result = ArrayList<RecordRef>()
        forEachDesc(typeRef) { result.add(it); null }

        return result
    }

    fun getParentRef(typeRef: RecordRef): RecordRef {
        return typesRepo.getTypeInfo(typeRef)?.parentRef ?: RecordRef.EMPTY
    }

    fun <T : Any> forEachAsc(typeRef: RecordRef, action: (RecordRef) -> T?): T? {

        val visited = LinkedHashSet<RecordRef>()

        var itTypeRef = typeRef
        while (RecordRef.isNotEmpty(itTypeRef)) {
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

    fun <T : Any> forEachAscInv(typeRef: RecordRef, action: (RecordRef) -> T?): T? {

        val types = ArrayList<RecordRef>()
        forEachAsc(typeRef) { types.add(it); null }

        for (i in types.size - 1 downTo 0) {
            val type = types[i]
            val res = action.invoke(type)
            if (res != null) {
                return res
            }
        }
        return null
    }

    fun <T : Any> forEachDesc(typeRef: RecordRef, action: (RecordRef) -> T?): T? {
        return forEachDesc(typeRef, action, LinkedHashSet())
    }

    private fun <T : Any> forEachDesc(
        typeRef: RecordRef,
        action: (RecordRef) -> T?,
        visited: MutableSet<RecordRef>
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
