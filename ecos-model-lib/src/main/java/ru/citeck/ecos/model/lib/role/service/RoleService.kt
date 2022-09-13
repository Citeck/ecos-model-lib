package ru.citeck.ecos.model.lib.role.service

import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.context.lib.auth.AuthGroup
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.ModelServiceFactoryAware
import ru.citeck.ecos.model.lib.attributes.dto.computed.ComputedAttType
import ru.citeck.ecos.model.lib.role.api.records.RolesMixin
import ru.citeck.ecos.model.lib.role.constants.RoleConstants
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.TypeRefService
import ru.citeck.ecos.records3.RecordsService
import ru.citeck.ecos.records3.record.atts.computed.RecordComputedAttValue
import ru.citeck.ecos.records3.record.request.RequestContext
import ru.citeck.ecos.webapp.api.authority.EcosAuthorityService
import ru.citeck.ecos.webapp.api.entity.EntityRef

class RoleService : ModelServiceFactoryAware {

    private lateinit var typeRefService: TypeRefService
    private lateinit var recordsService: RecordsService
    private lateinit var typesRepo: TypesRepo
    private lateinit var currentAppName: String
    private var authorityService: EcosAuthorityService? = null

    override fun setModelServiceFactory(services: ModelServiceFactory) {
        typeRefService = services.typeRefService
        recordsService = services.records.recordsServiceV1
        typesRepo = services.typesRepo
        authorityService = services.getEcosWebAppContext()?.getAuthorityService()
        currentAppName = services.webappProps.appName
        services.records.globalAttMixinsProvider.addMixin(RolesMixin(this))
    }

    fun getCurrentUserRoles(record: Any?): List<String> {
        return getCurrentUserRoles(record, typeRefService.getTypeRef(record))
    }

    fun getCurrentUserRoles(record: Any?, typeRef: EntityRef?): List<String> {
        return getRolesForAuthorities(record, typeRef, AuthContext.getCurrentUserWithAuthorities())
    }

    fun getRolesForAuthorities(record: Any?, authorities: Collection<String>?): List<String> {
        return getRolesForAuthorities(record, typeRefService.getTypeRef(record), authorities)
    }

    fun getRolesForAuthorities(record: Any?, typeRef: EntityRef?, authorities: Collection<String>?): List<String> {

        record ?: return emptyList()

        val roles = getRolesId(typeRef)
        if (roles.isEmpty()) {
            return emptyList()
        }

        val assigneesByRoleId = getAssignees(record, typeRef, roles)
        val currentUserAuthorities = authorities?.toSet() ?: emptySet()

        val result = mutableListOf<String>()
        assigneesByRoleId.forEach { (roleId, assignees) ->
            if (roleId == RoleConstants.ROLE_EVERYONE || assignees.any { currentUserAuthorities.contains(it) }) {
                result.add(roleId)
            }
        }
        return result
    }

    fun getRolesId(typeRef: EntityRef?): List<String> {
        return getRoles(typeRef).map { it.id }
    }

    fun getRoles(typeRef: EntityRef?): List<RoleDef> {

        typeRef ?: return emptyList()
        if (EntityRef.isEmpty(typeRef)) {
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

    fun getAssignees(record: Any?, roles: Collection<String>?): Map<String, List<String>> {
        return getAssignees(record, typeRefService.getTypeRef(record), roles)
    }

    fun getAssignees(record: Any?, roleId: String?): List<String> {
        return getAssignees(record, typeRefService.getTypeRef(record), roleId)
    }

    fun getAssignees(record: Any?, typeRef: EntityRef?, roleId: String?): List<String> {
        if (record == null || typeRef == null || roleId.isNullOrBlank()) {
            return emptyList()
        }
        return getAssignees(record, typeRef, listOf(roleId)).getOrDefault(roleId, emptyList())
    }

    fun getAssignees(record: Any?, typeRef: EntityRef?, roles: Collection<String>?): Map<String, List<String>> {

        if (roles.isNullOrEmpty() || record == null || typeRef == null || typeRef.getLocalId().isBlank()) {
            return roles?.associateWith { emptyList() } ?: emptyMap()
        }

        val rolesToEval = roles.toMutableSet()
        val result = LinkedHashMap<String, List<String>>()

        val it = rolesToEval.iterator()
        while (it.hasNext()) {
            val roleId = it.next()
            if (roleId == RoleConstants.ROLE_EVERYONE) {
                // Role 'EVERYONE' should always contain GROUP_EVERYONE
                result[roleId] = arrayListOf(AuthGroup.EVERYONE)
                it.remove()
            } else if (roleId.isBlank()) {
                result[roleId] = emptyList()
                it.remove()
            }
        }
        if (rolesToEval.isEmpty()) {
            return result
        }

        if (record is EntityRef && record.getAppName().isNotEmpty() && record.getAppName() != currentAppName) {
            val assigneesAtts = rolesToEval.associateWith {
                RoleConstants.ATT_ROLES + "." + RoleConstants.ATT_ASSIGNEES_OF + ".$it[]?str"
            }
            recordsService.getAtts(record, assigneesAtts).forEach { roleId, assignees ->
                result[roleId] = assignees.asStrList()
            }
            return result
        }

        val roleDefById = getRoles(typeRef).associateBy { it.id }

        val rolesDef = rolesToEval.mapNotNull {
            val def = roleDefById[it]
            if (def == null) {
                result[it] = emptyList()
            }
            def
        }

        // todo: join record attributes calculation
        rolesDef.forEach { roleDef ->

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

            val names = authorityService?.getAuthorityNames(uniqueAssignees) ?: uniqueAssignees

            if (names.size != uniqueAssignees.size) {
                error(
                    "Authority component should return list with same length from method getAuthorityNames. " +
                        "Actual names: $names Argument names: $uniqueAssignees"
                )
            }
            if (names === uniqueAssignees) {
                result[roleDef.id] = names
            }
            assigneesSet.clear()
            result[roleDef.id] = names.map { it.trim() }.filter { it.isNotBlank() && assigneesSet.add(it) }
        }
        return result
    }

    fun getRoleDef(typeRef: EntityRef?, roleId: String?): RoleDef {

        typeRef ?: return RoleDef.EMPTY
        roleId ?: return RoleDef.EMPTY

        if (EntityRef.isEmpty(typeRef) || roleId.isBlank()) {
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
