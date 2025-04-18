package ru.citeck.ecos.model.lib.workspace

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import ru.citeck.ecos.commons.data.DataValue
import ru.citeck.ecos.model.lib.workspace.api.WorkspaceWebApi

class WorkspaceWebApiTest {

    @Test
    fun getUserWorkspacesReqTest() {
        val data = DataValue.createObj().set("user", "admin")
        val dto = data.getAs(WorkspaceWebApi.GetUserWorkspacesReq::class.java)
        assertThat(dto!!.user).isEqualTo("admin")
    }
}
