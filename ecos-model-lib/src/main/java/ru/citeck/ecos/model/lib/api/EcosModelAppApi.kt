package ru.citeck.ecos.model.lib.api

import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.webapp.api.entity.EntityRef

interface EcosModelAppApi {

    fun getNextNumberForModel(model: ObjectData, templateRef: EntityRef): Long
}
