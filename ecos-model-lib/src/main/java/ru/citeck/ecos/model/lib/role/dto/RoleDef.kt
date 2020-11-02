package ru.citeck.ecos.model.lib.role.dto

import ecos.com.fasterxml.jackson210.annotation.JsonInclude
import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.commons.json.Json

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonDeserialize(builder = RoleDef.Builder::class)
data class RoleDef(
    val id: String,
    val name: MLText,
    val config: ObjectData,
    val assignees: RoleAssigneeDef
) {

    companion object {

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
        var config: ObjectData = ObjectData.create()
        var assignees = RoleAssigneeDef.EMPTY

        constructor(base: RoleDef) : this() {
            this.id = base.id
            this.name = Json.mapper.copy(base.name)!!
            this.config = ObjectData.deepCopy(base.config)!!
            this.assignees = Json.mapper.copy(base.assignees)!!
        }

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withName(name: MLText): Builder {
            this.name = name
            return this
        }

        fun withConfig(config: ObjectData): Builder {
            this.config = config
            return this
        }

        fun withAssignees(assignees: RoleAssigneeDef): Builder {
            this.assignees = assignees
            return this
        }

        fun build(): RoleDef {
            return RoleDef(id, name, config, assignees)
        }
    }
}
