package ru.citeck.ecos.model.lib.permissions.service.roles

import ru.citeck.ecos.model.lib.permissions.dto.PermissionLevel
import ru.citeck.ecos.webapp.api.perms.PermissionType

object ReadWritePermissions : RolesPermissions {

    override fun isAllowed(roles: Collection<String>, permission: String): Boolean {
        return permission == PermissionType.READ.name || permission == PermissionType.WRITE.name
    }

    override fun isAllowed(roles: Collection<String>, permission: PermissionType): Boolean {
        return permission == PermissionType.READ || permission == PermissionType.WRITE
    }

    override fun isReadAllowed(roles: Collection<String>): Boolean {
        return true
    }

    override fun isWriteAllowed(roles: Collection<String>): Boolean {
        return true
    }

    override fun getPermissions(roles: Collection<String>): Set<String> {
        return PermissionLevel.WRITE.permissions.map { it.name }.toSet()
    }

    override fun getPermissions(role: String): Set<String> {
        return PermissionLevel.WRITE.permissions.map { it.name }.toSet()
    }
}
