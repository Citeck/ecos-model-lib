package ru.citeck.ecos.model.lib.permissions.service.roles

import ru.citeck.ecos.model.lib.permissions.dto.PermissionLevel
import ru.citeck.ecos.webapp.api.perms.PermissionType

object ReadPermissions : RolesPermissions {

    override fun isAllowed(roles: Collection<String>, permission: String): Boolean {
        return permission == PermissionType.READ.name
    }

    override fun isAllowed(roles: Collection<String>, permission: PermissionType): Boolean {
        return permission == PermissionType.READ
    }

    override fun isReadAllowed(roles: Collection<String>): Boolean {
        return true
    }

    override fun isWriteAllowed(roles: Collection<String>): Boolean {
        return false
    }

    override fun getPermissions(roles: Collection<String>): Set<String> {
        return PermissionLevel.READ.permissions.map { it.name }.toSet()
    }

    override fun getPermissions(role: String): Set<String> {
        return PermissionLevel.READ.permissions.map { it.name }.toSet()
    }
}
