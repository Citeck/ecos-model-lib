package ru.citeck.ecos.model.lib.role.dto

import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault

@IncludeNonDefault
@JsonDeserialize(builder = RoleDef.Builder::class)
data class RoleDef(
    val id: String,
    val name: MLText,
    val attributes: List<String>,
    val assignees: List<String>,
    val computed: RoleComputedDef
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
        var attributes: List<String> = emptyList()
        var assignees: List<String> = emptyList()
        var computed: RoleComputedDef = RoleComputedDef.EMPTY

        constructor(base: RoleDef) : this() {
            this.id = base.id
            this.name = base.name
            this.attributes = ArrayList(base.attributes)
            this.assignees = base.assignees.toList()
            this.computed = base.computed
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
            withAttributes(
                if (attribute.isNullOrBlank()) {
                    emptyList()
                } else {
                    listOf(attribute)
                }
            )
            return this
        }

        fun withAttributes(attributes: List<String>?): Builder {
            this.attributes = attributes?.filter { it.isNotBlank() } ?: emptyList()
            return this
        }

        fun withAssignees(assignees: List<String>?): Builder {
            this.assignees = assignees?.filter { it.isNotBlank() } ?: emptyList()
            return this
        }

        fun withComputed(computed: RoleComputedDef?): Builder {
            this.computed = computed ?: RoleComputedDef.EMPTY
            return this
        }

        fun build(): RoleDef {
            return RoleDef(id, name, attributes, assignees, computed)
        }
    }
}
