package ru.citeck.ecos.model.lib.permissions.service

import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.EcosModelServiceFactory
import ru.citeck.ecos.model.lib.permissions.dto.PermissionLevel
import ru.citeck.ecos.model.lib.permissions.dto.PermissionRule
import ru.citeck.ecos.model.lib.permissions.dto.PermissionType
import ru.citeck.ecos.model.lib.permissions.dto.PermissionsDef
import ru.citeck.ecos.model.lib.permissions.service.attributes.AttributesPerms
import ru.citeck.ecos.model.lib.permissions.service.attributes.AttributesPermsImpl
import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissions
import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissionsImpl
import ru.citeck.ecos.model.lib.status.constants.StatusAtts
import ru.citeck.ecos.records2.RecordMeta
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records2.predicate.PredicateUtils
import ru.citeck.ecos.records2.predicate.RecordElement

class PermissionsService(services: EcosModelServiceFactory) {

    companion object {

        private const val ATT_TYPE = "_type?id"

        private const val ATT_TYPE_ROLES = "_type.roles[].id?str"
        private const val ATT_TYPE_STATUSES = "_type.statuses[].id?str"

        private val DEFAULT_ATTS_TO_LOAD = hashSetOf(
            StatusAtts.STATUS,
            ATT_TYPE_ROLES,
            ATT_TYPE_STATUSES
        )
    }

    private val recordsService = services.recordsServices.recordsService
    private val predicateService = services.recordsServices.predicateService

    fun getPermissions(record: RecordRef, permissions: PermissionsDef) : RolesPermissions {

        val attsToLoad = HashSet(DEFAULT_ATTS_TO_LOAD)
        attsToLoad.addAll(getAttributesToLoad(permissions.rules))

        val recordData = recordsService.getAttributes(record, attsToLoad).attributes
        return RolesPermissionsImpl(getPermissionsImpl(recordData, permissions))
    }

    fun getAttributesPerms(record: RecordRef, attributes: Map<String, PermissionsDef>) : AttributesPerms {

        val result = HashMap<String, RolesPermissions>()

        val attsToLoad = HashSet(DEFAULT_ATTS_TO_LOAD)
        attributes.values.forEach {
            attsToLoad.addAll(getAttributesToLoad(it.rules))
        }

        val recordData = recordsService.getAttributes(record, attsToLoad).attributes
        attributes.forEach { (k, v) -> result[k] = RolesPermissionsImpl(getPermissionsImpl(recordData, v)) }

        return AttributesPermsImpl(result)
    }

    private fun getAttributesToLoad(rules: List<PermissionRule>) : Set<String> {
        if (rules.isEmpty()) {
            return emptySet()
        }
        val attributesToLoad = hashSetOf<String>()
        for (rule in rules) {
            attributesToLoad.addAll(PredicateUtils.getAllPredicateAttributes(rule.condition))
        }
        return attributesToLoad
    }

    private fun getPermissionsImpl(recordData: ObjectData, permissions: PermissionsDef) : Map<String, Set<String>> {

        val permissionsByRole = HashMap<String, MutableSet<String>>()

        val roles = HashSet(permissions.matrix.keys)
        recordData.get(ATT_TYPE_ROLES).forEach { roles.add(it.asText()) }

        val status = recordData.get(StatusAtts.STATUS).asText()
        if (status.isNotBlank()) {
            for (role in roles) {
                val level = permissions.matrix[role]?.get(status) ?: PermissionLevel.READ
                permissionsByRole[role] = level.permissions.map { it.name }.toHashSet()
            }
        } else {
            roles.forEach { permissionsByRole[it] = hashSetOf(PermissionType.READ.name) }
        }

        permissions.rules.filter {
            it.statuses.isEmpty() || it.statuses.contains(status)
        }.filter {
            predicateService.isMatch(RecordElement(RecordMeta(RecordRef.EMPTY, recordData)), it.condition)
        }.forEach { rule ->
            rule.roles.forEach { role ->
                val rolePermissions = permissionsByRole.computeIfAbsent(role) { HashSet() }
                if (rule.allow) {
                    rule.permissions.forEach { permission ->
                        rolePermissions.addAll(PermissionLevel.getPermissionsFor(permission).map { it.name })
                    }
                } else {
                    rolePermissions.removeAll(rule.permissions)
                }
            }
        }

        return permissionsByRole
    }
}
