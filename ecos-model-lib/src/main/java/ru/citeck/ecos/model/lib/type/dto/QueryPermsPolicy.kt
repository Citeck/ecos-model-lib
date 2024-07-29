package ru.citeck.ecos.model.lib.type.dto

enum class QueryPermsPolicy {
    /**
     * Inherit policy from parent
     */
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
