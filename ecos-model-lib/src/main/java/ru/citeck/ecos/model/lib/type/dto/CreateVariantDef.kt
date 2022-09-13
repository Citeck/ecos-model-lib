package ru.citeck.ecos.model.lib.type.dto

import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import ru.citeck.ecos.webapp.api.entity.EntityRef
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@JsonDeserialize(builder = CreateVariantDef.Builder::class)
@JackJsonDeserialize(builder = CreateVariantDef.Builder::class)
@IncludeNonDefault
data class CreateVariantDef(
    val id: String,
    val name: MLText,
    val sourceId: String,
    val typeRef: EntityRef,
    val formRef: EntityRef,
    val postActionRef: EntityRef,
    val formOptions: ObjectData,
    val attributes: ObjectData,
    val allowedFor: List<String>,
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
        var typeRef: EntityRef = EntityRef.EMPTY
        var formRef: EntityRef = EntityRef.EMPTY
        var postActionRef: EntityRef = EntityRef.EMPTY
        var formOptions: ObjectData = ObjectData.create()
        var attributes: ObjectData = ObjectData.create()
        var allowedFor: List<String> = emptyList()
        var properties: ObjectData = ObjectData.create()

        constructor(base: CreateVariantDef) : this() {
            id = base.id
            name = base.name
            sourceId = base.sourceId
            typeRef = base.typeRef
            formRef = base.formRef
            allowedFor = base.allowedFor
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

        fun withTypeRef(typeRef: EntityRef?): Builder {
            this.typeRef = typeRef ?: EntityRef.EMPTY
            return this
        }

        fun withFormRef(formRef: EntityRef?): Builder {
            this.formRef = formRef ?: EntityRef.EMPTY
            return this
        }

        fun withPostActionRef(postActionRef: EntityRef?): Builder {
            this.postActionRef = postActionRef ?: EntityRef.EMPTY
            return this
        }

        fun withFormOptions(formOptions: ObjectData?): Builder {
            this.formOptions = formOptions ?: ObjectData.create()
            return this
        }

        fun withAllowedFor(allowedFor: List<String>?): Builder {
            this.allowedFor = allowedFor ?: emptyList()
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
                allowedFor,
                properties
            )
        }
    }
}
