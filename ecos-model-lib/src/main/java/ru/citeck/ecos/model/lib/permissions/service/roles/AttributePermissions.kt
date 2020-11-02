package ru.citeck.ecos.model.lib.permissions.service.roles

interface AttributePermissions {

    fun getPermissions(attribute: String): RolesPermissions
}
