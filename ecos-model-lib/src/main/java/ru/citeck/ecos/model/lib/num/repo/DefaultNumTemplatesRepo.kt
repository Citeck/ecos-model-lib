package ru.citeck.ecos.model.lib.num.repo

import ru.citeck.ecos.model.lib.num.dto.NumTemplateDef
import ru.citeck.ecos.records2.RecordRef

class DefaultNumTemplatesRepo : NumTemplatesRepo {

    override fun getNumTemplate(templateRef: RecordRef): NumTemplateDef? {
        return null
    }
}
