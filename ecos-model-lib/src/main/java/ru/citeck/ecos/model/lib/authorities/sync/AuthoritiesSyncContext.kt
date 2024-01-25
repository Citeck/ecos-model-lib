package ru.citeck.ecos.model.lib.authorities.sync

import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.authorities.AuthorityType

interface AuthoritiesSyncContext<T> {

    fun setState(state: T?)

    fun updateAuthorities(type: AuthorityType, authorities: List<ObjectData>)

    fun deleteAuthorities(type: AuthorityType, authorities: List<String>)
}
