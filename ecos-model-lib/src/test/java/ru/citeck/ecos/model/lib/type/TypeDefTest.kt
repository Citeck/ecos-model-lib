package ru.citeck.ecos.model.lib.type

import org.junit.jupiter.api.Test
import ru.citeck.ecos.model.lib.ModelServiceFactory
import ru.citeck.ecos.model.lib.type.dto.DocLibDef
import ru.citeck.ecos.model.lib.type.dto.TypeDef
import ru.citeck.ecos.model.lib.type.repo.TypesRepo
import ru.citeck.ecos.model.lib.type.service.utils.TypeUtils
import ru.citeck.ecos.records2.RecordRef
import ru.citeck.ecos.records3.RecordsServiceFactory
import kotlin.test.assertEquals

class TypeDefTest {

    @Test
    fun test() {

        val typeDef = TypeDef.create {
            id = "test"
            docLib = DocLibDef.create { withEnabled(true) }
        }

        val services = object : ModelServiceFactory() {
            override fun createTypesRepo(): TypesRepo {
                return object : TypesRepo {
                    override fun getTypeDef(typeRef: RecordRef): TypeDef? {
                        if (typeRef.id == typeDef.id) {
                            return typeDef
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

        val typeRef = TypeUtils.getTypeRef(typeDef.id)
        val docLibDef = services.typeDefService.getDocLib(typeRef)

        assertEquals(1, docLibDef.fileTypeRefs.size)
        assertEquals(typeRef, docLibDef.fileTypeRefs[0])
        assertEquals(TypeUtils.DOCLIB_DEFAULT_DIR_TYPE, docLibDef.dirTypeRef)
    }
}
