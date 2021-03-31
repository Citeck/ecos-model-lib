package ru.citeck.ecos.model.lib.permissions.service

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.permissions.dto.PermissionsDef
import ru.citeck.ecos.model.lib.permissions.repo.PermissionsRepo
import ru.citeck.ecos.model.lib.permissions.service.roles.AttributePermissions
import ru.citeck.ecos.model.lib.permissions.service.roles.AttributePermissionsImpl
import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissions
import ru.citeck.ecos.model.lib.role.constants.RoleConstants
import ru.citeck.ecos.model.lib.status.constants.StatusConstants
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records2.RecordRef

class RecordPermsService(services: ModelServiceFactory) {

    private val permsRepo: PermissionsRepo = services.permissionsRepo
    private val permsEvaluator: PermsEvaluator = services.permsEvaluator

    private val recordsService = services.records.recordsService
    private val typeRefService = services.typeRefService
    private val typesRepo = services.typesRepo

    fun getRecordPerms(recordRef: RecordRef): RolesPermissions? {

        val context = getPermsEvalContextForRecord(recordRef) ?: return null

        val typePerms: PermissionsDef = typeRefService.forEachAsc(context.typeRef) {
            val permissions = permsRepo.getPermissionsForType(TypeUtils.getTypeRef(it.id))
            if (permissions == null || permissions.permissions.isEmpty()) {
                null
            } else {
                permissions.permissions
            }
        } ?: return null

        return permsEvaluator.getPermissions(recordRef, context.roles, context.statuses, listOf(typePerms))[0]
    }

    fun getRecordAttsPerms(recordRef: RecordRef): AttributePermissions? {

        val context = getPermsEvalContextForRecord(recordRef) ?: return null

        if (context.attributes.isEmpty()) {
            return null
        }

        val typeAttsPerms: Map<String, PermissionsDef> = typeRefService.forEachAsc(context.typeRef) {
            val permissions = permsRepo.getPermissionsForType(TypeUtils.getTypeRef(it.id))
            if (permissions == null || permissions.attributes.isEmpty()) {
                null
            } else {
                permissions.attributes
            }
        } ?: return null

        val permsList = typeAttsPerms.toList().filter { context.attributes.contains(it.first) }
        val rolePerms = permsEvaluator.getPermissions(
            recordRef,
            context.roles,
            context.statuses,
            permsList.map { it.second }
        )

        return AttributePermissionsImpl(
            permsList.mapIndexed {
                idx, perms ->
                Pair(perms.first, rolePerms[idx])
            }.toMap()
        )
    }

    private fun getPermsEvalContextForRecord(recordRef: RecordRef): PermsEvalContext? {
        val typeRefStr = recordsService.getAtt(recordRef, "_type?id").asText()
        return getPermsEvalContextForTypeRef(RecordRef.valueOf(typeRefStr))
    }

    private fun getPermsEvalContextForTypeRef(typeRef: RecordRef): PermsEvalContext? {

        if (RecordRef.isEmpty(typeRef)) {
            return null
        }

        val typeModel = typesRepo.getModel(typeRef)

        return PermsEvalContext(
            typeRef,
            roles = getStrSetOrDefault(typeModel.roles, setOf(RoleConstants.ROLE_EVERYONE)) { it.id },
            statuses = getStrSetOrDefault(typeModel.statuses, setOf(StatusConstants.STATUS_EMPTY)) { it.id },
            attributes = typeModel.attributes.map { it.id }.toSet()
        )
    }

    private inline fun <T> getStrSetOrDefault(
        list: List<T>,
        default: Set<String>,
        getStr: (T) -> String
    ): Set<String> {
        val result = mutableSetOf<String>()
        for (item in list) {
            val str = getStr.invoke(item)
            if (str.isNotBlank()) {
                result.add(str)
            }
        }
        if (result.isEmpty()) {
            result.addAll(default)
        }
        return result
    }

    private data class PermsEvalContext(
        val typeRef: RecordRef,
        val roles: Set<String>,
        val statuses: Set<String>,
        val attributes: Set<String>
    )
}
