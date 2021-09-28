package ru.citeck.ecos.model.lib.api

import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.records2.RecordRef

class DefaultModelAppApi : EcosModelAppApi {

    override fun getNextNumberForModel(model: ObjectData, templateRef: RecordRef): Long {
        error("Not implemented")
    }
}
