package ru.citeck.ecos.model.lib.permissions.service.roles

class AttributePermissionsImpl(
    private val permissions: Map<String, RolesPermissions>,
    private val modelAttributes: Set<String>
) : AttributePermissions {

    companion object {
        @JvmField
        val EMPTY = AttributePermissionsImpl(emptyMap(), emptySet())
    }

    override fun getPermissions(attribute: String): RolesPermissions {
        if (permissions.isEmpty()) {
            return ReadWritePermissions
        }
        var result = permissions[attribute]
        if (result == null) {
            result = if (modelAttributes.contains(attribute)) {
                ReadPermissions
            } else {
                ReadWritePermissions
            }
        }
        return result
    }
}
