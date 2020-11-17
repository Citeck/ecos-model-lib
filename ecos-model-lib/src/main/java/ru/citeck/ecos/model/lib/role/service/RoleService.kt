package ru.citeck.ecos.model.lib.role.service

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.records2.RecordRef
import java.util.concurrent.atomic.AtomicReference

class RoleService(services: ModelServiceFactory) {

    private val typeDefService = services.typeDefService
    private val recordsService = services.records.recordsServiceV1

    fun getRolesId(typeRef: RecordRef?): List<String> {
        return getRoles(typeRef).map { it.id }
    }

    fun getRoles(typeRef: RecordRef?): List<RoleDef> {

        typeRef ?: return emptyList()
        if (RecordRef.isEmpty(typeRef)) {
            return emptyList()
        }
        val roles = mutableListOf<RoleDef>()
        typeDefService.forEachAsc(typeRef) {
            roles.addAll(it.model.roles)
            false
        }

        return roles
    }

    fun getAssignees(record: Any?, roleId: String?): List<String> {

        record ?: return emptyList()
        roleId ?: return emptyList()

        return getAssignees(record, typeDefService.getTypeRef(record), roleId)
    }

    fun getAssignees(record: Any?, typeRef: RecordRef?, roleId: String?): List<String> {

        record ?: return emptyList()
        typeRef ?: return emptyList()

        val roleDef = getRoleDef(typeRef, roleId)
        if (roleDef.id.isBlank()) {
            return emptyList()
        }
        val assignees: MutableList<String>
        assignees = if (roleDef.attribute.isNotBlank()) {
            recordsService.getAtt(record, "${roleDef.attribute}[]?str").asStrList()
        } else {
            ArrayList()
        }
        assignees.addAll(roleDef.assignees)

        val assigneesSet = HashSet<String>()
        return assignees.filter { it.isNotBlank() && assigneesSet.add(it) }
    }

    fun getRoleDef(typeRef: RecordRef?, roleId: String?): RoleDef {

        typeRef ?: return RoleDef.EMPTY
        roleId ?: return RoleDef.EMPTY

        if (RecordRef.isEmpty(typeRef) || roleId.isBlank()) {
            return RoleDef.EMPTY
        }

        val resRoleDef = AtomicReference<RoleDef>()
        typeDefService.forEachAsc(typeRef) {
            for (roleDef in it.model.roles) {
                if (roleDef.id == roleId) {
                    resRoleDef.set(roleDef)
                    break
                }
            }
            resRoleDef.get() != null
        }

        return resRoleDef.get() ?: RoleDef.EMPTY
    }
}
