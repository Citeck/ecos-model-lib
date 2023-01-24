package ru.citeck.ecos.model.lib.num.repo

import ru.citeck.ecos.model.lib.num.dto.NumTemplateDef
import ru.citeck.ecos.webapp.api.entity.EntityRef

interface NumTemplatesRepo {

    fun getNumTemplate(templateRef: EntityRef): NumTemplateDef?
}
