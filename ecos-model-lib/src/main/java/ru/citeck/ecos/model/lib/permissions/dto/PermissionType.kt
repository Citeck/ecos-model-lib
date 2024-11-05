package ru.citeck.ecos.model.lib.permissions.dto

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue

enum class PermissionType {
    @JsonEnumDefaultValue
    READ,
    WRITE
}
