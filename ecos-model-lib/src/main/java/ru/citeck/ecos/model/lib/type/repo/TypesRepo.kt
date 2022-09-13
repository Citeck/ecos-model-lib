package ru.citeck.ecos.model.lib.type.repo

import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.webapp.api.entity.EntityRef

interface TypesRepo {

    fun getTypeInfo(typeRef: EntityRef): TypeInfo?

    fun getChildren(typeRef: EntityRef): List<EntityRef>
}
