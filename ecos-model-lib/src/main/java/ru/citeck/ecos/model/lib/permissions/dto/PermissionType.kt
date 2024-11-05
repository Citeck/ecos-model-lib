package ru.citeck.ecos.model.lib.permissions.dto

import ecos.com.fasterxml.jackson210.annotation.JsonEnumDefaultValue

enum class PermissionType {
    @JsonEnumDefaultValue
    READ,
    WRITE
}
