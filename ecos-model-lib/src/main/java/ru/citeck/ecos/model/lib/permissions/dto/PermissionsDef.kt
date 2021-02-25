package ru.citeck.ecos.model.lib.permissions.dto

import ecos.com.fasterxml.jackson210.annotation.JsonIgnore
import ecos.com.fasterxml.jackson210.annotation.JsonSetter
import ecos.com.fasterxml.jackson210.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.json.Json
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import com.fasterxml.jackson.annotation.JsonIgnore as JackJsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize as JackJsonDeserialize

@IncludeNonDefault
@JsonDeserialize(builder = PermissionsDef.Builder::class)
@JackJsonDeserialize(builder = PermissionsDef.Builder::class)
data class PermissionsDef(
    val matrix: Matrix,
    val rules: List<PermissionRule>
) {
    companion object {

        @JvmField
        val EMPTY = create {}

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): PermissionsDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): PermissionsDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    @JsonIgnore
    @JackJsonIgnore
    fun isEmpty(): Boolean {
        return rules.isEmpty() && matrix.isEmpty()
    }

    class Builder() {

        private var matrix: Matrix = Matrix()
        private var rules: List<PermissionRule> = emptyList()

        constructor(base: PermissionsDef) : this() {
            this.matrix = Json.mapper.copy(base.matrix)!!
            this.rules = ArrayList(base.rules)
        }

        fun withMatrix(matrix: Map<String, Map<String, PermissionLevel>>?): Builder {
            this.matrix = if (matrix.isNullOrEmpty()) {
                Matrix()
            } else {
                Json.mapper.convert(matrix, Matrix::class.java) ?: error("Incorrect matrix: $matrix")
            }
            return this
        }

        @JsonSetter
        fun withMatrix(matrix: Matrix?): Builder {
            this.matrix = matrix ?: Matrix()
            return this
        }

        fun withRules(rules: List<PermissionRule>?): Builder {
            this.rules = rules ?: emptyList()
            return this
        }

        fun build(): PermissionsDef {
            return PermissionsDef(matrix, rules)
        }
    }

    /**
     * <Role, <Status, PermissionType>>
     */
    class Matrix : LinkedHashMap<String, Map<String, PermissionLevel>>()
}
