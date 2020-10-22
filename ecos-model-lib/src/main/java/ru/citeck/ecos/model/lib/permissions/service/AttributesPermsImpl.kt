package ru.citeck.ecos.model.lib.permissions.service

import ru.citeck.ecos.model.lib.permissions.dto.PermissionType

class AttributesPermsImpl(private val matrix: Map<String, Map<String, String>>) : AttributesPerms {

    override fun isAllowed(attribute: String, role: String, permission: String) : Boolean {
        return true
    }

    override fun isAllowed(attribute: String, role: String, permission: PermissionType) : Boolean {
        return isAllowed(attribute, role, permission.name)
    }

    override fun isReadAllowed(attribute: String, role: String) : Boolean {
        return isAllowed(attribute, role, PermissionType.READ)
    }

    override fun isWriteAllowed(attribute: String, role: String) : Boolean {
        return isAllowed(attribute, role, PermissionType.WRITE)
    }
}
