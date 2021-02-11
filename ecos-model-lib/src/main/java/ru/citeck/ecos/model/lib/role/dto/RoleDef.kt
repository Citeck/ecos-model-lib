package ru.citeck.ecos.model.lib.role.dto

import ecos.com.fasterxml.jackson210.annotation.JsonInclude
import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.MLText

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
            this.name = base.name
            this.attribute = base.attribute
            this.assignees = base.assignees.toList()
        }

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withName(name: MLText?): Builder {
            this.name = name ?: MLText()
            return this
        }

        fun withAttribute(attribute: String?): Builder {
            this.attribute = attribute ?: ""
            return this
        }

        fun withAssignees(assignees: List<String>?): Builder {
            this.assignees = assignees?.filter { it.isNotBlank() } ?: emptyList()
            return this
        }

        fun build(): RoleDef {
            return RoleDef(id, name, attribute, assignees)
        }
    }
}
