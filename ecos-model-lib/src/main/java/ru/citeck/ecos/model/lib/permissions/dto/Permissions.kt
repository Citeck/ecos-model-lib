package ru.citeck.ecos.model.lib.permissions.dto

data class Permissions(
        /**
     * <Role></Role>, <Status></Status>, PermissionType>>
     */
    val matrix: Map<String, Map<String, PermissionType>> = emptyMap(),
        val rules: List<PermissionRule> = emptyList()
)
