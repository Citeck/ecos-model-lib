package ru.citeck.ecos.model.lib.permissions.service.attributes

import ru.citeck.ecos.model.lib.permissions.service.roles.RolesPermissions

interface AttributesPerms {

    fun getRolesPerms(attribute: String) : RolesPermissions
}
