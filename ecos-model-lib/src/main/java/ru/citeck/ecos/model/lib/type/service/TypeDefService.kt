package ru.citeck.ecos.model.lib.type.service

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.status.dto.StatusDef
import ru.citeck.ecos.model.lib.type.dto.DocLibDef
import ru.citeck.ecos.model.lib.type.dto.TypeDef
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records2.RecordConstants
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.record.op.atts.service.computed.ComputedAtt
import ru.citeck.ecos.records3.record.op.atts.service.computed.ComputedAttType
import ru.citeck.ecos.records3.record.type.RecordTypeService
import java.util.concurrent.atomic.AtomicReference

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

    fun getDocLib(typeRef: RecordRef?): DocLibDef {

        typeRef ?: return DocLibDef.EMPTY

        val typeDef = typesRepo.getTypeDef(typeRef) ?: return DocLibDef.EMPTY
        if (!typeDef.docLib.enabled) {
            return DocLibDef.EMPTY
        }

        val dirType = typeDef.docLib.dirTypeRef
        val fileTypes = typeDef.docLib.fileTypeRefs

        return DocLibDef.create {
            enabled = true
            fileTypeRefs = if (fileTypes.isEmpty()) {
                listOf(TypeUtils.DOCLIB_DEFAULT_FILE_TYPE)
            } else {
                fileTypes
            }
            dirTypeRef = if (RecordRef.isEmpty(dirType)) {
                TypeUtils.DOCLIB_DEFAULT_DIR_TYPE
            } else {
                dirType
            }
        }
    }

    fun getNumTemplate(typeRef: RecordRef): RecordRef? {

        val result = AtomicReference<RecordRef>()

        forEachAsc(typeRef) { typeDto ->

            val numTemplateRef: RecordRef? = typeDto.numTemplateRef
            if (RecordRef.isNotEmpty(numTemplateRef)) {
                result.set(numTemplateRef)
                true
            } else {
                !typeDto.inheritNumTemplate
            }
        }

        return result.get()
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

    fun getTypeDef(typeRef: RecordRef): TypeDef? {
        return typesRepo.getTypeDef(typeRef)
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

    fun expandTypeWithChildren(typeRef: RecordRef?): List<RecordRef> {

        if (typeRef == null || RecordRef.isEmpty(typeRef)) {
            return emptyList()
        }

        val result = ArrayList<RecordRef>()
        forEachDesc(typeRef) { typeDef ->
            result.add(TypeUtils.getTypeRef(typeDef.id))
        }

        return result
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
