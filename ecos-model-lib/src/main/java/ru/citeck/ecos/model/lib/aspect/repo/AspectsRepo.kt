package ru.citeck.ecos.model.lib.aspect.repo

import ru.citeck.ecos.model.lib.aspect.dto.AspectInfo
import ru.citeck.ecos.webapp.api.entity.EntityRef

interface AspectsRepo {

    fun getAspectInfo(aspectRef: EntityRef): AspectInfo?

    fun getAspectsForAtts(attributes: Set<String>): List<EntityRef>
}
