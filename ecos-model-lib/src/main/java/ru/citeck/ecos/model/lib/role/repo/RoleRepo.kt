package ru.citeck.ecos.model.lib.role.repo

import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.records2.RecordRef

interface RoleRepo {

    fun getRolesByType(typeRef: RecordRef): List<RoleDef>
}
