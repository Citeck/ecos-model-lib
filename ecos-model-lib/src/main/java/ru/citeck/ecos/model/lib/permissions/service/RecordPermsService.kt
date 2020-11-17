package ru.citeck.ecos.model.lib.permissions.service

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.permissions.dto.PermissionsDef
import ru.citeck.ecos.model.lib.permissions.repo.PermissionsRepo
import ru.citeck.ecos.model.lib.permissions.service.roles.AttributePermissions
import ru.citeck.ecos.model.lib.permissions.service.roles.AttributePermissionsImpl
import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissions
import ru.citeck.ecos.model.lib.type.constants.TypeConstants
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records2.RecordRef

class RecordPermsService(services: ModelServiceFactory) {

    private val permsRepo: PermissionsRepo = services.permissionsRepo
    private val permsEvaluator: PermsEvaluator = services.permsEvaluator

    private val recordsService = services.records.recordsService
    private val typeDefService = services.typeDefService

    fun getRecordPerms(recordRef: RecordRef): RolesPermissions? {

        val typeRef = RecordRef.valueOf(recordsService.getAtt(recordRef, "_type?id").asText())
        if (RecordRef.isEmpty(typeRef)) {
            return null
        }

        var typePerms: PermissionsDef? = null
        typeDefService.forEachAsc(typeRef) {
            val permissions = permsRepo.getPermissionsForType(TypeUtils.getTypeRef(it.id))
            if (permissions == null || permissions.permissions.isEmpty()) {
                false
            } else {
                typePerms = permissions.permissions
                true
            }
        }
        val notNullTypePerms = typePerms ?: return null

        val typeModel = typeDefService.getModelDef(typeRef)
        val statuses = getListWithDefault(typeModel.statuses.map { it.id }, TypeConstants.STATUS_NONE)
        val roles = getListWithDefault(typeModel.roles.map { it.id }, TypeConstants.ROLE_ALL)

        return permsEvaluator.getPermissions(recordRef, roles, statuses, listOf(notNullTypePerms))[0]
    }

    private fun <T> getListWithDefault(list: List<T>, def: T): List<T> {
        return if (list.isEmpty()) {
            listOf(def)
        } else {
            list
        }
    }

    fun getRecordAttsPerms(recordRef: RecordRef): AttributePermissions? {

        val typeRef = RecordRef.valueOf(recordsService.getAtt(recordRef, "_type?id").asText())
        if (RecordRef.isEmpty(typeRef)) {
            return null
        }

        val typeModel = typeDefService.getModelDef(typeRef)
        val attributes = typeModel.attributes.map { it.id }.toSet()
        if (attributes.isEmpty()) {
            return null
        }

        var typeAttsPerms: Map<String, PermissionsDef>? = null
        typeDefService.forEachAsc(typeRef) {
            val permissions = permsRepo.getPermissionsForType(TypeUtils.getTypeRef(it.id))
            if (permissions == null || permissions.attributes.isEmpty()) {
                false
            } else {
                typeAttsPerms = permissions.attributes
                true
            }
        }

        val notNullAttsPerms = typeAttsPerms ?: return null

        val statuses = getListWithDefault(typeModel.statuses.map { it.id }, TypeConstants.STATUS_NONE)
        val roles = getListWithDefault(typeModel.roles.map { it.id }, TypeConstants.ROLE_ALL)

        val permsList = notNullAttsPerms.toList().filter { attributes.contains(it.first) }
        val rolePerms = permsEvaluator.getPermissions(recordRef, roles, statuses, permsList.map { it.second })

        return AttributePermissionsImpl(
            permsList.mapIndexed {
                idx, perms ->
                Pair(perms.first, rolePerms[idx])
            }.toMap()
        )
    }
}
