package ru.citeck.ecos.model.lib.type.dto

import ecos.com.fasterxml.jackson210.annotation.JsonInclude
import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.records2.RecordRef
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@JsonDeserialize(builder = TypeDef.Builder::class)
@JackJsonDeserialize(builder = TypeDef.Builder::class)
@JsonInclude(value = JsonInclude.Include.NON_DEFAULT)
data class TypeDef(
    val id: String,
    val parentRef: RecordRef?,
    val model: TypeModelDef,
    val docLib: DocLibDef,
    val numTemplateRef: RecordRef?,
    val inheritNumTemplate: Boolean
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
        var parentRef: RecordRef? = null
        var model: TypeModelDef = TypeModelDef.EMPTY
        var docLib: DocLibDef = DocLibDef.EMPTY

        var numTemplateRef: RecordRef? = null
        var inheritNumTemplate: Boolean = true

        constructor(base: TypeDef) : this() {
            this.id = base.id
            this.parentRef = base.parentRef
            this.model = base.model
            this.docLib = base.docLib
            this.numTemplateRef = base.numTemplateRef
            this.inheritNumTemplate = base.inheritNumTemplate
        }

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withParentRef(parentRef: RecordRef?): Builder {
            this.parentRef = parentRef
            return this
        }

        fun withModel(model: TypeModelDef): Builder {
            this.model = model
            return this
        }

        fun withDocLib(docLib: DocLibDef): Builder {
            this.docLib = docLib
            return this
        }

        fun withNumTemplateRef(numTemplateRef: RecordRef?): Builder {
            this.numTemplateRef = numTemplateRef
            return this
        }

        fun withInheritNumTemplate(inheritNumTemplate: Boolean): Builder {
            this.inheritNumTemplate = inheritNumTemplate
            return this
        }

        fun build(): TypeDef {
            return TypeDef(id, parentRef, model, docLib, numTemplateRef, inheritNumTemplate)
        }
    }
}
