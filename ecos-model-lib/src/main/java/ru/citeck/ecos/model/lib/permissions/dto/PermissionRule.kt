package ru.citeck.ecos.model.lib.permissions.dto

import ru.citeck.ecos.records2.predicate.model.Predicate
import ru.citeck.ecos.records2.predicate.model.VoidPredicate

data class PermissionRule(

    val roles: List<String> = emptyList(),
    val statuses: List<String> = emptyList(),

    val condition: Predicate = VoidPredicate.INSTANCE,

    val permission: String,

    val allow: Boolean = true
)
