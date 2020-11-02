package ru.citeck.ecos.model.lib.permissions.service.roles

class AttributePermissionsImpl(private val permissions: Map<String, RolesPermissions>) : AttributePermissions {

    companion object {
        @JvmField
        val EMPTY = AttributePermissionsImpl(emptyMap())
    }

    override fun getPermissions(attribute: String): RolesPermissions {
        if (permissions.isEmpty()) {
            return ReadWritePermissions
        }
        return permissions[attribute] ?: ReadPermissions
    }
}
