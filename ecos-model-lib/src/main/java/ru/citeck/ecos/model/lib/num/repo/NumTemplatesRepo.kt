package ru.citeck.ecos.model.lib.num.repo

import ru.citeck.ecos.model.lib.num.dto.NumTemplateDef
import ru.citeck.ecos.records2.RecordRef

interface NumTemplatesRepo {

    fun getNumTemplate(templateRef: RecordRef): NumTemplateDef?
}
