package ru.citeck.ecos.model.lib.authorities.sync

import ru.citeck.ecos.model.lib.authorities.AuthorityType

interface AuthoritiesSyncFactory<C : Any, S : Any> {

    fun createSync(
        config: C,
        authorityType: AuthorityType,
        context: AuthoritiesSyncContext<S>
    ): AuthoritiesSync<S>

    fun getType(): String
}
