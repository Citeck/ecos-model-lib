package ru.citeck.ecos.model.lib.permissions;

import org.junit.jupiter.api.Test;
import ru.citeck.ecos.model.lib.type.dto.TypePermsDef;
import ru.citeck.ecos.records2.RecordsServiceFactory;

import java.util.Map;

public class PerTest {

    @Test
    void test() {

        RecordsServiceFactory factory = new RecordsServiceFactory();
        Map<String, String> attributes = factory.getRecordsMetaService().getAttributes(TypePermsDef.Mutable.class);

        System.out.println(attributes);

        //AttributeDef attributeDef = new AttributeDef("adw", new MLText());
        //attributeDef.copy().setType(AttributeType.BOOLEAN).build();
    }

}
