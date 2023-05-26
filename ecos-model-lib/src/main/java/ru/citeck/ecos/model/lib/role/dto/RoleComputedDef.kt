package ru.citeck.ecos.model.lib.role.dto

import ecos.com.fasterxml.jackson210.annotation.JsonEnumDefaultValue
import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault

@IncludeNonDefault
@JsonDeserialize(builder = RoleComputedDef.Builder::class)
data class RoleComputedDef(
    val type: ComputedRoleType,
    val config: ObjectData
) {
    companion object {

        @JvmField
        val EMPTY = create().build()

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): RoleComputedDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): RoleComputedDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        var type: ComputedRoleType = ComputedRoleType.NONE
        var config: ObjectData = ObjectData.create()

        constructor(base: RoleComputedDef) : this() {
            this.type = base.type
            this.config = base.config
        }

        fun withType(type: ComputedRoleType?): Builder {
            this.type = type ?: ComputedRoleType.NONE
            return this
        }

        fun withConfig(config: ObjectData): Builder {
            this.config = config
            return this
        }

        fun build(): RoleComputedDef {
            return RoleComputedDef(type, config)
        }
    }
}

enum class ComputedRoleType {
    SCRIPT,
    ATTRIBUTE,
    VALUE,
    DMN,

    @JsonEnumDefaultValue
    NONE;
}
