package ru.citeck.ecos.model.lib.role.service

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.role.constants.RoleConstants
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.records2.RecordRef

class RoleService(services: ModelServiceFactory) {

    private val typeDefService = services.typeRefService
    private val recordsService = services.records.recordsServiceV1
    private val typesRepo = services.typesRepo

    fun getRolesId(typeRef: RecordRef?): List<String> {
        return getRoles(typeRef).map { it.id }
    }

    fun getRoles(typeRef: RecordRef?): List<RoleDef> {

        typeRef ?: return emptyList()
        if (RecordRef.isEmpty(typeRef)) {
            return emptyList()
        }
        return typesRepo.getModel(typeRef).roles
    }

    fun getAssignees(record: Any?, roleId: String?): List<String> {

        roleId ?: return emptyList()
        record ?: return emptyList()

        return getAssignees(record, typeDefService.getTypeRef(record), roleId)
    }

    fun getAssignees(record: Any?, typeRef: RecordRef?, roleId: String?): List<String> {

        record ?: return emptyList()
        typeRef ?: return emptyList()

        if (roleId == RoleConstants.ROLE_ALL) {
            // Role 'ALL' is virtual and doesn't have assignees
            return emptyList()
        }

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
        return assignees.map { it.trim() }.filter { it.isNotBlank() && assigneesSet.add(it) }
    }

    fun getRoleDef(typeRef: RecordRef?, roleId: String?): RoleDef {

        typeRef ?: return RoleDef.EMPTY
        roleId ?: return RoleDef.EMPTY

        if (RecordRef.isEmpty(typeRef) || roleId.isBlank()) {
            return RoleDef.EMPTY
        }

        if (roleId == RoleConstants.ROLE_ALL) {
            return RoleDef.create {
                withId(RoleConstants.ROLE_ALL)
            }
        }

        return getRoles(typeRef).firstOrNull { it.id == roleId } ?: RoleDef.EMPTY
    }
}
