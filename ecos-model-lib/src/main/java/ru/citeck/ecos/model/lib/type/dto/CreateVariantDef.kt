package ru.citeck.ecos.model.lib.type.dto

import ecos.com.fasterxml.jackson210.annotation.JsonInclude
import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.records2.RecordRef
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@JsonDeserialize(builder = CreateVariantDef.Builder::class)
@JackJsonDeserialize(builder = CreateVariantDef.Builder::class)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
data class CreateVariantDef(
    val id: String,
    val name: MLText,
    val sourceId: String,
    val typeRef: RecordRef,
    val formRef: RecordRef,
    val postActionRef: RecordRef,
    val formOptions: ObjectData,
    val attributes: ObjectData,
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
        fun create(builder: Builder.() -> Unit): CreateVariantDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): CreateVariantDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        var id: String = ""
        var name: MLText = MLText.EMPTY
        var sourceId: String = ""
        var typeRef: RecordRef = RecordRef.EMPTY
        var formRef: RecordRef = RecordRef.EMPTY
        var postActionRef: RecordRef = RecordRef.EMPTY
        var formOptions: ObjectData = ObjectData.create()
        var attributes: ObjectData = ObjectData.create()
        var properties: ObjectData = ObjectData.create()

        constructor(base: CreateVariantDef) : this() {
            id = base.id
            name = base.name
            sourceId = base.sourceId
            typeRef = base.typeRef
            formRef = base.formRef
            postActionRef = base.postActionRef
            formOptions = ObjectData.deepCopyOrNew(base.formOptions)
            attributes = ObjectData.deepCopyOrNew(base.attributes)
            properties = ObjectData.deepCopyOrNew(base.properties)
        }

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withName(name: MLText?): Builder {
            this.name = name ?: MLText.EMPTY
            return this
        }

        fun withSourceId(sourceId: String?): Builder {
            this.sourceId = sourceId ?: ""
            return this
        }

        fun withTypeRef(typeRef: RecordRef?): Builder {
            this.typeRef = typeRef ?: RecordRef.EMPTY
            return this
        }

        fun withFormRef(formRef: RecordRef?): Builder {
            this.formRef = formRef ?: RecordRef.EMPTY
            return this
        }

        fun withPostActionRef(postActionRef: RecordRef?): Builder {
            this.postActionRef = postActionRef ?: RecordRef.EMPTY
            return this
        }

        fun withFormOptions(formOptions: ObjectData?): Builder {
            this.formOptions = formOptions ?: ObjectData.create()
            return this
        }

        fun withAttributes(attributes: ObjectData?): Builder {
            this.attributes = attributes ?: ObjectData.create()
            return this
        }

        fun withProperties(properties: ObjectData?): Builder {
            this.properties = properties ?: ObjectData.create()
            return this
        }

        fun build(): CreateVariantDef {
            return CreateVariantDef(
                id,
                name,
                sourceId,
                typeRef,
                formRef,
                postActionRef,
                formOptions,
                attributes,
                properties
            )
        }
    }
}
