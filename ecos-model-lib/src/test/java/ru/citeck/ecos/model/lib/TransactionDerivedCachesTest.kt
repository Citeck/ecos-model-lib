package ru.citeck.ecos.model.lib

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import ru.citeck.ecos.commons.data.ObjectData
import ru.citeck.ecos.commons.json.Json
import ru.citeck.ecos.context.lib.auth.AuthContext
import ru.citeck.ecos.model.lib.computed.ComputeDmnDecisionResponseDto
import ru.citeck.ecos.model.lib.computed.ComputeDmnDecisionWebApi
import ru.citeck.ecos.model.lib.role.service.ComputedRoleProcessor
import ru.citeck.ecos.model.lib.workspace.USER_WORKSPACE_PREFIX
import ru.citeck.ecos.model.lib.workspace.WorkspaceService
import ru.citeck.ecos.model.lib.workspace.api.WorkspaceApi
import ru.citeck.ecos.model.lib.workspace.api.WsMembershipType
import ru.citeck.ecos.records3.RecordsService
import ru.citeck.ecos.records3.RecordsServiceFactory
import ru.citeck.ecos.records3.record.dao.impl.mem.InMemDataRecordsDao
import ru.citeck.ecos.test.commons.EcosWebAppApiMock
import ru.citeck.ecos.txn.lib.TxnContext
import ru.citeck.ecos.txn.lib.manager.TransactionManagerImpl
import ru.citeck.ecos.webapp.api.EcosWebAppApi
import ru.citeck.ecos.webapp.api.constants.AppName
import ru.citeck.ecos.webapp.api.entity.EntityRef
import java.lang.reflect.Proxy
import java.util.concurrent.atomic.AtomicInteger

class TransactionDerivedCachesTest {

    private lateinit var api: EcosWebAppApiMock
    private lateinit var manager: TransactionManagerImpl
    private lateinit var records: RecordsService
    private lateinit var processor: ComputedRoleProcessor
    private lateinit var modelServices: ModelServiceFactory
    private lateinit var workspaces: WorkspaceService
    private lateinit var decision: EntityRef
    private lateinit var record: EntityRef
    private val dmnCalls = AtomicInteger()
    private val workspaceCalls = AtomicInteger()
    private val memberships = HashMap<Pair<String, WsMembershipType>, Set<String>>()

    @BeforeEach
    fun setUp() {
        api = EcosWebAppApiMock()
        manager = TransactionManagerImpl()
        manager.init(api)
        TxnContext.setManager(manager)
        api.webClientExecuteImpl = { app, path, body ->
            assertThat(app).isEqualTo(AppName.EPROC)
            assertThat(path).isEqualTo(ComputeDmnDecisionWebApi.PATH)
            dmnCalls.incrementAndGet()
            val request = Json.mapper.readNotNull(body as ByteArray, ObjectData::class.java)
            ComputeDmnDecisionResponseDto(mapOf("assignee" to listOf(request["variables"]["performer"].asText())))
        }
        val recordServices = object : RecordsServiceFactory() {
            override fun getEcosWebAppApi(): EcosWebAppApi = api
        }
        val services = object : ModelServiceFactory() {
            override fun getEcosWebAppApi(): EcosWebAppApi = api
        }
        modelServices = services
        services.setRecordsServices(recordServices)
        services.setWorkspaceApi(object : WorkspaceApi {
            override fun getNestedWorkspaces(workspaces: Collection<String>): List<Set<String>> =
                workspaces.map { emptySet() }
            override fun getUserWorkspaces(user: String, membershipType: WsMembershipType): Set<String> {
                workspaceCalls.incrementAndGet()
                return memberships[user to membershipType]?.toSet() ?: emptySet()
            }
            override fun isUserManagerOf(user: String, workspace: String): Boolean = false
            override fun mapIdentifiers(identifiers: List<String>, mappingType: WorkspaceApi.IdMappingType): List<String> =
                identifiers
        })
        records = recordServices.recordsService
        records.register(InMemDataRecordsDao("dmn"))
        records.register(InMemDataRecordsDao("test"))
        decision = records.create("dmn", mapOf("definition" to mapOf("model" to mapOf("performer" to "performer?str"))))
        record = records.create("test", mapOf("performer" to "before"))
        processor = ComputedRoleProcessor(services)
        workspaces = services.workspaceService
    }

