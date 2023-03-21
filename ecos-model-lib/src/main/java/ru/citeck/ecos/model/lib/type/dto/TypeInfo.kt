package ru.citeck.ecos.model.lib.type.dto

import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import ru.citeck.ecos.webapp.api.entity.EntityRef

@IncludeNonDefault
@JsonDeserialize(builder = TypeInfo.Builder::class)
data class TypeInfo(
    val id: String,
    val name: MLText,
    val sourceId: String,
    val parentRef: EntityRef,
    val dispNameTemplate: MLText,
    val numTemplateRef: EntityRef,
    val model: TypeModelDef,
    val contentConfig: TypeContentConfig,
    val aspects: List<TypeAspectDef>,
    val permsPolicy: TypePermsPolicy
) {
    companion object {

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): TypeInfo {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): TypeInfo {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        var id: String = ""
        var name: MLText = MLText.EMPTY
        var sourceId: String = ""
        var parentRef: EntityRef = EntityRef.EMPTY
        var dispNameTemplate: MLText = MLText.EMPTY
        var numTemplateRef: EntityRef = EntityRef.EMPTY
        var model: TypeModelDef = TypeModelDef.EMPTY
        var contentConfig: TypeContentConfig = TypeContentConfig.EMPTY
        var aspects: List<TypeAspectDef> = emptyList()
        var permsPolicy: TypePermsPolicy = TypePermsPolicy.OWN

        constructor(base: TypeInfo) : this() {
            id = base.id
            name = base.name
            sourceId = base.sourceId
            parentRef = base.parentRef
            dispNameTemplate = base.dispNameTemplate
            numTemplateRef = base.numTemplateRef
            model = base.model
            contentConfig = base.contentConfig
            aspects = base.aspects
            permsPolicy = base.permsPolicy
        }

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withName(name: MLText): Builder {
            this.name = name
            return this
        }

        fun withSourceId(sourceId: String): Builder {
            this.sourceId = sourceId
            return this
        }

        fun withParentRef(parentRef: EntityRef?): Builder {
            this.parentRef = parentRef ?: EntityRef.EMPTY
            return this
        }

        fun withDispNameTemplate(dispNameTemplate: MLText?): Builder {
            this.dispNameTemplate = dispNameTemplate ?: MLText.EMPTY
            return this
        }

        fun withNumTemplateRef(numTemplateRef: EntityRef?): Builder {
            this.numTemplateRef = numTemplateRef ?: EntityRef.EMPTY
            return this
        }

        fun withModel(model: TypeModelDef?): Builder {
            this.model = model ?: TypeModelDef.EMPTY
            return this
        }

        fun withContentConfig(contentConfig: TypeContentConfig?): Builder {
            this.contentConfig = contentConfig ?: TypeContentConfig.EMPTY
            return this
        }

        fun withAspects(aspects: List<TypeAspectDef>?): Builder {
            this.aspects = aspects?.filter { it.ref.isNotEmpty() } ?: emptyList()
            return this
        }

        fun withPermsPolicy(permsPolicy: TypePermsPolicy?): Builder {
            this.permsPolicy = permsPolicy ?: TypePermsPolicy.OWN
            return this
        }

        fun build(): TypeInfo {

            val aspects = if (aspects.isEmpty()) {
                emptyList()
            } else {
                val aspectsMap = LinkedHashMap<EntityRef, TypeAspectDef>()
                aspects.forEach { aspectsMap[it.ref] = it }
                aspectsMap.values.toList()
            }

            return TypeInfo(
                id,
                name,
                sourceId,
                parentRef,
                dispNameTemplate,
                numTemplateRef,
                model,
                contentConfig,
                aspects,
                permsPolicy
            )
        }
    }
}
