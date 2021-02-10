package ru.citeck.ecos.model.lib.type.dto

import ecos.com.fasterxml.jackson210.annotation.JsonInclude
import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.model.lib.permissions.dto.PermissionsDef
import ru.citeck.ecos.records2.RecordRef
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(builder = TypePermsDef.Builder::class)
@JackJsonDeserialize(builder = TypeModelDef.Builder::class)
data class TypePermsDef(
    val id: String,
    val typeRef: RecordRef,
    val permissions: PermissionsDef,
    val attributes: Map<String, PermissionsDef>
) {

    companion object {

        @JvmField
        val EMPTY = Builder().build()

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): TypePermsDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): TypePermsDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        var id: String = ""
        var typeRef: RecordRef = RecordRef.EMPTY
        var permissions: PermissionsDef = PermissionsDef.EMPTY
        var attributes: Map<String, PermissionsDef> = emptyMap()

        constructor(base: TypePermsDef) : this() {
            this.id = base.id
            this.typeRef = base.typeRef
            this.permissions = base.permissions
            this.attributes = HashMap(base.attributes)
        }

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withTypeRef(typeRef: RecordRef): Builder {
            this.typeRef = typeRef
            return this
        }

        fun withPermissions(permissions: PermissionsDef?): Builder {
            this.permissions = permissions ?: PermissionsDef.EMPTY
            return this
        }

        fun withAttributes(attributes: Map<String, PermissionsDef>?): Builder {
            this.attributes = attributes ?: emptyMap()
            return this
        }

        fun build(): TypePermsDef {
            return TypePermsDef(id, typeRef, permissions, attributes)
        }
    }
}
