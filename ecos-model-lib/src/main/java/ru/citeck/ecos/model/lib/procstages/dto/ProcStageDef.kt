package ru.citeck.ecos.model.lib.procstages.dto

import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@JsonDeserialize(builder = ProcStageDef.Builder::class)
@JackJsonDeserialize(builder = ProcStageDef.Builder::class)
@IncludeNonDefault
data class ProcStageDef(
    val id: String,
    val name: MLText,
    val statuses: List<String>
) {

    companion object {

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): ProcStageDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): ProcStageDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        var id: String = ""
        var name: MLText = MLText()
        var statuses: List<String> = emptyList()

        constructor(base: ProcStageDef) : this() {
            this.id = base.id
            this.name = base.name
            this.statuses = ArrayList(base.statuses)
        }

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withName(name: MLText?): Builder {
            this.name = name ?: MLText.EMPTY
            return this
        }

        fun withStatuses(statuses: List<String>?): Builder {
            this.statuses = statuses ?: emptyList()
            return this
        }

        fun build(): ProcStageDef {
            return ProcStageDef(id, name, statuses)
        }
    }
}
