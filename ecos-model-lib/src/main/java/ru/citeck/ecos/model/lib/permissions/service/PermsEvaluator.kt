package ru.citeck.ecos.model.lib.permissions.service

import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.permissions.dto.*
import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissions
import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissionsImpl
import ru.citeck.ecos.model.lib.status.constants.StatusConstants
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records2.predicate.PredicateUtils

class PermsEvaluator(services: ModelServiceFactory) {

    private val recordsService = services.records.recordsServiceV1
    private val predicateService = services.records.predicateService

    fun getPermissions(
        recordRef: RecordRef,
        roles: Collection<String>,
        statuses: Collection<String>,
        permissions: PermissionsDef
    ): RolesPermissions {

        return getPermissions(recordRef, roles, statuses, listOf(permissions))[0]
    }

    fun getPermissions(
        recordRef: RecordRef,
        roles: Collection<String>,
        statuses: Collection<String>,
        permissions: List<PermissionsDef>
    ): List<RolesPermissions> {

        val attsToLoad = permissions.flatMap { getAttributesToLoad(it.rules) }.toMutableList()
        attsToLoad.add(StatusConstants.ATT_STATUS_STR)

        val recordData = recordsService.getAtts(recordRef, attsToLoad).getAtts()
        return permissions.map { RolesPermissionsImpl(getPermissionsImpl(recordData, roles, statuses, it)) }
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
        recordData: ObjectData,
        roles: Collection<String>,
        statuses: Collection<String>,
        permissions: PermissionsDef
    ): Map<String, Set<String>> {

        val permissionsByRole = HashMap<String, MutableSet<String>>()
        val status = getStatusFromData(recordData)

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

        permissions.rules.filter {
            it.statuses.isEmpty() || it.statuses.contains(status)
        }.filter {
            predicateService.isMatch(recordData, it.condition)
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
        val status = data.get(StatusConstants.ATT_STATUS_STR).asText()
        if (status.isBlank()) {
            return StatusConstants.STATUS_EMPTY
        }
        return status
    }
}
