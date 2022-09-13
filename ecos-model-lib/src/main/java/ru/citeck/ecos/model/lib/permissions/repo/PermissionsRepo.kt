package ru.citeck.ecos.model.lib.permissions.repo

import ru.citeck.ecos.model.lib.type.dto.TypePermsDef
import ru.citeck.ecos.webapp.api.entity.EntityRef

interface PermissionsRepo {

    fun getPermissionsForType(typeRef: EntityRef): TypePermsDef?
}
