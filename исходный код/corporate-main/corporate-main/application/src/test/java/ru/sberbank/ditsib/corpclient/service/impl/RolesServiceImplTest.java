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
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.corpclient.database.dao.RolesRepository;
import ru.sberbank.ditsib.corpclient.database.model.messages.Role;
import ru.sberbank.ditsib.corpclient.service.RolesService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка сервиса работы с ролями")
@Transactional
@ActiveProfiles("test")
class RolesServiceImplTest {

    @Autowired
    private RolesService service;
    
    @Autowired
    private RolesRepository repository;

    @BeforeEach
    void setup() {
        repository.deleteAllInBatch();
    }

    @DisplayName("Сохранение")
    @Test
    void test_save() {
        var role = new Role();
        
        role.setCode("Code");
        role.setName("Name");
    
        assertThat(repository.count()).isZero();
        
        service.save(role);
        
        assertThat(repository.count()).isEqualTo(1);
        
        var actual = repository.findAll().getFirst();
        
        assertThat(actual.getCode()).isEqualTo(role.getCode());
        assertThat(actual.getName()).isEqualTo(role.getName());
    }
    
    @DisplayName("Удаление")
    @Test
    void test_delete() {
        var role = new Role();
        
        role.setCode("Code");
        role.setName("Name");
        
        repository.save(role);
    
        assertThat(repository.count()).isEqualTo(1);
        
        service.delete("Code");
        
        assertThat(repository.count()).isZero();
    }
    
    @DisplayName("Поиск по идентификатору")
    @Test
    void test_findById() {
        var role = new Role();
    
        role.setCode("Code");
        role.setName("Name");
    
        repository.save(role);
    
        assertThat(repository.count()).isEqualTo(1);
        
        var actual = service.get("Code");
        
        assertThat(actual).isPresent();
        assertThat(actual.get().getCode()).isEqualTo(role.getCode());
        assertThat(actual.get().getName()).isEqualTo(role.getName());
        assertThat(service.get("Code 2")).isNotPresent();
    }
    
    @DisplayName("Поиск по имени")
    @Test
    void test_findByName() {
        var role = new Role();
    
        role.setCode("Code");
        role.setName("Name");
    
        repository.save(role);
    
        assertThat(repository.count()).isEqualTo(1);
    
        var actual = service.getByName("Name");
    
        assertThat(actual).isPresent();
        assertThat(actual.get().getCode()).isEqualTo(role.getCode());
        assertThat(actual.get().getName()).isEqualTo(role.getName());
        assertThat(service.getByName("Name 2")).isNotPresent();
    }
    
}