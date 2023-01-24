package ru.citeck.ecos.model.lib.attributes.dto

import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@IncludeNonDefault
@JsonDeserialize(builder = AttIndexDef.Builder::class)
@JackJsonDeserialize(builder = AttIndexDef.Builder::class)
data class AttIndexDef(
    val enabled: Boolean
) {

    companion object {

        val EMPTY = create {}

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): AttIndexDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): AttIndexDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        var enabled: Boolean = false

        constructor(enabled: Boolean) : this() {
            withEnabled(enabled)
        }

        constructor(base: AttIndexDef) : this() {
            enabled = base.enabled
        }

        fun withEnabled(enabled: Boolean?): Builder {
            this.enabled = enabled ?: false
            return this
        }

        fun build(): AttIndexDef {
            return AttIndexDef(
                enabled = enabled
            )
        }
    }
}
