package ru.citeck.ecos.model.lib.authorities

import ru.citeck.ecos.context.lib.auth.AuthGroup
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.webapp.api.constants.AppName

enum class AuthorityType(val sourceId: String, val authorityPrefix: String) {

    PERSON("person", ""),
    GROUP("authority-group", AuthGroup.PREFIX);

    fun getRef(id: String): RecordRef {
        return RecordRef.create(AppName.EMODEL, sourceId, id)
    }
}
