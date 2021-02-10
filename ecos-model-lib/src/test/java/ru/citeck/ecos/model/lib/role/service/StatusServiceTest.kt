package ru.citeck.ecos.model.lib.role.service

import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.data.MLText
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.status.dto.StatusDef
import ru.citeck.ecos.model.lib.type.dto.TypeDef
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.op.atts.service.schema.annotation.AttName
import kotlin.test.assertEquals

class StatusServiceTest {

    @Test
    fun test() {

        val statusDraft = "draft"
        val statusRework = "rework"

        val services = object : ModelServiceFactory() {
            override fun createTypesRepo(): TypesRepo {
                return object : TypesRepo {
                    override fun getTypeDef(typeRef: RecordRef): TypeDef? {
                        if (typeRef == RecordDto.RECORD_TYPE_REF) {
                            return TypeDef.create {
                                id = RecordDto.RECORD_TYPE_REF.id
                                model = TypeModelDef.create {
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
                            }
                        }
                        if (typeRef == RecordDto.RECORD_TYPE_REF_CHILD) {
                            return TypeDef.create {
                                id = RecordDto.RECORD_TYPE_REF_CHILD.id
                                model = TypeModelDef.create {
                                    statuses = listOf(
                                        StatusDef.create {
                                            id = statusRework
                                            name = MLText(statusRework + "_child")
                                        }
                                    )
                                }
                                parentRef = RecordDto.RECORD_TYPE_REF
                            }
                        }
                        return null
                    }
                    override fun getChildren(typeRef: RecordRef): List<RecordRef> {
                        return emptyList()
                    }
                }
            }
        }
        services.setRecordsServices(RecordsServiceFactory())

        val expectedDraft = StatusDef(statusDraft, MLText(statusDraft), ObjectData.create(null))
        val expectedRework = StatusDef(statusRework, MLText(statusRework), ObjectData.create(null))
        val expectedReworkChild = StatusDef(statusRework, MLText(statusRework + "_child"), ObjectData.create(null))

        val draftParent = services.statusService.getStatusDefByType(RecordDto(RecordDto.RECORD_TYPE_REF).type, statusDraft)
        assertEquals(expectedDraft, draftParent)

        val draftChild = services.statusService.getStatusDefByType(RecordDto(RecordDto.RECORD_TYPE_REF_CHILD).type, statusDraft)
        assertEquals(expectedDraft, draftChild)

        val reworkParent = services.statusService.getStatusDefByType(RecordDto(RecordDto.RECORD_TYPE_REF).type, statusRework)
        assertEquals(expectedRework, reworkParent)

        val reworkChild = services.statusService.getStatusDefByType(RecordDto(RecordDto.RECORD_TYPE_REF_CHILD).type, statusRework)
        assertEquals(expectedReworkChild, reworkChild)

        val statusesParent = services.statusService.getStatusesByType(RecordDto(RecordDto.RECORD_TYPE_REF).type)
        assertEquals(statusesParent, hashMapOf(Pair(statusDraft, expectedDraft), Pair(statusRework, expectedRework)))

        val statusesChild = services.statusService.getStatusesByType(RecordDto(RecordDto.RECORD_TYPE_REF_CHILD).type)
        assertEquals(statusesChild, hashMapOf(Pair(statusDraft, expectedDraft), Pair(statusRework, expectedReworkChild)))
    }

    class RecordDto(
        @AttName("_type")
        val type: RecordRef = RECORD_TYPE_REF
    ) {
        companion object {
            val RECORD_TYPE_REF = TypeUtils.getTypeRef("custom-type")
            val RECORD_TYPE_REF_CHILD = TypeUtils.getTypeRef("custom-type-child")
        }
    }
}
