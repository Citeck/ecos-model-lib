package ru.citeck.ecos.model.lib.type.dto

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue

enum class WorkspaceScope {
    PUBLIC,
    PRIVATE,
    @JsonEnumDefaultValue
    DEFAULT
}
