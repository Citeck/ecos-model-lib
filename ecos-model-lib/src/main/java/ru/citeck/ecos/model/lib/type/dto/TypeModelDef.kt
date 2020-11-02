package ru.citeck.ecos.model.lib.type.dto

import ru.citeck.ecos.commons.data.DataValue
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.status.dto.StatusDef

data class TypeModelDef(
    val roles: List<RoleDef>,
    val statuses: List<StatusDef>,
    val attributes: List<AttributeDef>
) {

    companion object {

        @JvmField
        val EMPTY = create().build()

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): TypeModelDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): TypeModelDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        var roles: List<RoleDef> = emptyList()
        var statuses: List<StatusDef> = emptyList()
        var attributes: List<AttributeDef> = emptyList()

        constructor(base: TypeModelDef) : this() {
            this.roles = DataValue.create(base.roles).asList(RoleDef::class.java)
            this.statuses = DataValue.create(base.statuses).asList(StatusDef::class.java)
            this.attributes = DataValue.create(base.attributes).asList(AttributeDef::class.java)
        }

        fun withRoles(roles: List<RoleDef>): Builder {
            this.roles = roles
            return this
        }

        fun withStatuses(statuses: List<StatusDef>): Builder {
            this.statuses = statuses
            return this
        }

        fun withAttributes(attributes: List<AttributeDef>): Builder {
            this.attributes = attributes
            return this
        }

        fun build(): TypeModelDef {
            return TypeModelDef(roles, statuses, attributes)
        }
    }
}
