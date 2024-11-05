package ru.citeck.ecos.model.lib.type.dto

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue

enum class QueryPermsPolicy {
    /**
     * Inherit policy from parent
     */
    @JsonEnumDefaultValue
    DEFAULT,

    /**
     * Check own permissions in query
     */
    OWN,

    /**
     * Check parent permissions in query
     */
    PARENT,

    /**
     * Everyone can query entities without permissions check
     */
    PUBLIC,

    /**
     * Ability to query is disabled
     */
    NONE
}
