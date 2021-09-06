package ru.citeck.ecos.model.lib.role.service.auth

interface AuthorityComponent {

    fun getAuthorityNames(authorities: List<String>): List<String>
}
