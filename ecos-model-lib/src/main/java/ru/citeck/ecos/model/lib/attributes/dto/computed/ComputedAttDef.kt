package ru.citeck.ecos.model.lib.attributes.dto.computed

import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.ObjectData
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@JsonDeserialize(builder = ComputedAttDef.Builder::class)
@JackJsonDeserialize(builder = ComputedAttDef.Builder::class)
data class ComputedAttDef constructor(
    val type: ComputedAttType,
    val config: ObjectData,
    val storingType: ComputedAttStoringType
) {
    companion object {

        @JvmField
        val EMPTY = create {}

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): ComputedAttDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): ComputedAttDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        var type: ComputedAttType = ComputedAttType.NONE
        var config: ObjectData = ObjectData.create()
        var storingType = ComputedAttStoringType.NONE

        constructor(base: ComputedAttDef) : this() {
            this.type = base.type
            this.config = base.config.deepCopy()
            this.storingType = base.storingType
        }

        fun withType(type: ComputedAttType): Builder {
            this.type = type
            return this
        }

        fun withConfig(config: ObjectData): Builder {
            this.config = config
            return this
        }

        fun withStoringType(storingType: ComputedAttStoringType): Builder {
            this.storingType = storingType
            return this
        }

        fun build(): ComputedAttDef {
            return ComputedAttDef(type, config, storingType)
        }
    }
}
