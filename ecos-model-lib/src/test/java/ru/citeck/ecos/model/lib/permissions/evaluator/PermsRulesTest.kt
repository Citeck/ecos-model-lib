package ru.citeck.ecos.model.lib.permissions.evaluator

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.permissions.dto.PermissionRule
import ru.citeck.ecos.model.lib.permissions.dto.PermissionType
import ru.citeck.ecos.records2.predicate.model.Predicates

class PermsRulesTest : PermsEvaluatorTestBase() {

    @Test
    fun test() {

        setRoles(listOf("initiator", "approver"))
        setStatuses(listOf("draft", "approve"))
        setStatusForRecord("draft")

        val recordId = "recId"
        addRecord(
            recordId,
            RecData(
                "str-val", 123, 456.1, true,
                RecData("str-inner", 789, 1011.0, true)
            )
        )

        val perms0 = getPerms(
            recordId,
            createPermsDefWithRules(
                listOf(
                    PermissionRule(
                        roles = setOf("initiator"),
                        permissions = setOf("AddChildren")
                    ),
                    PermissionRule(
                        roles = setOf("approver"),
                        permissions = setOf(PermissionType.READ.name)
                    )
                )
            )
        )

        assertThat(perms0.getPermissions("initiator").contains("AddChildren")).isTrue()
        assertThat(perms0.getPermissions("initiator").contains("READ")).isFalse()
        assertThat(perms0.getPermissions("approver").contains("READ")).isTrue()
        assertThat(perms0.getPermissions("approver").contains("AddChildren")).isFalse()

        val perms1 = getPerms(
            recordId,
            createPermsDefWithRules(
                listOf(
                    PermissionRule(
                        roles = setOf("initiator"),
                        permissions = setOf("perms-by-str-eq"),
                        condition = Predicates.eq("fieldStr", "str-val")
                    ),
                    PermissionRule(
                        roles = setOf("initiator"),
                        permissions = setOf("perms-by-str-not-eq"),
                        condition = Predicates.eq("fieldStr", "str-val-not-eq")
                    ),
                    PermissionRule(
                        roles = setOf("initiator"),
                        permissions = setOf("perms-by-complex-condition"),
                        condition = Predicates.and(
                            Predicates.eq("fieldStr", "str-val"),
                            Predicates.gt("fieldNumDouble", 456.0),
                            Predicates.eq("fieldBool", true)
                        )
                    )
                )
            )
        )

        assertThat(perms1.getPermissions("initiator").contains("perms-by-str-eq")).isTrue()
        assertThat(perms1.getPermissions("initiator").contains("perms-by-str-not-eq")).isFalse()
        assertThat(perms1.getPermissions("initiator").contains("perms-by-complex-condition")).isTrue()

        val perms2 = getPerms(
            recordId,
            createPermsDefWithRules(
                listOf(
                    PermissionRule(
                        roles = setOf("initiator"),
                        permissions = setOf("perms-by-inner-att-condition"),
                        condition = Predicates.and(
                            Predicates.eq("inner.fieldStr", "str-inner"),
                            Predicates.eq("inner.fieldNumInt", 789)
                        )
                    )
                )
            )
        )

        assertThat(perms2.getPermissions("initiator").contains("perms-by-inner-att-condition")).isTrue()
    }

    class RecData(
        val fieldStr: String,
        val fieldNumInt: Int,
        val fieldNumDouble: Double,
        val fieldBool: Boolean,
        val inner: RecData? = null
    )
}
