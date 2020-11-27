package ru.citeck.ecos.model.lib.type.dto

import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.DataValue
import ru.citeck.ecos.records2.RecordRef
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@JsonDeserialize(builder = DocLibDef.Builder::class)
@JackJsonDeserialize(builder = DocLibDef.Builder::class)
data class DocLibDef(
    val enabled: Boolean,
    val dirTypeRef: RecordRef,
    val fileTypeRefs: List<RecordRef>
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
        var dirTypeRef: RecordRef = RecordRef.EMPTY
        var fileTypeRefs: List<RecordRef> = emptyList()

        constructor(base: DocLibDef) : this() {
            this.enabled = base.enabled
            this.dirTypeRef = base.dirTypeRef
            this.fileTypeRefs = DataValue.create(base.fileTypeRefs).asList(RecordRef::class.java)
        }

        fun withEnabled(enabled: Boolean): Builder {
            this.enabled = enabled
            return this
        }

        fun withDirTypeRef(dirTypeRef: RecordRef): Builder {
            this.dirTypeRef = dirTypeRef
            return this
        }

        fun withFileTypeRefs(fileTypeRefs: List<RecordRef>): Builder {
            this.fileTypeRefs = fileTypeRefs
            return this
        }

        fun build(): DocLibDef {
            return DocLibDef(enabled, dirTypeRef, fileTypeRefs)
        }
    }
}
