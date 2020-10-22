package ru.citeck.ecos.model.lib.permissions.service.roles

import ru.citeck.ecos.model.lib.permissions.dto.PermissionType

interface RolesPermissions {

    fun isAllowed(roles: Collection<String>, permission: String) : Boolean

    fun isAllowed(roles: Collection<String>, permission: PermissionType) : Boolean

    fun isReadAllowed(roles: Collection<String>) : Boolean

    fun isWriteAllowed(roles: Collection<String>) : Boolean
}
