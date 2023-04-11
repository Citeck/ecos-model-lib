package ru.citeck.ecos.model.lib.permissions.service

import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.permissions.dto.*
import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissions
import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissionsImpl
import ru.citeck.ecos.model.lib.status.constants.StatusConstants
import ru.citeck.ecos.records2.predicate.PredicateUtils
import ru.citeck.ecos.records2.predicate.element.elematts.RecordAttsElement
import ru.citeck.ecos.records3.record.atts.dto.RecordAtts

class PermsEvaluator(services: ModelServiceFactory) {

    private val recordsService = services.records.recordsServiceV1
    private val predicateService = services.records.predicateService

    fun getPermissions(
        record: Any,
        roles: Collection<String>,
        statuses: Collection<String>,
        permissions: PermissionsDef
    ): RolesPermissions {

        return getPermissions(record, roles, statuses, listOf(permissions))[0]
    }

    fun getPermissions(
        record: Any,
        roles: Collection<String>,
        statuses: Collection<String>,
        permissions: List<PermissionsDef>
    ): List<RolesPermissions> {

        val attsToLoad = permissions.flatMap { getAttributesToLoad(it.rules) }.toMutableList()
        attsToLoad.add(StatusConstants.ATT_STATUS_STR)

        val recordAtts = recordsService.getAtts(record, attsToLoad)
        return permissions.map { RolesPermissionsImpl(getPermissionsImpl(recordAtts, roles, statuses, it)) }
    }

    private fun getAttributesToLoad(rules: List<PermissionRule>): Set<String> {
        if (rules.isEmpty()) {
            return emptySet()
        }
        val attributesToLoad = hashSetOf<String>()
        for (rule in rules) {
            attributesToLoad.addAll(PredicateUtils.getAllPredicateAttributes(rule.condition))
        }
        return attributesToLoad
    }

    private fun getPermissionsImpl(
        recordData: RecordAtts,
        roles: Collection<String>,
        statuses: Collection<String>,
        permissions: PermissionsDef
    ): Map<String, Set<String>> {

        val permissionsByRole = HashMap<String, MutableSet<String>>()
        val status = getStatusFromData(recordData.getAtts())

        if (!statuses.contains(status)) {
            if (status == StatusConstants.STATUS_EMPTY) {
                for (role in roles) {
                    permissionsByRole[role] = PermissionLevel.READ.permissions.map { it.name }.toHashSet()
                }
            } else {
                for (role in roles) {
                    permissionsByRole[role] = hashSetOf()
                }
            }
        } else {
            for (role in roles) {
                val rolePerms = permissions.matrix[role]
                val level = if (rolePerms != null) {
                    val statusPerms = rolePerms[status] ?: PermissionLevel.READ // status perms is not set
                    statusPerms.union(rolePerms[StatusConstants.STATUS_ANY])
                } else {
                    // role permissions is not set
                    PermissionLevel.READ
                }
                permissionsByRole[role] = level.permissions.map { it.name }.toHashSet()
            }
        }

        val recordElement = RecordAttsElement(recordData, recordData)
        permissions.rules.filter {
            it.statuses.isEmpty() || it.statuses.contains(status)
        }.filter {
            predicateService.isMatch(recordElement, it.condition)
        }.forEach { rule ->
            rule.roles.forEach { role ->
                val rolePermissions = permissionsByRole.computeIfAbsent(role) { HashSet() }
                when (rule.type) {
                    RuleType.ALLOW -> {
                        rolePermissions.addAll(rule.permissions)
                    }
                    RuleType.REVOKE -> {
                        rolePermissions.removeAll(rule.permissions)
                    }
                }
            }
        }
        return permissionsByRole
    }

    private fun getStatusFromData(data: ObjectData): String {
        val status = data[StatusConstants.ATT_STATUS_STR].asText()
        if (status.isBlank()) {
            return StatusConstants.STATUS_EMPTY
        }
        return status
    }
}
