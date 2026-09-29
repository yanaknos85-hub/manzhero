package ru.sberbank.ditsib.corpclient.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.model.PersonalCar;
import ru.sberbank.ditsib.corpclient.service.AcceptFileService;
import ru.sberbank.ditsib.corpclient.service.PersonalCarEnrichmentService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@MockitoBean(types = JwtDecoder.class)
@Transactional
@DisplayName("Проверка сервиcа обогашений")
@ActiveProfiles("test")
class PersonalCarEnrichmentServiceImplTest{

    @Autowired
    private AcceptFileService acceptFileService;

    private PersonalCarEnrichmentService personalCarEnrichmentService;


    @BeforeEach
    void setup() {
        personalCarEnrichmentService = new PersonalCarEnrichmentServiceImpl(acceptFileService);
    }


    @Test
    @DisplayName("Проверка обогащения информацией о согласии на обработку ПД")
    void testAddUserAcceptInfo() {
        var personalCar = new PersonalCar();
        personalCarEnrichmentService.addUserAcceptInfo(personalCar);
        assertThat(personalCar.getPersDataAccept()).isNotBlank();
        assertThat(personalCar.getPersDataAccept()).isEqualTo("b721c3cb01af3df07d927c2a9b81bdcd");
    }
}