package ru.citeck.ecos.model.lib.permissions.service

import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.permissions.dto.PermissionRule
import ru.citeck.ecos.model.lib.permissions.dto.Permissions
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records2.RecordsServiceFactory
import ru.citeck.ecos.records2.predicate.PredicateUtils

class PermissionsService(recordsServices: RecordsServiceFactory) {

    companion object {

        private const val TYPE_ATT = "_type?id"
        private const val STATUS_ATT = "_status.localId";

        private val DEFAULT_ATTS_TO_LOAD = hashSetOf(STATUS_ATT)
    }

    private val recordsService = recordsServices.recordsService

    fun getPermissions(record: RecordRef, permissions: Permissions) : Map<String, String> {

        val attsToLoad = HashSet(DEFAULT_ATTS_TO_LOAD)
        attsToLoad.addAll(getAttributesToLoad(permissions.rules))

        val recordData = recordsService.getAttributes(record, attsToLoad).attributes
        return getPermissionsImpl(recordData, permissions)
    }

    fun getAttributesPerms(record: RecordRef, attributes: Map<String, Permissions>) : AttributesPerms {

        val result = HashMap<String, Map<String, String>>()

        val attsToLoad = HashSet(DEFAULT_ATTS_TO_LOAD)
        attributes.values.forEach {
            attsToLoad.addAll(getAttributesToLoad(it.rules))
        }

        val recordData = recordsService.getAttributes(record, attsToLoad).attributes
        attributes.forEach { (k, v) -> result[k] = getPermissionsImpl(recordData, v) }

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

    private fun getPermissionsImpl(recordData: ObjectData, permissions: Permissions) : Map<String, String> {
        return emptyMap()
    }
}
