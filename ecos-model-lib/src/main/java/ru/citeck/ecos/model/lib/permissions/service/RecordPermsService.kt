package ru.citeck.ecos.model.lib.permissions.service

import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.permissions.dto.PermissionsDef
import ru.citeck.ecos.model.lib.permissions.repo.PermissionsRepo
import ru.citeck.ecos.model.lib.permissions.service.roles.AttributePermissions
import ru.citeck.ecos.model.lib.permissions.service.roles.AttributePermissionsImpl
import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissions
import ru.citeck.ecos.model.lib.role.constants.RoleConstants
import ru.citeck.ecos.model.lib.status.constants.StatusConstants
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.utils.ModelUtils
import ru.citeck.ecos.webapp.api.entity.EntityRef

class RecordPermsService(services: ModelServiceFactory) {

    private val permsRepo: PermissionsRepo = services.permissionsRepo
    private val permsEvaluator: PermsEvaluator = services.permsEvaluator

    private val recordsService = services.records.recordsServiceV1
    private val typeRefService = services.typeRefService
    private val typesRepo = services.typesRepo

    fun getRecordPerms(record: Any?): RolesPermissions? {

        record ?: return null
        val context = getPermsEvalContextForRecord(record) ?: return null

        val typePerms: PermissionsDef = typeRefService.forEachAsc(context.typeRef) {
            val permissions = permsRepo.getPermissionsForType(ModelUtils.getTypeRef(it.getLocalId()))
            if (permissions == null || permissions.permissions.isEmpty()) {
                null
            } else {
                permissions.permissions
            }
        } ?: return null

        return permsEvaluator.getPermissions(record, context.roles, context.statuses, listOf(typePerms))[0]
    }

    fun getRecordAttsPerms(record: Any?): AttributePermissions? {

        record ?: return null
        val context = getPermsEvalContextForRecord(record) ?: return null

        if (context.attributes.isEmpty()) {
            return null
        }

        val typeAttsPerms: Map<String, PermissionsDef> = typeRefService.forEachAsc(context.typeRef) {
            val permissions = permsRepo.getPermissionsForType(ModelUtils.getTypeRef(it.getLocalId()))
            if (permissions == null || permissions.attributes.isEmpty()) {
                null
            } else {
                permissions.attributes
            }
        } ?: return null

        val permsList = typeAttsPerms.toList().filter { context.attributes.contains(it.first) }
        val rolePerms = permsEvaluator.getPermissions(
            record,
            context.roles,
            context.statuses,
            permsList.map { it.second }
        )

        return AttributePermissionsImpl(
            permsList.mapIndexed {
                    idx, perms ->
                Pair(perms.first, rolePerms[idx])
            }.toMap(),
            context.attributes
        )
    }

    private fun getPermsEvalContextForRecord(record: Any): PermsEvalContext? {
        val typeRefStr = recordsService.getAtt(record, "_type?id").asText()
        return getPermsEvalContextForTypeRef(EntityRef.valueOf(typeRefStr))
    }

    private fun getPermsEvalContextForTypeRef(typeRef: EntityRef): PermsEvalContext? {

        if (EntityRef.isEmpty(typeRef)) {
            return null
        }

        val typeModel = typesRepo.getTypeInfo(typeRef)?.model ?: TypeModelDef.EMPTY

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
        val typeRef: EntityRef,
        val roles: Set<String>,
        val statuses: Set<String>,
        val attributes: Set<String>
    )
}
