package ru.citeck.ecos.model.lib.permissions.service

import ru.citeck.ecos.model.lib.permissions.dto.PermissionType

interface AttributesPerms {

    fun isAllowed(attribute: String, role: String, permission: String) : Boolean

    fun isAllowed(attribute: String, role: String, permission: PermissionType) : Boolean

    fun isReadAllowed(attribute: String, role: String) : Boolean

    fun isWriteAllowed(attribute: String, role: String) : Boolean
}
