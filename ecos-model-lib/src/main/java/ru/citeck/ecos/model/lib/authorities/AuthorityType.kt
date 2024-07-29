package ru.citeck.ecos.model.lib.authorities

import ru.citeck.ecos.context.lib.auth.AuthGroup
import ru.citeck.ecos.webapp.api.constants.AppName
import ru.citeck.ecos.webapp.api.entity.EntityRef

enum class AuthorityType(val sourceId: String, val authorityPrefix: String) {

    PERSON("person", ""),
    GROUP("authority-group", AuthGroup.PREFIX);

    fun getRef(id: String): EntityRef {
        return EntityRef.create(AppName.EMODEL, sourceId, id)
    }
}
