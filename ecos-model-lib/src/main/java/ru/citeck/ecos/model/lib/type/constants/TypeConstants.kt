package ru.citeck.ecos.model.lib.type.constants

import ru.citeck.ecos.webapp.api.constants.AppName

object TypeConstants {

    const val TYPE_APP = AppName.EMODEL
    const val TYPE_SOURCE = "type"

    const val ATT_IS_SUBTYPE_OF = "isSubTypeOf"

    /**
     * Prefix of the ids of auto artifacts of a type (journal, form): `type$<type localId>`.
     * The workspace prefix of a workspace type follows it: `type$ws-sys-id:my-type`.
     */
    const val AUTO_ARTIFACT_ID_PREFIX = "type$"
}
