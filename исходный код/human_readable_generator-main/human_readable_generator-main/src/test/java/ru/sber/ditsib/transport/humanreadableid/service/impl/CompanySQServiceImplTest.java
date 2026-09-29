package ru.sber.ditsib.transport.humanreadableid.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.humanreadableid.service.impl.CompanySQServiceImpl;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.humanreadableid.dao.AbstractRepository;
import ru.sber.transport.humanreadableid.exceptions.CannotCreateSequenceException;
import ru.sber.transport.humanreadableid.model.Prefix;
import ru.sber.transport.humanreadableid.service.SQCreator;

import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
@IsolatedTest
@Feature("lib_human_readable_generator")
@SpringBootTest(properties = "classpath:/application.yml")
@EmbeddedPostgres
@AutoConfigureMockMvc
@Import(CompanySQServiceImpl.class)
class CompanySQServiceImplTest {
    
    @Autowired
    private CompanySQServiceImpl companySQService;

    @MockitoBean
    private AbstractRepository<?> companySQRepository;

    @MockitoBean
    private SQCreator sqCreator;

    @Test
    void getNextValue_CannotCreateSequenceException() {
        assertThrows(CannotCreateSequenceException.class, () -> companySQService.getNextValue(Prefix.LD, 1L, 1));
    }
}