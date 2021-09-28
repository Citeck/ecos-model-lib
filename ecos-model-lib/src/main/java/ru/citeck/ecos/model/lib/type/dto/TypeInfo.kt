package ru.citeck.ecos.model.lib.type.dto

import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.records2.RecordRef

@IncludeNonDefault
@JsonDeserialize(builder = AttributeDef.Builder::class)
data class TypeInfo(
    val id: String,
    val name: MLText,
    val parentRef: RecordRef,
    val dispNameTemplate: MLText,
    val numTemplateRef: RecordRef,
    val model: TypeModelDef
) {
    companion object {

        fun create(): Builder {
            return Builder()
        }

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
        var parentRef: RecordRef = RecordRef.EMPTY
        var dispNameTemplate: MLText = MLText.EMPTY
        var numTemplateRef: RecordRef = RecordRef.EMPTY
        var model: TypeModelDef = TypeModelDef.EMPTY

        constructor(base: TypeInfo) : this() {
            id = base.id
            name = base.name
            parentRef = base.parentRef
            dispNameTemplate = base.dispNameTemplate
            numTemplateRef = base.numTemplateRef
            model = base.model
        }

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withName(name: MLText): Builder {
            this.name = name
            return this
        }

        fun withParentRef(parentRef: RecordRef?): Builder {
            this.parentRef = parentRef ?: RecordRef.EMPTY
            return this
        }

        fun withDispNameTemplate(dispNameTemplate: MLText?): Builder {
            this.dispNameTemplate = dispNameTemplate ?: MLText.EMPTY
            return this
        }

        fun withNumTemplateRef(numTemplateRef: RecordRef?): Builder {
            this.numTemplateRef = numTemplateRef ?: RecordRef.EMPTY
            return this
        }

        fun withModel(model: TypeModelDef?): Builder {
            this.model = model ?: TypeModelDef.EMPTY
            return this
        }

        fun build(): TypeInfo {
            return TypeInfo(
                id,
                name,
                parentRef,
                dispNameTemplate,
                numTemplateRef,
                model
            )
        }
    }
}
