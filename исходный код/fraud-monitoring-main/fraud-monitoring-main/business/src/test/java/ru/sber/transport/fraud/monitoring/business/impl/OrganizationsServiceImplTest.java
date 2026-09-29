package ru.sber.transport.fraud.monitoring.business.impl;

import io.qameta.allure.Feature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.fraud.monitoring.business.model.TestOrganization;
import ru.sber.transport.fraud.monitoring.providers.OrganizationsDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.OrganizationsGrpcProvider;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;


@Slf4j
@UnitTest
@IsolatedTest
@Isolated
@Feature("app_platform_fraud_monitoring")
@DisplayName("Проверка сервиса организаций")
class OrganizationsServiceImplTest {
    private final OrganizationsDatabaseProvider organizationsDatabaseProvider = mock(OrganizationsDatabaseProvider.class);
    private final OrganizationsGrpcProvider organizationsGrpcProvider = mock(OrganizationsGrpcProvider.class);

    private final OrganizationsServiceImpl organizationsService = new OrganizationsServiceImpl(organizationsDatabaseProvider, organizationsGrpcProvider);

    @Test
    @DisplayName("Проверка сохранения организации")
    void test_createOrUpdate() {
        final var organizationId = UUID.randomUUID();

        final var source = new TestOrganization(organizationId, 1L);

        when(organizationsDatabaseProvider.createOrUpdate(source)).thenReturn(source);

        final var result = organizationsService.createOrUpdate(source);

        assertThat(result).isEqualTo(source);

        verify(organizationsDatabaseProvider).createOrUpdate(source);
    }

    @Test
    @DisplayName("getExistedOrCreate возвращает существующую организацию из БД")
    void test_getExistedOrCreate_returnsExistingOrganization() {
        final var organizationId = UUID.randomUUID();
        final var existingOrganization = new TestOrganization(organizationId, 1L);

        when(organizationsDatabaseProvider.get(organizationId)).thenReturn(existingOrganization);

        final var result = organizationsService.getExistedOrCreate(organizationId);

        assertThat(result).isEqualTo(existingOrganization);
        verify(organizationsDatabaseProvider).get(organizationId);
        verify(organizationsGrpcProvider, never()).get(any(UUID.class));
    }

    @Test
    @DisplayName("getExistedOrCreate возвращает организацию по gRPC, если не найдена в БД")
    void test_getExistedOrCreate_handlesNullFromDatabase() {
        final var organizationId = UUID.randomUUID();
        final var grpcOrganization = new TestOrganization(organizationId, 2L);

        when(organizationsDatabaseProvider.get(organizationId)).thenReturn(null);
        when(organizationsGrpcProvider.get(organizationId))
                .thenReturn(grpcOrganization);
        when(organizationsDatabaseProvider.createOrUpdate(grpcOrganization))
                .thenReturn(grpcOrganization);

        final var result = organizationsService.getExistedOrCreate(organizationId);

        assertThat(result).isEqualTo(grpcOrganization);
        verify(organizationsDatabaseProvider).get(organizationId);
        verify(organizationsGrpcProvider).get(organizationId);
        verify(organizationsDatabaseProvider).createOrUpdate(grpcOrganization);
    }
}