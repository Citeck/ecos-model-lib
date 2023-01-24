package ru.citeck.ecos.model.lib.type.dto

import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.DataValue
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import ru.citeck.ecos.webapp.api.entity.EntityRef
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@IncludeNonDefault
@JsonDeserialize(builder = DocLibDef.Builder::class)
@JackJsonDeserialize(builder = DocLibDef.Builder::class)
data class DocLibDef(
    val enabled: Boolean,
    val dirTypeRef: EntityRef,
    val fileTypeRefs: List<EntityRef>
) {

    companion object {

        @JvmField
        val EMPTY = create {}

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): DocLibDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): DocLibDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        var enabled: Boolean = false
        var dirTypeRef: EntityRef = EntityRef.EMPTY
        var fileTypeRefs: List<EntityRef> = emptyList()

        constructor(base: DocLibDef) : this() {
            this.enabled = base.enabled
            this.dirTypeRef = base.dirTypeRef
            this.fileTypeRefs = DataValue.create(base.fileTypeRefs).asList(EntityRef::class.java)
        }

        fun withEnabled(enabled: Boolean?): Builder {
            this.enabled = enabled ?: false
            return this
        }

        fun withDirTypeRef(dirTypeRef: EntityRef?): Builder {
            this.dirTypeRef = dirTypeRef ?: EntityRef.EMPTY
            return this
        }

        fun withFileTypeRefs(fileTypeRefs: List<EntityRef>?): Builder {
            this.fileTypeRefs = fileTypeRefs?.filter { EntityRef.isNotEmpty(it) } ?: emptyList()
            return this
        }

        fun build(): DocLibDef {
            return DocLibDef(enabled, dirTypeRef, fileTypeRefs)
        }
    }
}
