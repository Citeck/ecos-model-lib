package ru.citeck.ecos.model.lib.role.service

import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttType
import ru.citeck.ecos.model.lib.role.constants.RoleConstants
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.record.atts.computed.RecordComputedAttValue
import ru.citeck.ecos.records3.record.request.RequestContext

class RoleService(services: ModelServiceFactory) {

    private val typeDefService = services.typeRefService
    private val recordsService = services.records.recordsServiceV1
    private val typesRepo = services.typesRepo
    private val authorityComponent = services.authorityComponent
    private val currentAppName = services.records.properties.appName

    fun getRolesId(typeRef: RecordRef?): List<String> {
        return getRoles(typeRef).map { it.id }
    }

    fun getRoles(typeRef: RecordRef?): List<RoleDef> {

        typeRef ?: return emptyList()
        if (RecordRef.isEmpty(typeRef)) {
            return emptyList()
        }
        return typesRepo.getTypeInfo(typeRef)?.model?.roles ?: emptyList()
    }

    fun isRoleMember(record: Any?, roleId: String?): Boolean {
        if (roleId == RoleConstants.ROLE_EVERYONE) {
            return true
        }
        val currentUserAuthorities = AuthContext.getCurrentUserWithAuthorities()
        val assignees = getAssignees(record, roleId)
        return assignees.any { currentUserAuthorities.contains(it) }
    }

    fun getAssignees(record: Any?, roleId: String?): List<String> {

        roleId ?: return emptyList()
        record ?: return emptyList()

        return getAssignees(record, typeDefService.getTypeRef(record), roleId)
    }

    fun getAssignees(record: Any?, typeRef: RecordRef?, roleId: String?): List<String> {

        record ?: return emptyList()
        typeRef ?: return emptyList()

        if (roleId == RoleConstants.ROLE_EVERYONE) {
            // Role 'EVERYONE' is virtual and doesn't have assignees
            return emptyList()
        }

        if (record is RecordRef && record.appName.isNotEmpty() && record.appName != currentAppName) {
            return recordsService.getAtt(
                record,
                RoleConstants.ATT_ROLES + "." + RoleConstants.ATT_ASSIGNEES_OF + ".$roleId[]?str"
            ).asStrList()
        }

        val roleDef = getRoleDef(typeRef, roleId)
        if (roleDef.id.isBlank()) {
            return emptyList()
        }
        val roleAtts = roleDef.attributes
        val assignees: MutableList<String> = ArrayList()

        if (roleAtts.isNotEmpty()) {

            val atts = roleAtts.associateWith { "$it[]?str" }
            val attsValues = recordsService.getAtts(record, atts)

            roleAtts.forEach {
                val value = attsValues.getAtt(it)
                if (value.isArray()) {
                    assignees.addAll(value.asStrList())
                }
            }
        }
        assignees.addAll(roleDef.assignees)

        val computed = roleDef.computed

        if (computed.type == ComputedAttType.SCRIPT ||
            computed.type == ComputedAttType.VALUE ||
            computed.type == ComputedAttType.ATTRIBUTE ||
            computed.type == ComputedAttType.TEMPLATE
        ) {

            val computedAttValue = RecordComputedAttValue.create()
                .withType(computed.type.toRecordComputedType())
                .withConfig(computed.config)
                .build()

            val authorities = RequestContext.doWithAtts(mapOf("roleAtt" to computedAttValue)) { _ ->
                recordsService.getAtt(record, "\$roleAtt[]?str").asStrList()
            }
            assignees.addAll(authorities)
        }

        val assigneesSet = HashSet<String>()
        val uniqueAssignees = assignees.map { it.trim() }.filter { it.isNotBlank() && assigneesSet.add(it) }
        val names = authorityComponent.getAuthorityNames(uniqueAssignees)
        if (names.size != uniqueAssignees.size) {
            error(
                "Authority component should return list with same length from method getAuthorityNames. " +
                    "Actual names: $names Argument names: $uniqueAssignees"
            )
        }
        if (names === uniqueAssignees) {
            return names
        }
        assigneesSet.clear()
        return names.map { it.trim() }.filter { it.isNotBlank() && assigneesSet.add(it) }
    }

    fun getRoleDef(typeRef: RecordRef?, roleId: String?): RoleDef {

        typeRef ?: return RoleDef.EMPTY
        roleId ?: return RoleDef.EMPTY

        if (RecordRef.isEmpty(typeRef) || roleId.isBlank()) {
            return RoleDef.EMPTY
        }

        if (roleId == RoleConstants.ROLE_EVERYONE) {
            return RoleDef.create {
                withId(RoleConstants.ROLE_EVERYONE)
            }
        }

        return getRoles(typeRef).firstOrNull { it.id == roleId } ?: RoleDef.EMPTY
    }
}
