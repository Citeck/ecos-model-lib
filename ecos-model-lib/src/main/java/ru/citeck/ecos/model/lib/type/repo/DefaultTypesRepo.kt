package ru.citeck.ecos.model.lib.type.repo

import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.webapp.api.entity.EntityRef

class DefaultTypesRepo : TypesRepo {

    override fun getTypeInfo(typeRef: EntityRef): TypeInfo? = null

    override fun getChildren(typeRef: EntityRef): List<EntityRef> = emptyList()
}
