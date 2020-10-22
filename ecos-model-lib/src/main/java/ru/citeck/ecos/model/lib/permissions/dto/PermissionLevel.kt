package ru.citeck.ecos.model.lib.permissions.dto

enum class PermissionLevel(val permissions: Set<PermissionType>) {

    NONE(emptySet()),
    READ(setOf(PermissionType.READ)),
    WRITE(setOf(PermissionType.READ, PermissionType.WRITE));

    companion object {

        fun getPermissionsFor(level: String) : Set<PermissionType> {
            if (level == READ.name) {
                return READ.permissions
            } else if (level == WRITE.name) {
                return WRITE.permissions
            }
            return NONE.permissions
        }
    }
}
