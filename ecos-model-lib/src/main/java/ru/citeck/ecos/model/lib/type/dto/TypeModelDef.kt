package ru.citeck.ecos.model.lib.type.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ru.citeck.ecos.commons.data.DataValue
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.json.serialization.annotation.IncludeNonDefault
import ru.citeck.ecos.model.lib.attributes.dto.AttributeDef
import ru.citeck.ecos.model.lib.procstages.dto.ProcStageDef
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.status.dto.StatusDef
import ru.citeck.ecos.model.lib.utils.ModelUtils
import kotlin.random.Random

@JsonDeserialize(builder = TypeModelDef.Builder::class)
@IncludeNonDefault
data class TypeModelDef(
    val roles: List<RoleDef>,
    val statuses: List<StatusDef>,
    val stages: List<ProcStageDef>,
    val attributes: List<AttributeDef>,
    val systemAttributes: List<AttributeDef>
) {

    companion object {

        @JvmField
        val EMPTY = create().build()

        @JvmStatic
        fun create(): Builder {
            return Builder()
        }

        @JvmStatic
        fun create(builder: Builder.() -> Unit): TypeModelDef {
            val builderObj = Builder()
            builder.invoke(builderObj)
            return builderObj.build()
        }

        private fun generateId(): String {
            return Random.nextInt(100000, Integer.MAX_VALUE).toString(36).padStart(6, '0')
        }
    }

    fun copy(): Builder {
        return Builder(this)
    }

    fun copy(builder: Builder.() -> Unit): TypeModelDef {
        val builderObj = Builder(this)
        builder.invoke(builderObj)
        return builderObj.build()
    }

    @JsonIgnore
    fun getAllAttributes(): List<AttributeDef> {
        val result = ArrayList<AttributeDef>()
        result.addAll(attributes)
        result.addAll(systemAttributes)
        return result
    }

    @JsonIgnore
    fun isEmpty(): Boolean {
        return roles.isEmpty() &&
            statuses.isEmpty() &&
            attributes.isEmpty() &&
            systemAttributes.isEmpty()
    }

    class Builder() {

        var roles: List<RoleDef> = emptyList()
        var statuses: List<StatusDef> = emptyList()
        var stages: List<ProcStageDef> = emptyList()
        var attributes: List<AttributeDef> = emptyList()
        var systemAttributes: List<AttributeDef> = emptyList()

        constructor(base: TypeModelDef) : this() {
            this.roles = DataValue.create(base.roles).asList(RoleDef::class.java)
            this.stages = DataValue.create(base.stages).asList(ProcStageDef::class.java)
            this.statuses = DataValue.create(base.statuses).asList(StatusDef::class.java)
            this.attributes = DataValue.create(base.attributes).asList(AttributeDef::class.java)
            this.systemAttributes = DataValue.create(base.systemAttributes).asList(AttributeDef::class.java)
        }

        fun withRoles(roles: List<RoleDef>?): Builder {
            this.roles = roles?.filter { it.id.isNotBlank() } ?: emptyList()
            return this
        }

        fun withStatuses(statuses: List<StatusDef>?): Builder {
            this.statuses = statuses?.filter { it.id.isNotBlank() } ?: emptyList()
            return this
        }

        fun withStages(stages: List<ProcStageDef>?): Builder {
            this.stages = stages?.filter { stage ->
                stage.id.isNotBlank() ||
                    !MLText.isEmpty(stage.name) ||
                    stage.statuses.any { it.isNotBlank() }
            } ?: emptyList()
            return this
        }

        fun withAttributes(attributes: List<AttributeDef>?): Builder {
            this.attributes = attributes?.filter { it.id.isNotBlank() } ?: emptyList()
            return this
        }

        fun withSystemAttributes(systemAttributes: List<AttributeDef>?): Builder {
            this.systemAttributes = systemAttributes?.filter { it.id.isNotBlank() } ?: emptyList()
            return this
        }

        fun build(): TypeModelDef {

            val statuses = ModelUtils.mergeElementsById(statuses) { it.id }
            val roles = ModelUtils.mergeElementsById(roles) { it.id }

            val (attributes, systemAttributes) = ModelUtils.getMergedModelAtts(
                attributes,
                systemAttributes
            )

            if (stages.isNotEmpty()) {
                val statusIds = statuses.mapTo(HashSet()) { it.id }
                val errorMessages = ArrayList<String>()
                val stagesByStatus = HashMap<String, ProcStageDef>()
                for (stage in stages) {
                    stage.statuses.forEach {
                        val stageByStatus = stagesByStatus.putIfAbsent(it, stage)
                        if (stageByStatus != null) {
                            errorMessages.add(
                                "Status $it exists in multiple " +
                                    "stages: $stage and $stageByStatus"
                            )
                        }
                        if (!statusIds.contains(it)) {
                            errorMessages.add("Status $it not found in model")
                        }
                    }
                }
                if (errorMessages.isNotEmpty()) {
                    error("Invalid stages config: \n${errorMessages.joinToString("\n")}")
                }
                val registeredIds = HashSet<String>(stages.size)
                stages.forEach {
                    if (it.id.isNotBlank()) {
                        registeredIds.add(it.id)
                    }
                }
                if (registeredIds.size != stages.size) {
                    stages = stages.map { stage ->
                        if (stage.id.isBlank()) {
                            var newId = generateId()
                            while (!registeredIds.add(newId)) {
                                newId = generateId()
                            }
                            stage.copy().withId(newId).build()
                        } else {
                            stage
                        }
                    }
                }
            }
            return TypeModelDef(roles, statuses, stages, attributes, systemAttributes)
        }
    }
}
