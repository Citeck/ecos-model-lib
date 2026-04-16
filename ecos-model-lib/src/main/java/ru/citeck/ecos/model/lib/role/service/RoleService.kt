package ru.citeck.ecos.model.lib.role.service

import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.context.lib.auth.AuthGroup
import ru.citeck.ecos.context.lib.auth.AuthRole
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.ModelServiceFactoryAware
import ru.citeck.ecos.model.lib.role.api.records.RolesMixin
import ru.citeck.ecos.model.lib.role.constants.RoleConstants
import ru.citeck.ecos.model.lib.role.dto.ComputedRoleType
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.TypeRefService
import ru.citeck.ecos.records3.RecordsService
import ru.citeck.ecos.records3.record.atts.computed.RecordComputedAttType
import ru.citeck.ecos.records3.record.atts.computed.RecordComputedAttValue
import ru.citeck.ecos.records3.record.atts.value.AttValue
import ru.citeck.ecos.records3.record.atts.value.AttValueCtx
import ru.citeck.ecos.records3.record.request.RequestContext
import ru.citeck.ecos.txn.lib.TxnContext
import ru.citeck.ecos.webapp.api.authority.EcosAuthoritiesApi
import ru.citeck.ecos.webapp.api.entity.EntityRef
import ru.citeck.ecos.webapp.api.entity.toEntityRef

class RoleService : ModelServiceFactoryAware {

    companion object {
        private val COMPUTED_ATT_TYPES_TO_CALC_USING_RECORDS_SERVICE = setOf(
            ComputedRoleType.SCRIPT,
            ComputedRoleType.VALUE,
            ComputedRoleType.ATTRIBUTE
        )
    }

    private lateinit var typeRefService: TypeRefService
    private lateinit var typesRepo: TypesRepo
    private lateinit var currentAppName: String
    private var authoritiesApi: EcosAuthoritiesApi? = null

    private lateinit var recordsService: RecordsService
    private lateinit var computedRoleProcessor: ComputedRoleProcessor

    private val rolesCacheTxnKey = Any()

    override fun setModelServiceFactory(services: ModelServiceFactory) {
        recordsService = services.records.recordsService
        computedRoleProcessor = ComputedRoleProcessor(services)

        typeRefService = services.typeRefService
        typesRepo = services.typesRepo
        authoritiesApi = services.getEcosWebAppApi()?.getAuthoritiesApi()
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

        val result = LinkedHashSet<String>()

        val currentUserAuthorities = authorities?.toSet() ?: emptySet()

        val roles = getRolesId(typeRef)
        if (roles.isNotEmpty()) {
            val assigneesByRoleId = getAssignees(record, typeRef, roles)
            assigneesByRoleId.forEach { (roleId, assignees) ->
                if (assignees.any { currentUserAuthorities.contains(it) }) {
                    result.add(roleId)
                }
            }
        }
        result.add(RoleConstants.ROLE_EVERYONE)
        currentUserAuthorities.forEach {
            if (it.startsWith(AuthRole.PREFIX)) {
                result.add(it)
            }
        }

        return result.toList()
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
        roleId ?: return false
        if (roleId == RoleConstants.ROLE_EVERYONE) {
            return true
        }
        val currentUserAuthorities = AuthContext.getCurrentUserWithAuthorities()
        if (roleId.startsWith(AuthRole.PREFIX)) {
            return currentUserAuthorities.contains(roleId)
        }
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

        val assigneesCache = getAssigneesCache(record)

        val it = rolesToEval.iterator()
        while (it.hasNext()) {
            val roleId = it.next()
            if (roleId == RoleConstants.ROLE_EVERYONE) {
                // Role 'EVERYONE' should always contain GROUP_EVERYONE
                result[roleId] = arrayListOf(AuthGroup.EVERYONE)
                it.remove()
            } else if (roleId.isBlank() || roleId.startsWith(AuthRole.PREFIX)) {
                result[roleId] = emptyList()
                it.remove()
            } else if (assigneesCache != null) {
                assigneesCache[roleId]?.let { cachedVal ->
                    result[roleId] = cachedVal
                    it.remove()
                }
            }
        }
        if (rolesToEval.isEmpty()) {
            return result
        }

        if (record is EntityRef && record.getAppName().isNotEmpty() && record.getAppName() != currentAppName) {
            val assigneesAtts = rolesToEval.associateWith {
                RoleConstants.ATT_ROLES + "." + RoleConstants.ATT_ASSIGNEES_OF + ".$it[]?str"
            }
            AuthContext.runAsSystem {
                recordsService.getAtts(record, assigneesAtts).forEach { roleId, assignees ->
                    val assigneesStrList = assignees.asStrList()
                    result[roleId] = assigneesStrList
                    assigneesCache?.set(roleId, assigneesStrList)
                }
            }
            return result
        }

        val roleDefById = getRoles(typeRef).associateBy { it.id }

        val rolesDef = rolesToEval.mapNotNull {
            val def = roleDefById[it]
            if (def == null) {
                result[it] = emptyList()
                assigneesCache?.set(it, emptyList())
            }
            def
        }

        AuthContext.runAsSystem {
            val computedAssigneesByRole = computeAssigneesForRoles(record, rolesDef)
            rolesDef.forEach { roleDef ->
                computedAssigneesByRole[roleDef.id]?.let {
                    result[roleDef.id] = it
                    assigneesCache?.set(roleDef.id, it)
                }
            }
        }
        return result
    }

