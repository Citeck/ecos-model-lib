package ru.citeck.ecos.model.lib.permissions.dto

import ru.citeck.ecos.commons.json.Json

data class PermissionsDef(
    val matrix: Matrix,
    val rules: List<PermissionRule>
) {

    companion object {

        @JvmField
        val EMPTY = PermissionsDef(Matrix.EMPTY, emptyList())

        @JvmStatic
        fun create() : Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit) : PermissionsDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }
    }

    fun copy() : Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit) : PermissionsDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    class Builder() {

        private var matrix = Matrix.EMPTY
        private var rules = emptyList<PermissionRule>()

        constructor(base: PermissionsDef) : this() {
            this.matrix = Json.mapper.copy(base.matrix)!!
            this.rules = ArrayList(base.rules)
        }

        fun setMatrix(matrix: Map<String, Map<String, PermissionLevel>>) : Builder {
            this.matrix = Json.mapper.convert(matrix, Matrix::class.java)!!
            return this
        }

        fun setMatrix(matrix: Matrix) : Builder {
            this.matrix = matrix
            return this
        }

        fun setRules(rules: List<PermissionRule>) : Builder {
            this.rules = rules
            return this
        }

        fun build() : PermissionsDef {
            return PermissionsDef(matrix, rules)
        }
    }

    /**
     * <Role, <Status, PermissionType>>
     */
    class Matrix : HashMap<String, Map<String, PermissionLevel>>() {

        companion object {
            @JvmField
            val EMPTY = Matrix()
        }
    }
}