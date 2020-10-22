package ru.citeck.ecos.model.lib

import ru.citeck.ecos.model.lib.permissions.service.PermissionsService
import ru.citeck.ecos.records2.RecordsServiceFactory

open class EcosModelServiceFactory {

    val permissionsService: PermissionsService by lazy { createPermissionsService() }

    lateinit var recordsServices: RecordsServiceFactory

    protected open fun createPermissionsService() : PermissionsService {
        return PermissionsService(this)
    }
}
