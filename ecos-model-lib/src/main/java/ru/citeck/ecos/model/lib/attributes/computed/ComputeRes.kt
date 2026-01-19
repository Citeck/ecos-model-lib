package ru.citeck.ecos.model.lib.attributes.computed

import ru.citeck.ecos.commons.data.DataValue

data class ComputeRes(
    /**
     * Computation result
     */
    val value: DataValue,

    /**
     * Indicates whether the computation result
     * may change between subsequent calculations
     * with the same input data
     */
    val stateful: Boolean
) {
    companion object {
        val EMPTY = ComputeRes(DataValue.NULL, false)
    }
}
