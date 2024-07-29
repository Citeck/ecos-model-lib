package ru.citeck.ecos.model.lib.type.service.utils

import ru.citeck.ecos.model.lib.type.constants.TypeConstants
import ru.citeck.ecos.webapp.api.entity.EntityRef

@Deprecated(
    "Use ModelUtils instead",
    ReplaceWith(
        "ModelUtils",
        "ru.citeck.ecos.model.lib.utils.ModelUtils"
    )
)
object TypeUtils {

    val DOCLIB_DEFAULT_DIR_TYPE = getTypeRef("directory")

    @JvmStatic
    fun getTypeRef(typeId: String): EntityRef {
        if (typeId.isBlank()) {
            return EntityRef.EMPTY
        }
        return EntityRef.create(TypeConstants.TYPE_APP, TypeConstants.TYPE_SOURCE, typeId)
    }
}
