package ru.citeck.ecos.model.lib.type.service.utils

import ru.citeck.ecos.model.lib.type.constants.TypeConstants
import ru.citeck.ecos.records2.RecordRef

object TypeUtils {

    val DOCLIB_DEFAULT_DIR_TYPE = getTypeRef("directory")

    @JvmStatic
    fun getTypeRef(typeId: String): RecordRef {
        return RecordRef.create(TypeConstants.TYPE_APP, TypeConstants.TYPE_SOURCE, typeId)
    }
}
