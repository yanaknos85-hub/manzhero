package ru.sber.ditsib.transport.humanreadableid.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.humanreadableid.service.impl.CompanySQServiceImpl;
import ru.sber.transport.humanreadableid.service.impl.HumanReadableIdFormatterImpl;
import ru.sber.transport.humanreadableid.service.impl.SQGeneratorImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.humanreadableid.model.Prefix;
import ru.sber.transport.humanreadableid.service.SQGenerator;

import jakarta.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@IsolatedTest
@Feature("lib_human_readable_generator")
@Transactional
@SpringBootTest(properties = "classpath:/application.yml")
@EmbeddedPostgres
@AutoConfigureMockMvc
@Import({SQGeneratorImpl.class, CompanySQServiceImpl.class, HumanReadableIdFormatterImpl.class})
@DisplayName("Проверка генерирования id")
class SQGeneratorImplTest {
    
    @Autowired
    SQGenerator sqGenerator;
    
    @Test
    void getNextId() {
        assertEquals("US-0001-00000001", sqGenerator.getNextId(Prefix.US, 1L));
    }
    
    
    @Test
    void getNextId_corporateClientId_Prefix_NULL() {
        assertThrows(ConstraintViolationException.class, () -> sqGenerator.getNextId(null,
                                                                                     9999L));
    }
    
    
}