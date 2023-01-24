package ru.citeck.ecos.model.lib.api.commands

import ru.citeck.ecos.commands.CommandsServiceFactory
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.api.EcosModelAppApi
import ru.citeck.ecos.webapp.api.entity.EntityRef

class CommandsModelAppApi(commandsServices: CommandsServiceFactory) : EcosModelAppApi {

    private val commandsService = commandsServices.commandsService

    override fun getNextNumberForModel(model: ObjectData, templateRef: EntityRef): Long {

        val result = commandsService.executeSync {
            withTargetApp("emodel")
            withBody(GetNextNumberCommand(templateRef, model))
        }.getResultAs(GetNextNumberResult::class.java)
            ?: error("Unexpected result for getNextNumber. Template: $templateRef model: $model")

        return result.number
    }
}