    @AfterEach
    fun tearDown() {
        manager.shutdown()
        api.dispose()
    }

    @Test
    fun testDmnReusesUnchangedInputsInReadOnlyTransaction() {
        TxnContext.doInTxn(readOnly = true) {
            assertThat(processor.computeRoleAssigneesFromDmn(decision, record)).containsExactly("before")
            assertThat(processor.computeRoleAssigneesFromDmn(decision, record)).containsExactly("before")
        }
        assertThat(dmnCalls.get()).isEqualTo(1)
    }

    @Test
    fun testDmnSnapshotSurvivesWritableScopes() {
        TxnContext.doInTxn {
            TxnContext.doInTxn(readOnly = true) {
                assertThat(processor.computeRoleAssigneesFromDmn(decision, record)).containsExactly("before")
            }
            records.mutateAtt(record, "performer", "after")
            TxnContext.doInTxn(readOnly = true) {
                assertThat(processor.computeRoleAssigneesFromDmn(decision, record)).containsExactly("before")
            }
        }
        TxnContext.doInTxn(readOnly = true) {
            assertThat(processor.computeRoleAssigneesFromDmn(decision, record)).containsExactly("after")
        }
        assertThat(dmnCalls.get()).isEqualTo(2)
    }

    @Test
    fun testDmnReusesResultsInWritableTransaction() {
        TxnContext.doInTxn {
            assertThat(processor.computeRoleAssigneesFromDmn(decision, record)).containsExactly("before")
            records.mutateAtt(record, "performer", "after")
            assertThat(processor.computeRoleAssigneesFromDmn(decision, record)).containsExactly("before")
        }
        assertThat(dmnCalls.get()).isEqualTo(1)
    }

    @Test
    fun testDmnKeepsFirstResultForSameRecordObject() {
        val input = MutableInputRecord("before")
        TxnContext.doInTxn(readOnly = true) {
            assertThat(processor.computeRoleAssigneesFromDmn(decision, input)).containsExactly("before")
            input.performer = "after"
            assertThat(processor.computeRoleAssigneesFromDmn(decision, input)).containsExactly("before")
        }
        assertThat(dmnCalls.get()).isEqualTo(1)
    }

    @Test
    fun testDmnWithoutTransactionRecomputesResults() {
        assertThat(processor.computeRoleAssigneesFromDmn(decision, record)).containsExactly("before")
        assertThat(processor.computeRoleAssigneesFromDmn(decision, record)).containsExactly("before")
        assertThat(dmnCalls.get()).isEqualTo(2)
    }

    @ParameterizedTest
    @EnumSource(WsMembershipType::class)
    fun testMembershipSnapshotSurvivesWritableScopes(type: WsMembershipType) {
        AuthContext.runAsSystem {
            TxnContext.doInTxn {
                memberships["alice" to type] = setOf("old")
                TxnContext.doInTxn(readOnly = true) {
                    assertThat(workspaces.getUserWorkspaces("alice", type)).isEqualTo(expectedMembership(type, "old"))
                }
                memberships["alice" to type] = setOf("new")
                TxnContext.doInTxn(readOnly = true) {
                    assertThat(workspaces.getUserWorkspaces("alice", type)).isEqualTo(expectedMembership(type, "old"))
                }
            }
            TxnContext.doInTxn(readOnly = true) {
                assertThat(workspaces.getUserWorkspaces("alice", type)).isEqualTo(expectedMembership(type, "new"))
            }
        }
        assertThat(workspaceCalls.get()).isEqualTo(2)
    }

