package ru.citeck.ecos.model.lib.role.service.auth

class DefaultAuthorityComponent : AuthorityComponent {

    override fun getAuthorityNames(authorities: List<String>): List<String> {
        return authorities
    }
}
