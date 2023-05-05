package ru.citeck.ecos.model.lib.delegation.dto

data class AuthDelegation(
    val delegator: String,
    val delegatedTypes: Set<String>,
    val delegatedAuthorities: Set<String>
)
