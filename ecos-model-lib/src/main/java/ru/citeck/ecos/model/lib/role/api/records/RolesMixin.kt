package ru.citeck.ecos.model.lib.role.api.records

import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.role.constants.RoleConstants
import ru.citeck.ecos.model.lib.role.service.RoleService
import ru.citeck.ecos.records3.record.atts.value.AttValue
import ru.citeck.ecos.records3.record.atts.value.AttValueCtx
import ru.citeck.ecos.records3.record.mixin.AttMixin

class RolesMixin(val service: RoleService) : AttMixin {

    override fun getAtt(path: String, value: AttValueCtx): Any? {
        if (path == RoleConstants.ATT_ROLES) {
            return RolesAttValue(value)
        }
        return null
    }

    override fun getProvidedAtts(): Collection<String> {
        return listOf(RoleConstants.ATT_ROLES)
    }

    inner class RolesAttValue(private val value: AttValueCtx) : AttValue {

        override fun getAtt(name: String?): Any? {
            return when (name) {
                RoleConstants.ATT_IS_CURRENT_USER_MEMBER_OF -> IsMemberOfRoleValue(value)
                RoleConstants.ATT_ASSIGNEES_OF -> AssigneesOfRoleValue(value)
                else -> null
            }
        }
    }

    inner class AssigneesOfRoleValue(private val value: AttValueCtx) : AttValue {

        override fun getAtt(roleName: String): Any {
            return AuthContext.runAsSystem {
                service.getAssignees(value.getRef(), roleName)
            }
        }
    }

    inner class IsMemberOfRoleValue(private val value: AttValueCtx) : AttValue {

        override fun getAtt(roleName: String): Boolean {
            return AuthContext.runAsSystem {
                service.isRoleMember(value.getRef(), roleName)
            }
        }
    }
}
