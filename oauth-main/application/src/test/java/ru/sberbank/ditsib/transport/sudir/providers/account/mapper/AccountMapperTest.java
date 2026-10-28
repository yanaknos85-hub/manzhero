package ru.sberbank.ditsib.transport.sudir.providers.account.mapper;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.jooq.JSON;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.database.sudir.tables.records.AccountRecord;
import ru.sber.transport.database.sudir.tables.records.RoleRecord;

import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_oauth")
@DisplayName("Проверка маппера")
class AccountMapperTest {

    private final AccountMapper accountMapper = new AccountMapperImpl();

    @Test
    @DisplayName("Конвертация в бизнес-объект")
    void test_toBusiness() {
        var source = new AccountRecord();
        source.setId(UUID.randomUUID());
        source.setPersonnelNumber(Instancio.create(String.class));
        source.setActive(Instancio.create(Boolean.class));

        var roles = new HashSet<RoleRecord>();

        for (var i = 0; i < 100; i++) {
            var role = new RoleRecord();
            role.setCode("Code " + i);
            role.setName("Name " + i);
            role.setDescription("Description " + i);
            role.setDataMaster(i % 2 == 0);
            role.setDefaultFor(JSON.valueOf("[]"));

            roles.add(role);
        }

        var actual = accountMapper.toBusiness(source, roles);

        assertThat(actual.getId()).isEqualTo(source.getId());
        assertThat(actual.getPersonnelNumber()).isEqualTo(source.getPersonnelNumber());
        assertThat(actual.isActive()).isEqualTo(source.getActive());
        assertThat(actual.getRoles().keySet()).hasSameElementsAs(roles.stream().map(RoleRecord::getCode).collect(Collectors.toSet()));
    }

}