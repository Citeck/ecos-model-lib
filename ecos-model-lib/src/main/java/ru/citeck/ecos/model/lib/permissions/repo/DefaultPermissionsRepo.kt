package ru.citeck.ecos.model.lib.permissions.repo

import ru.citeck.ecos.model.lib.type.dto.TypePermsDef
import ru.citeck.ecos.records2.RecordRef

class DefaultPermissionsRepo : PermissionsRepo {

    override fun getPermissionsForType(typeRef: RecordRef): TypePermsDef? {
        return null
    }
}