    @Test
    fun testMembershipReusesResultsInWritableTransaction() {
        AuthContext.runAsSystem {
            TxnContext.doInTxn {
                memberships["alice" to WsMembershipType.ALL] = setOf("old")
                assertThat(workspaces.getUserWorkspaces("alice")).contains("old")
                memberships["alice" to WsMembershipType.ALL] = setOf("new")
                assertThat(workspaces.getUserWorkspaces("alice")).contains("old").doesNotContain("new")
            }
        }
        assertThat(workspaceCalls.get()).isEqualTo(1)
    }

    @Test
    fun testMembershipCacheSeparatesUsersAndMembershipTypes() {
        AuthContext.runAsSystem {
            TxnContext.doInTxn(readOnly = true) {
                memberships["alice" to WsMembershipType.ALL] = setOf("alice-all")
                memberships["alice" to WsMembershipType.DIRECT] = setOf("alice-direct")
                memberships["bob" to WsMembershipType.ALL] = setOf("bob-all")
                repeat(2) {
                    assertThat(workspaces.getUserWorkspaces("alice")).containsExactlyInAnyOrder("alice-all", "${USER_WORKSPACE_PREFIX}alice")
                    assertThat(workspaces.getUserWorkspaces("alice", WsMembershipType.DIRECT)).containsExactly("alice-direct")
                    assertThat(workspaces.getUserWorkspaces("bob")).containsExactlyInAnyOrder("bob-all", "${USER_WORKSPACE_PREFIX}bob")
                }
            }
        }
        assertThat(workspaceCalls.get()).isEqualTo(3)
    }

    @Test
    fun testCachedMembershipDoesNotBypassCurrentUserCheck() {
        TxnContext.doInTxn(readOnly = true) {
            memberships["alice" to WsMembershipType.ALL] = setOf("private")
            AuthContext.runAsSystem {
                assertThat(workspaces.getUserWorkspaces("alice")).contains("private")
            }
            AuthContext.runAs("bob") {
                assertThat(workspaces.getUserWorkspaces("alice")).isEmpty()
            }
        }
        assertThat(workspaceCalls.get()).isEqualTo(1)
    }

    @Test
    fun testMembershipWithoutTransactionRecomputesResults() {
        AuthContext.runAsSystem {
            memberships["alice" to WsMembershipType.ALL] = setOf("old")
            assertThat(workspaces.getUserWorkspaces("alice")).contains("old")
            memberships["alice" to WsMembershipType.ALL] = setOf("new")
            assertThat(workspaces.getUserWorkspaces("alice")).contains("new").doesNotContain("old")
        }
        assertThat(workspaceCalls.get()).isEqualTo(2)
    }

    @Test
    fun testDmnCacheHitDoesNotLoadDecisionModelOrRecordInputs() {
        val inputReads = AtomicInteger()
        val countedRecords = Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(RecordsService::class.java)
        ) { _, method, args ->
            if (method.name == "getAtt" || method.name == "getAtts") {
                inputReads.incrementAndGet()
            }
            method.invoke(records, *(args ?: emptyArray()))
        } as RecordsService
        val countedProcessor = ComputedRoleProcessor(modelServices, recordsService = countedRecords)
        TxnContext.doInTxn {
            assertThat(countedProcessor.computeRoleAssigneesFromDmn(decision, record)).containsExactly("before")
            val readsAfterFirstCall = inputReads.get()
            assertThat(readsAfterFirstCall).isGreaterThan(0)
            assertThat(countedProcessor.computeRoleAssigneesFromDmn(decision, record)).containsExactly("before")
            assertThat(inputReads.get()).isEqualTo(readsAfterFirstCall)
        }
        assertThat(dmnCalls.get()).isEqualTo(1)
    }

    private fun expectedMembership(type: WsMembershipType, workspace: String): Set<String> =
        if (type == WsMembershipType.ALL) setOf(workspace, "${USER_WORKSPACE_PREFIX}alice") else setOf(workspace)

    class MutableInputRecord(var performer: String) {
        override fun toString(): String = "same-record"
    }
}
