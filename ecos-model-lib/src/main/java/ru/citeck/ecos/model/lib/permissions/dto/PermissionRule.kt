package ru.citeck.ecos.model.lib.permissions.dto

import ru.citeck.ecos.records2.predicate.model.Predicate
import ru.citeck.ecos.records2.predicate.model.VoidPredicate

data class PermissionRule(

    val roles: Set<String> = emptySet(),
    val permissions: Set<String> = emptySet(),

    val statuses: Set<String> = emptySet(),
    val condition: Predicate = VoidPredicate.INSTANCE,

    val type: RuleType = RuleType.ALLOW
)
