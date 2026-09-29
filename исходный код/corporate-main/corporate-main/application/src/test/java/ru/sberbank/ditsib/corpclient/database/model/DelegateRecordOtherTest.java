package ru.sberbank.ditsib.corpclient.database.model;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.CorporateClientApplication;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;

import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CorporateClientApplication.class)
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
class DelegateRecordOtherTest extends SharedData {

    @MockitoBean
    private JwtDecoder jwtDecoder;
    
    @Test
    @DisplayName("Статус записи")
    void RecordStatus() {
        DelegateRecord delegateRecord = new DelegateRecord();
        assertEquals(RecordStatus.ACTIVE, delegateRecord.getStatus());
    }
}