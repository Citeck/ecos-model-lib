package ru.citeck.ecos.model.lib.type.service

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.status.dto.StatusDef
import ru.citeck.ecos.model.lib.type.dto.TypeDef
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.records2.RecordRef

class TypeDefService(services: ModelServiceFactory) {

    private val typesRepo = services.typesRepo

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
