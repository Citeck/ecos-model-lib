package ru.citeck.ecos.model.lib.delegation.api

import ru.citeck.ecos.model.lib.delegation.dto.AuthDelegation

interface DelegationApi {
    fun getActiveAuthDelegations(user: String, types: Collection<String>): List<AuthDelegation>
}
