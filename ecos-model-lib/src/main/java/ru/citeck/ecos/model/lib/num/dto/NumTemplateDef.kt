package ru.citeck.ecos.model.lib.num.dto

import ecos.com.fasterxml.jackson210.annotation.JsonInclude
import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.DataValue
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@JsonDeserialize(builder = NumTemplateDef.Builder::class)
@JackJsonDeserialize(builder = NumTemplateDef.Builder::class)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
data class NumTemplateDef(
    val id: String,
    val name: String,
    val counterKey: String,
    val modelAttributes: List<String>
) {

    companion object {

        fun create(): Builder {
            return Builder()
        }

        fun create(builder: Builder.() -> Unit): NumTemplateDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): NumTemplateDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        var id: String = ""
        var name: String = ""
        var counterKey: String = ""
        var modelAttributes: List<String> = emptyList()

        constructor(base: NumTemplateDef) : this() {
            id = base.id
            name = base.name
            counterKey = base.counterKey
            modelAttributes = DataValue.create(base.modelAttributes).asStrList()
        }

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withName(name: String): Builder {
            this.name = name
            return this
        }

        fun withCounterKey(counterKey: String): Builder {
            this.counterKey = counterKey
            return this
        }

        fun withModelAttributes(modelAttributes: List<String>): Builder {
            this.modelAttributes = ArrayList(modelAttributes)
            return this
        }

        fun build(): NumTemplateDef {
            return NumTemplateDef(id, name, counterKey, modelAttributes)
        }
    }
}
