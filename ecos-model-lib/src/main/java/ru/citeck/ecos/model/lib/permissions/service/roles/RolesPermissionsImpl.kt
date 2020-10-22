package ru.citeck.ecos.model.lib.permissions.service.roles

import ru.citeck.ecos.model.lib.permissions.dto.PermissionType

class RolesPermissionsImpl(private val permissions: Map<String, Set<String>>) : RolesPermissions {

    override fun isAllowed(roles: Collection<String>, permission: String): Boolean {
        return roles.any { permissions[it]?.contains(permission) ?: false }
    }

    override fun isAllowed(roles: Collection<String>, permission: PermissionType): Boolean {
        return isAllowed(roles, permission.name)
    }

    override fun isReadAllowed(roles: Collection<String>): Boolean {
        return isAllowed(roles, PermissionType.READ)
    }

    override fun isWriteAllowed(roles: Collection<String>): Boolean {
        return isAllowed(roles, PermissionType.WRITE)
    }
}
