package ru.citeck.ecos.model.lib.role.service

import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.status.dto.StatusDef
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.utils.ModelUtils
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.atts.schema.annotation.AttName
import ru.citeck.ecos.webapp.api.entity.EntityRef
import kotlin.test.assertEquals

class StatusServiceTest {

    @Test
    fun test() {

        val statusDraft = "draft"
        val statusRework = "rework"

        val services = object : ModelServiceFactory() {

            override fun createTypesRepo(): TypesRepo {

                return object : TypesRepo {

                    override fun getTypeInfo(typeRef: EntityRef): TypeInfo? {
                        if (typeRef == RecordDto.RECORD_TYPE_REF || typeRef == RecordDto.RECORD_TYPE_REF_CHILD) {
                            val model = TypeModelDef.create {
                                statuses = listOf(
                                    StatusDef.create {
                                        id = statusDraft
                                        name = MLText(statusDraft)
                                    },
                                    StatusDef.create {
                                        id = statusRework
                                        name = MLText(statusRework)
                                    }
                                )
                            }
                            val parentRef = if (typeRef == RecordDto.RECORD_TYPE_REF_CHILD) {
                                RecordDto.RECORD_TYPE_REF
                            } else {
                                EntityRef.EMPTY
                            }
                            return TypeInfo.create {
                                withId(typeRef.getLocalId())
                                withParentRef(parentRef)
                                withModel(model)
                            }
                        }
                        return null
                    }
                    override fun getChildren(typeRef: EntityRef): List<EntityRef> {
                        return emptyList()
                    }
                }
            }
        }

        services.setRecordsServices(RecordsServiceFactory())

        val expectedDraft = StatusDef(statusDraft, MLText(statusDraft), ObjectData.create(null))
        val expectedRework = StatusDef(statusRework, MLText(statusRework), ObjectData.create(null))

        val draftParent = services.statusService.getStatusDefByType(RecordDto(RecordDto.RECORD_TYPE_REF).type, statusDraft)
        assertEquals(expectedDraft, draftParent)

        val draftChild = services.statusService.getStatusDefByType(RecordDto(RecordDto.RECORD_TYPE_REF_CHILD).type, statusDraft)
        assertEquals(expectedDraft, draftChild)

        val reworkParent = services.statusService.getStatusDefByType(RecordDto(RecordDto.RECORD_TYPE_REF).type, statusRework)
        assertEquals(expectedRework, reworkParent)

        val statusesParent = services.statusService.getStatusesByType(RecordDto(RecordDto.RECORD_TYPE_REF).type)
        assertEquals(statusesParent, hashMapOf(Pair(statusDraft, expectedDraft), Pair(statusRework, expectedRework)))

        val statusesChild = services.statusService.getStatusesByType(RecordDto(RecordDto.RECORD_TYPE_REF_CHILD).type)
        assertEquals(statusesChild, hashMapOf(Pair(statusDraft, expectedDraft), Pair(statusRework, expectedRework)))
    }

    class RecordDto(
        @AttName("_type")
        val type: EntityRef = RECORD_TYPE_REF
    ) {
        companion object {
            val RECORD_TYPE_REF = ModelUtils.getTypeRef("custom-type")
            val RECORD_TYPE_REF_CHILD = ModelUtils.getTypeRef("custom-type-child")
        }
    }
}
