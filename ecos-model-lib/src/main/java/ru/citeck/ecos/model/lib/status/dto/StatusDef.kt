package ru.citeck.ecos.model.lib.status.dto

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault

@JsonDeserialize(builder = StatusDef.Builder::class)
@IncludeNonDefault
data class StatusDef(
    val id: String,
    val name: MLText,
    val config: ObjectData
) {

    companion object {

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): StatusDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): StatusDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        var id: String = ""
        var name: MLText = MLText()
        var config: ObjectData = ObjectData.create()

        constructor(base: StatusDef) : this() {
            this.id = base.id
            this.name = base.name
            this.config = ObjectData.deepCopyOrNew(base.config)
        }

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withName(name: MLText?): Builder {
            this.name = name ?: MLText.EMPTY
            return this
        }

        fun withConfig(config: ObjectData?): Builder {
            this.config = config ?: ObjectData.create()
            return this
        }

        fun build(): StatusDef {
            return StatusDef(id, name, config)
        }
    }
}
