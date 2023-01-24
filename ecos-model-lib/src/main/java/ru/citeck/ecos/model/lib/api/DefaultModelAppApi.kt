package ru.citeck.ecos.model.lib.api

import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.webapp.api.entity.EntityRef

class DefaultModelAppApi : EcosModelAppApi {

    override fun getNextNumberForModel(model: ObjectData, templateRef: EntityRef): Long {
        error("Not implemented")
    }
}
