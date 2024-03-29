package ru.citeck.ecos.model.lib.role.api.records

import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.role.constants.RoleConstants
import ru.citeck.ecos.model.lib.role.service.RoleService
import ru.citeck.ecos.records3.record.atts.value.AttValue
import ru.citeck.ecos.records3.record.atts.value.AttValueCtx
import ru.citeck.ecos.records3.record.mixin.AttMixin
import ru.citeck.ecos.webapp.api.entity.EntityRef

class RolesMixin(val service: RoleService) : AttMixin {

    override fun getAtt(path: String, value: AttValueCtx): Any? {
        if (path == RoleConstants.ATT_ROLES) {
            return RolesAttValue(value.getRawRef())
        }
        return null
    }

    override fun getProvidedAtts(): Collection<String> {
        return listOf(RoleConstants.ATT_ROLES)
    }

    inner class RolesAttValue(private val valueRef: EntityRef) : AttValue {

        override fun getAtt(name: String): Any? {
            return when (name) {
                RoleConstants.ATT_IS_CURRENT_USER_MEMBER_OF -> IsMemberOfRoleValue(valueRef)
                RoleConstants.ATT_ASSIGNEES_OF -> AssigneesOfRoleValue(valueRef)
                else -> null
            }
        }
    }

    inner class AssigneesOfRoleValue(private val valueRef: EntityRef) : AttValue {

        override fun getAtt(roleName: String): Any {
            return AuthContext.runAsSystem {
                service.getAssignees(valueRef, roleName)
            }
        }
    }

    inner class IsMemberOfRoleValue(private val valueRef: EntityRef) : AttValue {

        override fun getAtt(roleName: String): Boolean {
            return AuthContext.runAsSystem {
                service.isRoleMember(valueRef, roleName)
            }
        }
    }
}