    private fun computeAssigneesForRoles(record: Any?, roles: List<RoleDef>): Map<String, List<String>> {

        val attsToLoad = HashSet<String>()
        val attsForRoles = HashMap<String, MutableSet<String>>()
        val contextAtts = HashMap<String, RecordComputedAttValue>()
        val preCalculatedAssignees = HashMap<String, MutableSet<String>>()

        val uniqueAuthorities = LinkedHashSet<String>()

        var computedAttIdx = 0
        for (role in roles) {
            val attsForRole = attsForRoles.computeIfAbsent(role.id) { HashSet() }
            role.attributes.forEach {
                val att = "$it[]?str"
                attsToLoad.add(att)
                attsForRole.add(att)
            }

            for (authority in role.assignees) {
                if (authority.isNotBlank()) {
                    val trimmedAuth = authority.trim()
                    preCalculatedAssignees.computeIfAbsent(role.id) { LinkedHashSet() }.add(trimmedAuth)
                    uniqueAuthorities.add(trimmedAuth)
                }
            }

            // TODO: Compute [ComputedRoleType.VALUE] and [ComputedRoleType.ATTRIBUTE] explicit,
            //  dont use [RecordComputedAttValue]. Remove toRecordComputedType() extension function
            if (role.computed.type in COMPUTED_ATT_TYPES_TO_CALC_USING_RECORDS_SERVICE) {

                val computedAttValue = RecordComputedAttValue.create()
                    .withType(role.computed.type.toRecordComputedType())
                    .withConfig(role.computed.config)
                    .build()
                val attKey = "__compAttValue_${computedAttIdx++}"
                contextAtts[attKey] = computedAttValue
                val attToLoad = "$$attKey[]?str"
                attsToLoad.add(attToLoad)
                attsForRole.add(attToLoad)
            } else if (role.computed.type == ComputedRoleType.DMN) {

                val decisionRef = role.computed.config["decisionRef"].asText().toEntityRef()
                val rolePreCalc = preCalculatedAssignees.computeIfAbsent(role.id) { LinkedHashSet() }
                computedRoleProcessor.computeRoleAssigneesFromDmn(decisionRef, record).forEach {
                    if (it.isNotBlank()) {
                        val trimmed = it.trim()
                        rolePreCalc.add(trimmed)
                        uniqueAuthorities.add(trimmed)
                    }
                }
            }
        }

        val attributeValues = HashMap<String, List<String>>()
        if (attsToLoad.isNotEmpty()) {
            RequestContext.doWithAtts(contextAtts) { _ ->
                recordsService.getAtts(record, attsToLoad).getAttributes().forEach { att, value ->
                    val authorities = value.asStrList().filter { it.isNotBlank() }.map { it.trim() }
                    uniqueAuthorities.addAll(authorities)
                    attributeValues[att] = authorities
                }
            }
        }

        val authorityNamesMapping = HashMap<String, String>()
        val uniqueAuthoritiesList = uniqueAuthorities.toList()

        val names = authoritiesApi?.getAuthorityNames(uniqueAuthoritiesList)
        if (names != null) {
            if (names.size != uniqueAuthoritiesList.size) {
                error(
                    "Authority component should return list with same length from method getAuthorityNames. " +
                        "Actual names: $names Argument names: $uniqueAuthoritiesList"
                )
            }
            names.forEachIndexed { index, name ->
                val srcVal = uniqueAuthoritiesList[index]
                if (name != srcVal) {
                    authorityNamesMapping[srcVal] = name
                }
            }
        }

        val result = LinkedHashMap<String, List<String>>()
        for (role in roles) {
            val roleAuthorities = LinkedHashSet<String>()
            attsForRoles.getValue(role.id).forEach { attId ->
                attributeValues[attId]?.forEach { authority ->
                    roleAuthorities.add(authorityNamesMapping.getOrDefault(authority, authority))
                }
            }
            preCalculatedAssignees[role.id]?.forEach { assignee ->
                roleAuthorities.add(authorityNamesMapping.getOrDefault(assignee, assignee))
            }
            result[role.id] = roleAuthorities.toList()
        }

        return result
    }

    private fun getAssigneesCache(record: Any?): MutableMap<String, List<String>>? {
        val txn = TxnContext.getTxnOrNull() ?: return null
        if (!txn.isReadOnly()) {
            return null
        }
        val recordRef = getEntityRefForRecord(record)
        if (recordRef.isEmpty()) {
            return null
        }
        return txn.getData(rolesCacheTxnKey) {
            HashMap<EntityRef, MutableMap<String, List<String>>>()
        }.computeIfAbsent(recordRef) { HashMap() }
    }

    private fun getEntityRefForRecord(record: Any?): EntityRef {
        record ?: return EntityRef.EMPTY
        if (record is EntityRef) {
            return record
        }
        return if (record is AttValue) {
            record.id as? EntityRef ?: EntityRef.EMPTY
        } else if (record is AttValueCtx) {
            record.getRef()
        } else if (record is String) {
            val slashIdx = record.indexOf('/')
            val srcIdDelimIdx = record.indexOf('@')
            if (slashIdx in 1 ..< srcIdDelimIdx) {
                EntityRef.valueOf(record)
            } else {
                EntityRef.EMPTY
            }
        } else {
            EntityRef.EMPTY
        }
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

    private fun ComputedRoleType.toRecordComputedType(): RecordComputedAttType {
        return when (this) {
            ComputedRoleType.SCRIPT -> RecordComputedAttType.SCRIPT
            ComputedRoleType.ATTRIBUTE -> RecordComputedAttType.ATTRIBUTE
            ComputedRoleType.VALUE -> RecordComputedAttType.VALUE
            ComputedRoleType.DMN -> RecordComputedAttType.NONE
            ComputedRoleType.NONE -> RecordComputedAttType.NONE
        }
    }
}
