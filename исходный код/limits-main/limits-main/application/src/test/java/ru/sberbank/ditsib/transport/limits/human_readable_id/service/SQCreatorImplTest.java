package ru.sberbank.ditsib.transport.limits.human_readable_id.service;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.humanreadableid.model.Prefix;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SQCreatorImplTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;
    
    @Autowired
    @Qualifier("sQGeneratorLimits")
    SQGenerator sqGenerator;
    
    @Test
    void getNextId() {
        assertEquals("LU-0001-00000001", sqGenerator.getNextId(Prefix.LU, 1L));
        assertEquals("LU-0001-00000002", sqGenerator.getNextId(Prefix.LU, 1L));
        assertEquals("LU-0002-00000001", sqGenerator.getNextId(Prefix.LU, 2L));
        
        assertEquals("LD-0001-00000001", sqGenerator.getNextId(Prefix.LD, 1L));
        assertEquals("LD-0001-00000002", sqGenerator.getNextId(Prefix.LD, 1L));
        assertEquals("LD-0002-00000001", sqGenerator.getNextId(Prefix.LD, 2L));
        
    }
    
}