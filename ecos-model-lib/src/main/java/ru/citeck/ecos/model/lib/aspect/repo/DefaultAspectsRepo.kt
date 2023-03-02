package ru.citeck.ecos.model.lib.aspect.repo

import ru.citeck.ecos.model.lib.aspect.dto.AspectInfo
import ru.citeck.ecos.webapp.api.entity.EntityRef

class DefaultAspectsRepo : AspectsRepo {

    override fun getAspectsForAtts(attributes: Set<String>): List<EntityRef> {
        return emptyList()
    }

    override fun getAspectInfo(aspectRef: EntityRef): AspectInfo? {
        return null
    }
}
