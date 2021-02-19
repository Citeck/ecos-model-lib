package ru.citeck.ecos.model.lib.type.dto

import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import ru.citeck.ecos.records2.RecordRef
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@JsonDeserialize(builder = TypeDef.Builder::class)
@JackJsonDeserialize(builder = TypeDef.Builder::class)
@IncludeNonDefault
data class TypeDef(
    val id: String,
    val name: MLText,
    val parentRef: RecordRef,
    val model: TypeModelDef,
    val docLib: DocLibDef,
    val numTemplateRef: RecordRef,
    val inheritNumTemplate: Boolean,
    val createVariants: List<CreateVariantDef>,
    val properties: ObjectData
) {

    companion object {

        @JvmField
        val EMPTY = create {}

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): TypeDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): TypeDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        var id: String = ""
        var name: MLText = MLText()
        var parentRef: RecordRef? = RecordRef.EMPTY
        var model: TypeModelDef = TypeModelDef.EMPTY
        var docLib: DocLibDef = DocLibDef.EMPTY

        var numTemplateRef: RecordRef = RecordRef.EMPTY
        var inheritNumTemplate: Boolean = true

        var properties: ObjectData = ObjectData.create()

        var createVariants: List<CreateVariantDef> = emptyList()

        constructor(base: TypeDef) : this() {
            this.id = base.id
            this.parentRef = base.parentRef
            this.model = base.model
            this.docLib = base.docLib
            this.properties = ObjectData.deepCopyOrNew(base.properties);
            this.numTemplateRef = base.numTemplateRef
            this.inheritNumTemplate = base.inheritNumTemplate
            this.createVariants = base.createVariants.toList()
        }

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withName(name: MLText?): Builder {
            this.name = name ?: MLText()
            return this
        }

        fun withParentRef(parentRef: RecordRef?): Builder {
            this.parentRef = parentRef ?: RecordRef.EMPTY
            return this
        }

        fun withModel(model: TypeModelDef?): Builder {
            this.model = model ?: TypeModelDef.EMPTY
            return this
        }

        fun withDocLib(docLib: DocLibDef?): Builder {
            this.docLib = docLib ?: DocLibDef.EMPTY
            return this
        }

        fun withNumTemplateRef(numTemplateRef: RecordRef?): Builder {
            this.numTemplateRef = numTemplateRef ?: RecordRef.EMPTY
            return this
        }

        fun withInheritNumTemplate(inheritNumTemplate: Boolean?): Builder {
            this.inheritNumTemplate = inheritNumTemplate ?: true
            return this
        }

        fun withCreateVariants(createVariants: List<CreateVariantDef>?): Builder {
            this.createVariants = createVariants ?: emptyList()
            return this
        }

        fun withProperties(properties: ObjectData?) {
            this.properties = properties ?: ObjectData.create()
        }

        fun build(): TypeDef {
            return TypeDef(
                id,
                name,
                parentRef ?: RecordRef.EMPTY,
                model,
                docLib,
                numTemplateRef,
                inheritNumTemplate,
                createVariants,
                properties
            )
        }
    }
}
