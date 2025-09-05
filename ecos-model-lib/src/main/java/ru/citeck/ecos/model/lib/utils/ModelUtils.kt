package ru.citeck.ecos.model.lib.utils

import ru.citeck.ecos.model.lib.aspect.constants.AspectConstants
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.model.lib.type.constants.TypeConstants
import ru.citeck.ecos.webapp.api.entity.EntityRef

object ModelUtils {

    const val DEFAULT_WORKSPACE_ID = "default"
    val DOCLIB_DEFAULT_DIR_TYPE = getTypeRef("directory")

    @JvmStatic
    fun getTypeRef(typeId: String?): EntityRef {
        if (typeId.isNullOrBlank()) {
            return EntityRef.EMPTY
        }
        return EntityRef.create(TypeConstants.TYPE_APP, TypeConstants.TYPE_SOURCE, typeId)
    }

    @JvmStatic
    fun getAspectRef(aspectId: String?): EntityRef {
        if (aspectId.isNullOrBlank()) {
            return EntityRef.EMPTY
        }
        return EntityRef.create(AspectConstants.ASPECT_APP, AspectConstants.ASPECT_SOURCE, aspectId)
    }

    fun getMergedModelAtts(
        attributes: List<AttributeDef>,
        systemAttributes: List<AttributeDef>
    ): Pair<List<AttributeDef>, List<AttributeDef>> {

        val mergedSystemAtts = mergeElementsById(systemAttributes) { it.id }
        val systemAttIdsSet = mergedSystemAtts.map { it.id }.toSet()

        val mergedAtts = mergeElementsById(attributes, {
            !systemAttIdsSet.contains(it.id)
        }) {
            it.id
        }

        return mergedAtts to mergedSystemAtts
    }

    inline fun <T> mergeElementsById(
        elements: List<T>,
        crossinline getId: (T) -> String
    ): List<T> {
        return mergeElementsById(elements, { true }, getId)
    }

    inline fun <T> mergeElementsById(
        elements: List<T>,
        filter: (T) -> Boolean,
        getId: (T) -> String
    ): List<T> {
        val result = LinkedHashMap<String, T>()
        for (element in elements) {
            val id = getId(element)
            if (id.isNotBlank()) {
                if (filter(element)) {
                    result[id] = element
                }
            }
        }
        return result.values.toList()
    }
}
