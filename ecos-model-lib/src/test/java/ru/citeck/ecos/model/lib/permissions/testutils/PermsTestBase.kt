package ru.citeck.ecos.model.lib.permissions.testutils

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.permissions.repo.PermissionsRepo
import ru.citeck.ecos.model.lib.permissions.service.RecordPermsService
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.status.dto.StatusDef
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.dto.TypePermsDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.records2.source.dao.local.RecordsDaoBuilder
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.atts.schema.annotation.AttName
import ru.citeck.ecos.webapp.api.entity.EntityRef

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class PermsTestBase {

    protected lateinit var services: ModelServiceFactory
    protected lateinit var recordPermsService: RecordPermsService

    private var recordStatus: String? = null
    private var typePermsDef: TypePermsDef? = null

    private var typeRoles: List<String> = emptyList()
    private var typeStatuses: List<String> = emptyList()

    @BeforeEach
    fun before() {

        services = object : ModelServiceFactory() {
            override fun createTypesRepo(): TypesRepo {
                return object : TypesRepo {
                    override fun getTypeInfo(typeRef: EntityRef): TypeInfo? {
                        if (typeRef.getLocalId() == "test-type") {
                            return TypeInfo.create {
                                withId(typeRef.getLocalId())
                                withModel(
                                    TypeModelDef.create()
                                        .withRoles(typeRoles.map { RoleDef.create().withId(it).build() })
                                        .withStatuses(typeStatuses.map { StatusDef.create().withId(it).build() })
                                        .build()
                                )
                            }
                        }
                        return null
                    }
                    override fun getChildren(typeRef: EntityRef): List<EntityRef> {
                        return emptyList()
                    }
                }
            }
            override fun createPermissionsRepo(): PermissionsRepo {
                return object : PermissionsRepo {
                    override fun getPermissionsForType(typeRef: EntityRef): TypePermsDef? {
                        if (typeRef.getLocalId() == "test-type") {
                            return typePermsDef
                        }
                        return null
                    }
                }
            }
        }

        val recordsServices = RecordsServiceFactory()
        services.setRecordsServices(recordsServices)

        recordsServices.recordsServiceV1.register(
            RecordsDaoBuilder.create("test")
                .addRecord("test", TestRecord())
                .build()
        )

        recordPermsService = services.recordPermsService
    }

    fun getEntityRef(): EntityRef {
        return EntityRef.create("test", "test")
    }

    fun setRecordStatus(status: String?) {
        this.recordStatus = status
    }

    fun setTypeRoles(roles: List<String>) {
        this.typeRoles = roles
    }

    fun setTypeStatuses(statuses: List<String>) {
        this.typeStatuses = statuses
    }

    fun setPermissions(typePermsDef: TypePermsDef) {
        this.typePermsDef = typePermsDef
    }

    inner class TestRecord {

        @AttName("_type")
        fun getType(): EntityRef {
            return EntityRef.valueOf("emodel/type@test-type")
        }

        @AttName("_status")
        fun getStatus(): String? {
            return recordStatus
        }
    }
}
