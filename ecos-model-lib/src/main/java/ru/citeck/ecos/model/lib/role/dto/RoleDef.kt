package ru.citeck.ecos.model.lib.role.dto

import ecos.com.fasterxml.jackson210.annotation.JsonInclude
import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.DataValue
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.json.Json

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonDeserialize(builder = RoleDef.Builder::class)
data class RoleDef(
    val id: String,
    val name: MLText,
    val attribute: String,
    val assignees: List<String>
) {

    companion object {

        @JvmField
        val EMPTY = create().build()

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): RoleDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): RoleDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        var id: String = ""
        var name: MLText = MLText()
        var attribute: String = ""
        var assignees: List<String> = emptyList()

        constructor(base: RoleDef) : this() {
            this.id = base.id
            this.name = Json.mapper.copy(base.name)!!
            this.attribute = base.attribute
            this.assignees = DataValue.create(base.assignees).asStrList()
        }

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withName(name: MLText): Builder {
            this.name = name
            return this
        }

        fun withAttribute(attribute: String): Builder {
            this.attribute = attribute
            return this
        }

        fun withAssignees(authorities: List<String>): Builder {
            this.assignees = authorities
            return this
        }

        fun build(): RoleDef {
            return RoleDef(id, name, attribute, assignees)
        }
    }
}
