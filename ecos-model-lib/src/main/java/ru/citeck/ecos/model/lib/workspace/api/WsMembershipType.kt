package ru.citeck.ecos.model.lib.workspace.api

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue

/**
 * Defines how to interpret user membership in a workspace.
 *
 * If a user is both directly added to a workspace and included via a group,
 * the membership is considered INDIRECT for the purposes of filtering.
 */
enum class WsMembershipType {

    /**
     * The user is directly added to the workspace (not through a group).
     */
    DIRECT,

    /**
     * The user is a member of the workspace via a group (not directly).
     */
    INDIRECT,

    /**
     * The user is a member either directly or via a group.
     */
    @JsonEnumDefaultValue
    ALL
}
