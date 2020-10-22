package ru.citeck.ecos.model.lib.permissions.service.attributes

import ru.citeck.ecos.model.lib.permissions.service.roles.ReadWritePermissions
import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissions

class AttributesPermsImpl(private val matrix: Map<String, RolesPermissions>) : AttributesPerms {

    override fun getRolesPerms(attribute: String): RolesPermissions {
        return matrix[attribute] ?: ReadWritePermissions
    }
}
