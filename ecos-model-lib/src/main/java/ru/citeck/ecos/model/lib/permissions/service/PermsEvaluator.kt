package ru.citeck.ecos.model.lib.permissions.service

import mu.KotlinLogging
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.permissions.dto.*
import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissions
import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissionsImpl
import ru.citeck.ecos.model.lib.status.constants.StatusAtts
import ru.citeck.ecos.records2.RecordMeta
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records2.predicate.PredicateUtils
import ru.citeck.ecos.records2.predicate.RecordElement

class PermsEvaluator(services: ModelServiceFactory) {

    companion object {
        val log = KotlinLogging.logger {}
    }

    private val recordsService = services.recordsServices.recordsServiceV1
    private val predicateService = services.recordsServices.predicateService

    fun getPermissions(
        recordRef: RecordRef,
        roles: List<String>,
        statuses: List<String>,
        permissions: PermissionsDef
    ): RolesPermissions {

        return getPermissions(recordRef, roles, statuses, listOf(permissions))[0]
    }

    fun getPermissions(
        recordRef: RecordRef,
        roles: List<String>,
        statuses: List<String>,
        permissions: List<PermissionsDef>
    ): List<RolesPermissions> {

        val attsToLoad = permissions.flatMap { getAttributesToLoad(it.rules) }.toMutableList()
        attsToLoad.add(StatusAtts.STATUS_STR)

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
        roles: List<String>,
        statuses: List<String>,
        permissions: PermissionsDef
    ): Map<String, Set<String>> {

        val permissionsByRole = HashMap<String, MutableSet<String>>()
        val status = recordData.get(StatusAtts.STATUS_STR).asText()

        if (!statuses.contains(status)) {
            for (role in roles) {
                permissionsByRole[role] = mutableSetOf()
            }
            return permissionsByRole
        }

        for (role in roles) {
            val level = permissions.matrix[role]?.get(status) ?: PermissionLevel.READ
            permissionsByRole[role] = level.permissions.map { it.name }.toHashSet()
        }

        permissions.rules.filter {
            it.statuses.isEmpty() || it.statuses.contains(status)
        }.filter {
            predicateService.isMatch(RecordElement(RecordMeta(RecordRef.EMPTY, recordData)), it.condition)
        }.forEach { rule ->
            rule.roles.forEach { role ->
                val rolePermissions = permissionsByRole.computeIfAbsent(role) { HashSet() }
                when (rule.type) {
                    RuleType.ALLOW -> {
                        rule.permissions.forEach { permission ->
                            rolePermissions.addAll(PermissionLevel.getPermissionsFor(permission).map { it.name })
                        }
                    }
                    RuleType.REVOKE -> {
                        rolePermissions.removeAll(rule.permissions)
                    }
                }
            }
        }
        return permissionsByRole
    }
}
