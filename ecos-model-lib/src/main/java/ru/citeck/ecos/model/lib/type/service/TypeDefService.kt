package ru.citeck.ecos.model.lib.type.service

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.status.dto.StatusDef
import ru.citeck.ecos.model.lib.type.dto.TypeDef
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.records2.RecordConstants
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.record.op.atts.service.computed.ComputedAtt
import ru.citeck.ecos.records3.record.op.atts.service.computed.ComputedAttType
import ru.citeck.ecos.records3.record.type.RecordTypeService

class TypeDefService(services: ModelServiceFactory) : RecordTypeService {

    private val typesRepo = services.typesRepo
    private val recordsService = services.records.recordsServiceV1

    override fun getComputedAtts(typeRef: RecordRef): List<ComputedAtt> {
        return getAttributes(typeRef).filter {
            it.computed.type != ComputedAttType.NONE
        }.map {
            ComputedAtt(it.id, it.computed)
        }
    }

    fun getAttributes(typeRef: RecordRef): List<AttributeDef> {

        val attributes = ArrayList<AttributeDef>()

        forEachAsc(typeRef) { typeDef ->
            attributes.addAll(typeDef.model.attributes)
            false
        }

        val result = LinkedHashMap<String, AttributeDef>()
        for (idx in attributes.lastIndex downTo 0) {
            val att = attributes[idx]
            result[att.id] = att
        }

        return result.values.toList().reversed()
    }

    fun getTypeRef(record: Any?): RecordRef {

        record ?: return RecordRef.EMPTY

        if (record is RecordRef && RecordRef.isEmpty(record)) {
            return RecordRef.EMPTY
        }
        val typeStr = recordsService.getAtt(record, "${RecordConstants.ATT_TYPE}?id").asText()
        return RecordRef.valueOf(typeStr)
    }

    fun getModelDef(typeRef: RecordRef): TypeModelDef {

        val atts = ArrayList<AttributeDef>()
        val roles = ArrayList<RoleDef>()
        val statuses = ArrayList<StatusDef>()

        forEachAsc(typeRef) {
            atts.addAll(0, it.model.attributes)
            roles.addAll(0, it.model.roles)
            statuses.addAll(0, it.model.statuses)
            false
        }
        return TypeModelDef.create()
            .withRoles(roles)
            .withStatuses(statuses)
            .withAttributes(atts)
            .build()
    }

    fun getChildren(typeRef: RecordRef): List<RecordRef> {
        return typesRepo.getChildren(typeRef)
    }

    fun forEachAsc(typeRef: RecordRef, action: (TypeDef) -> Boolean) {

        var typeDef: TypeDef? = typesRepo.getTypeDef(typeRef)

        while (typeDef != null && !action.invoke(typeDef)) {
            val parent = typeDef.parentRef
            if (parent != null) {
                typeDef = typesRepo.getTypeDef(parent)
            } else {
                break
            }
        }
    }

    fun forEachDesc(typeRef: RecordRef, action: (TypeDef) -> Unit) {

        val typeDef = typesRepo.getTypeDef(typeRef) ?: return

        action.invoke(typeDef)
        for (childRef in typesRepo.getChildren(typeRef)) {
            forEachDesc(childRef, action)
        }
    }
}
