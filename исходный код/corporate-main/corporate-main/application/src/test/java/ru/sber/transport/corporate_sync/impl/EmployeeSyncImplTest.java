package ru.sber.transport.corporate_sync.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.corporate.business.model.*;
import ru.sber.transport.corporate.business.providers.EmployeeProvider;
import ru.sber.transport.corporate.business.senders.Sender;
import ru.sber.transport.corporate.messaging.mappers.ActiveMessageMapperImpl;
import ru.sber.transport.corporate_sync.cache.OrganizationsCache;
import ru.sber.transport.corporate_sync.mappers.EmployeeSyncMessageMapperImpl;
import ru.sber.transport.messages.easup.avro.EmployeeData;
import ru.sber.transport.messages.easup.avro.Gender;
import ru.sber.transport.messages.easup.avro.ItinerantType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("ALL")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@DisplayName("Проверка синхронизации сотрудников")
class EmployeeSyncImplTest {

    private final EmployeeProvider provider = mock(EmployeeProvider.class);

    private final Sender<Employee> sender = mock(Sender.class);

    private final BaseSync<EmployeeData> sync = new EmployeeSyncImpl(provider, new EmployeeSyncMessageMapperImpl(new ActiveMessageMapperImpl()), sender);

    @Test
    @DisplayName("Синхронизация")
    void test_sync() {
        var cache = mock(OrganizationsCache.class);
        var data = EmployeeData.newBuilder()
                .setLastName(Instancio.create(String.class))
                .setPersonnelNumber(Instancio.create(String.class))
                .setOrganizationId(Instancio.create(String.class))
                .setDepartmentId(Instancio.create(String.class))
                .setPositionId(Instancio.create(String.class))
                .setFirstName(Instancio.create(String.class))
                .setLastName(Instancio.create(String.class))
                .setGender(Instancio.create(Gender.class))
                .setUpdateDate(Instancio.create(Instant.class))
                .setItinerantType(Instancio.create(ItinerantType.class))
                .setHireDate(Instancio.create(LocalDate.class))
                .setConsent(Instancio.create(Boolean.class))
                .setActive(Instancio.create(Boolean.class))
                .build();

        var position = Instancio.create(Position.class);
        var department = Instancio.create(Department.class);

        when(cache.get(Position.class, PositionFilter.class, data.getPositionId())).thenReturn(Optional.of(position));
        when(cache.get(Department.class, DepartmentFilter.class, data.getDepartmentId())).thenReturn(Optional.of(department));
        when(provider.save(any())).then(it -> it.getArgument(0));

        sync.doSync(cache, data);

        var captor = ArgumentCaptor.forClass(Employee.class);
        verify(provider).saveSync(captor.capture());

        assertThat(captor.getValue().getDepartmentId()).isEqualTo(department.getId());
        assertThat(captor.getValue().getPositionId()).isEqualTo(position.getId());
        assertThat(captor.getValue().getLastName()).isEqualTo(data.getLastName());
        assertThat(captor.getValue().getFirstName()).isEqualTo(data.getFirstName());
        assertThat(captor.getValue().getPersonnelNumber()).isEqualTo(data.getPersonnelNumber());
    }

}