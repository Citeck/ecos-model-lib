package ru.citeck.ecos.model.lib.role.api.records

import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.role.service.RoleService
import ru.citeck.ecos.records3.record.atts.value.AttValue
import ru.citeck.ecos.records3.record.atts.value.AttValueCtx
import ru.citeck.ecos.records3.record.mixin.AttMixin

class RolesMixin(val service: RoleService) : AttMixin {

    companion object {
        private const val ATT_ROLES = "_roles"
    }

    override fun getAtt(path: String, value: AttValueCtx): Any? {
        if (path == ATT_ROLES) {
            return RolesAttValue(value)
        }
        return null
    }

    override fun getProvidedAtts(): Collection<String> {
        return listOf(ATT_ROLES)
    }

    inner class RolesAttValue(val value: AttValueCtx) {

        fun getIsCurrentUserMemberOf(): RolesIsMemberOfValue {
            return RolesIsMemberOfValue(value)
        }
    }

    inner class RolesIsMemberOfValue(private val value: AttValueCtx) : AttValue {

        override fun getAtt(roleName: String): Boolean {
            val currentUserAuthorities = AuthContext.getCurrentAuthorities()
            val assignees = service.getAssignees(value.getRef(), roleName)
            return assignees.any { currentUserAuthorities.contains(it) }
        }
    }
}
