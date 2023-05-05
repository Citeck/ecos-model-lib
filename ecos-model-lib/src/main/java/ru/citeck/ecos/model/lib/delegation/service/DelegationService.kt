package ru.citeck.ecos.model.lib.delegation.service

import ru.citeck.ecos.model.lib.delegation.dto.AuthDelegation

interface DelegationService {

    fun getActiveAuthDelegations(user: String, types: Collection<String>): List<AuthDelegation>
}
