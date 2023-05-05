package ru.citeck.ecos.model.lib.delegation.service

import ru.citeck.ecos.model.lib.delegation.dto.AuthDelegation

class DefaultDelegationService : DelegationService {

    override fun getActiveAuthDelegations(user: String, types: Collection<String>): List<AuthDelegation> {
        return emptyList()
    }
}
