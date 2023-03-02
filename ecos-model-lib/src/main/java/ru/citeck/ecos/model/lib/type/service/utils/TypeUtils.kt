package ru.citeck.ecos.model.lib.type.service.utils

import ru.citeck.ecos.model.lib.type.constants.TypeConstants
import ru.citeck.ecos.records2.RecordRef

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
    fun getTypeRef(typeId: String): RecordRef {
        if (typeId.isBlank()) {
            return RecordRef.EMPTY
        }
        return RecordRef.create(TypeConstants.TYPE_APP, TypeConstants.TYPE_SOURCE, typeId)
    }
}
