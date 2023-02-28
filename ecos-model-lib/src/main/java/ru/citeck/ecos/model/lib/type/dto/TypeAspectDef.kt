package ru.citeck.ecos.model.lib.type.dto

import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import ru.citeck.ecos.webapp.api.entity.EntityRef

@IncludeNonDefault
@JsonDeserialize(builder = TypeAspectDef.Builder::class)
data class TypeAspectDef(
    val ref: EntityRef,
    val config: ObjectData,
    val inheritConfig: Boolean
) {

    companion object {

        @JvmField
        val EMPTY = create {}

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): TypeAspectDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): TypeAspectDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    open class Builder() {

        var ref: EntityRef = EntityRef.EMPTY
        var config: ObjectData = ObjectData.create()
        var inheritConfig: Boolean = true

        constructor(base: TypeAspectDef) : this() {
            withRef(base.ref)
            withConfig(base.config)
            withInheritConfig(base.inheritConfig)
        }

        fun withRef(aspectRef: EntityRef?): Builder {
            this.ref = aspectRef ?: EntityRef.EMPTY
            return this
        }

        fun withConfig(aspectConfig: ObjectData?): Builder {
            this.config = aspectConfig ?: ObjectData.create()
            return this
        }

        fun withInheritConfig(inheritConfig: Boolean?): Builder {
            this.inheritConfig = inheritConfig ?: EMPTY.inheritConfig
            return this
        }

        fun build(): TypeAspectDef {
            return TypeAspectDef(
                ref,
                config,
                inheritConfig
            )
        }
    }
}
