package ru.citeck.ecos.model.lib.permissions.dto

import ecos.com.fasterxml.jackson210.annotation.JsonEnumDefaultValue

enum class PermissionLevel(val permissions: Set<PermissionType>) {

    @JsonEnumDefaultValue
    NONE(emptySet()),
    READ(setOf(PermissionType.READ)),
    WRITE(setOf(PermissionType.READ, PermissionType.WRITE));

    companion object {

        fun getPermissionsFor(level: String): Set<PermissionType> {
            if (level == READ.name) {
                return READ.permissions
            } else if (level == WRITE.name) {
                return WRITE.permissions
            }
            return NONE.permissions
        }
    }

    fun union(level: PermissionLevel?): PermissionLevel {
        level ?: return this
        return if (level.ordinal > this.ordinal) {
            level
        } else {
            this
        }
    }
}
