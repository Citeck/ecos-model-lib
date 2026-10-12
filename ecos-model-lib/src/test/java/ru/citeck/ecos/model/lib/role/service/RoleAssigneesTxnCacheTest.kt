package ru.citeck.ecos.model.lib.role.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.role.dto.RoleDef
import ru.citeck.ecos.model.lib.type.dto.TypeInfo
import ru.citeck.ecos.model.lib.type.dto.TypeModelDef
import ru.citeck.ecos.model.lib.type.repo.DefaultTypesRepo
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.utils.ModelUtils
import ru.citeck.ecos.records3.RecordsService
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.atts.computed.script.RecordsScriptService
import ru.citeck.ecos.records3.record.atts.value.AttValueCtx
import ru.citeck.ecos.records3.record.dao.impl.mem.InMemDataRecordsDao
import ru.citeck.ecos.records3.record.mixin.AttMixin
import ru.citeck.ecos.test.commons.EcosWebAppApiMock
import ru.citeck.ecos.txn.lib.TxnContext
import ru.citeck.ecos.txn.lib.manager.TransactionManagerImpl
import ru.citeck.ecos.webapp.api.entity.EntityRef

class RoleAssigneesTxnCacheTest {

    companion object {
        private const val SRC_ID = "test-src"
        private const val TYPE_ID = "test-type"
    }

    private lateinit var roleService: RoleService
    private lateinit var records: RecordsService
    private lateinit var scripts: RecordsScriptService

    @ParameterizedTest
    @ValueSource(strings = ["", "previous-user", "GROUP_previous-performers"])
    fun `performer role reflects a script save in the same transaction`(previous: String) {
        val api = EcosWebAppApiMock()
        val manager = TransactionManagerImpl()
        manager.init(api)
        TxnContext.setManager(manager)
        try {
            val ref = records.create(SRC_ID, mapOf("performer" to previous))
            val document = scripts.get(ref)
            TxnContext.doInTxn {
                TxnContext.doInTxn(readOnly = true) {
                    val expected = if (previous.isEmpty()) emptyList() else listOf(previous)
                    assertThat(roleService.getAssignees(ref, "performer")).isEqualTo(expected)
                }
                document.att("performer", "initiator-user")
                document.save()
                TxnContext.doInTxn(readOnly = true) {
                    assertThat(roleService.getAssignees(ref, "performer")).containsExactly("initiator-user")
                }
            }
        } finally {
            manager.shutdown()
            api.dispose()
        }
    }

    @BeforeEach
    fun beforeEach() {

        val types = mutableMapOf<String, TypeInfo>()
        val typesRepo = object : DefaultTypesRepo() {
            override fun getTypeInfo(typeRef: EntityRef): TypeInfo? {
                return types[typeRef.getLocalId()]
            }
            override fun getChildren(typeRef: EntityRef): List<EntityRef> {
                return emptyList()
            }
        }
        types[TYPE_ID] = TypeInfo.create {
            withId(TYPE_ID)
            withModel(
                TypeModelDef.create()
                    .withRoles(
                        listOf(
                            RoleDef.create()
                                .withId("performer")
                                .withAttributes(listOf("performer"))
                                .build()
                        )
                    )
                    .build()
            )
        }

        val recordsServices = RecordsServiceFactory()
        val services = object : ModelServiceFactory() {
            override fun createTypesRepo(): TypesRepo {
                return typesRepo
            }
        }
        services.setRecordsServices(recordsServices)
        val dao = InMemDataRecordsDao(SRC_ID)
        recordsServices.recordsService.register(dao)
        dao.addAttributesMixin(Mixin())

        this.roleService = services.roleService
        this.records = recordsServices.recordsService
        this.scripts = RecordsScriptService(recordsServices)
    }

    class Mixin : AttMixin {
        override fun getAtt(path: String, value: AttValueCtx): Any? {
            return ModelUtils.getTypeRef(TYPE_ID)
        }
        override fun getProvidedAtts() = listOf("_type")
    }
}
