package ru.sber.transport.corporate_sync.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.Department;
import ru.sber.transport.corporate.business.model.DepartmentFilter;
import ru.sber.transport.corporate.business.providers.Provider;
import ru.sber.transport.corporate.business.senders.Sender;
import ru.sber.transport.corporate.messaging.mappers.ActiveMessageMapperImpl;
import ru.sber.transport.corporate.messaging.mappers.DateMessageMapperImpl;
import ru.sber.transport.corporate_sync.cache.OrganizationsCache;
import ru.sber.transport.corporate_sync.mappers.DepartmentSyncMessageMapperImpl;
import ru.sber.transport.corporate_sync.mappers.MessageMapper;
import ru.sber.transport.messages.easup.avro.DepartmentData;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;
import static ru.sber.transport.corporate.business.model.Active.ACTIVE;
import static ru.sber.transport.corporate.business.model.Active.INACTIVE;

@SuppressWarnings("unchecked")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка синхронизации подразделений")
class BaseSyncTest {

    private final Provider<Department, DepartmentFilter> departmentProvider = mock(Provider.class);

    private final Sender<Department> sender = mock(Sender.class);

    private final OrganizationsCache cache = mock(OrganizationsCache.class);

    private final MessageMapper<Department, DepartmentData> messageMapper = new DepartmentSyncMessageMapperImpl(new DateMessageMapperImpl(), new ActiveMessageMapperImpl());

    private final BaseSync<DepartmentData> sync = new DepartmentSyncImpl(departmentProvider, messageMapper, sender) {

        @Override
        OrganizationsCache createCache() {
            return cache;
        }
    };

    @Test
    @DisplayName("Проверка синхронизации")
    void test_sync() {
        var organizationId = Instancio.create(String.class);
        var id = Instancio.create(String.class);
        var data = DepartmentData.newBuilder()
                .setUpdateDate(Instancio.create(Instant.class))
                .setCode(Instancio.create(String.class))
                .setId(Instancio.create(String.class))
                .setName(Instancio.create(String.class))
                .setOrganizationId(Instancio.create(String.class))
                .setFosType(Instancio.create(String.class))
                .setLevelCode(Instancio.create(String.class))
                .setLevelName(Instancio.create(String.class))
                .setCount(Instancio.create(Integer.class))
                .setNumber(Instancio.create(Integer.class))
                .setStartDate(Instancio.create(LocalDate.class))
                .setEndDate(Instancio.create(LocalDate.class))
                .setActive(Instancio.create(Boolean.class))
                .build();

        doNothing().when(cache).organization(organizationId);

        sync.sync(organizationId, id, data);

        var departmentCaptor = ArgumentCaptor.forClass(Department.class);

        verify(departmentProvider).saveSync(departmentCaptor.capture());

        assertThat(departmentCaptor.getValue()).isNotNull();

        assertSoftly(it -> {
           it.assertThat(departmentCaptor.getValue().getCode()).isEqualTo(data.getCode());
           it.assertThat(departmentCaptor.getValue().getName()).isEqualTo(data.getName());
           it.assertThat(departmentCaptor.getValue().getSyncId()).isEqualTo(data.getId());
           it.assertThat(departmentCaptor.getValue().getStartDate()).isEqualTo(data.getStartDate());
           it.assertThat(departmentCaptor.getValue().getStatus()).isEqualTo(data.getActive() ? ACTIVE : INACTIVE);
        });
    }

}