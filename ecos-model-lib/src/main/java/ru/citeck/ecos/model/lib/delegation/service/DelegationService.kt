package ru.citeck.ecos.model.lib.delegation.service

import ru.citeck.ecos.model.lib.delegation.dto.AuthDelegation
import ru.citeck.ecos.model.lib.delegation.dto.PermissionDelegateData
import ru.citeck.ecos.model.lib.permissions.dto.PermissionType

interface DelegationService {

    fun getActiveAuthDelegations(user: String, types: Collection<String>): List<AuthDelegation>

    /**
     * Delegate [permission] from one user to another for [record], if not delegated yet.
     */
    fun delegatePermission(record: Any, permission: PermissionType, from: String, to: String)

    /**
     * Get permission delegated authorities for [record] with [permission].
     */
    fun getPermissionDelegates(record: Any, permission: PermissionType): List<PermissionDelegateData>
}
