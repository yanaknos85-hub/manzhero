package ru.sber.transport.tariff_fleet.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.database.model.*;
import ru.sber.transport.tariff_fleet.exception.*;
import ru.sber.transport.tariff_fleet.service.tariff.impl.TariffValidationServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TariffValidationServiceTest {
    @InjectMocks
    private TariffValidationServiceImpl tariffValidationService;

    @Test
    void testValidateCreateTariffActiveContractSuccessful() {
        var activeContract = mock(Contract.class);
        when(activeContract.isActive()).thenReturn(true);
        tariffValidationService.validateCreateTariff(activeContract);
    }

    @Test
    void testValidateCreateTariffInactiveContractExceptionThrown() {
        var inactiveContract = mock(Contract.class);
        when(inactiveContract.isActive()).thenReturn(false);
        when(inactiveContract.getId()).thenReturn(UUID.randomUUID());

        assertThrows(ContractNotActiveException.class, () -> tariffValidationService.validateCreateTariff(inactiveContract));
    }


    @Test
    void testValidateCreateTariff_ActiveContract_Successful() {
        var activeContract = mock(Contract.class);
        when(activeContract.isActive()).thenReturn(true);
        tariffValidationService.validateCreateTariff(activeContract);
    }

    @Test
    void testValidateCreateTariff_InactiveContract_ExceptionThrown() {
        var inactiveContract = mock(Contract.class);
        when(inactiveContract.isActive()).thenReturn(false);
        when(inactiveContract.getId()).thenReturn(UUID.randomUUID());
        assertThrows(ContractNotActiveException.class, () -> tariffValidationService.validateCreateTariff(inactiveContract));
    }

    @Test
    void testValidateDeactivateTariff_ActiveTariff_Successful() {
        var activeTariff = mock(Tariff.class);
        when(activeTariff.isActive()).thenReturn(true);
        tariffValidationService.validateDeactivateTariff(activeTariff);
    }

    @Test
    void testValidateDeactivateTariff_InactiveTariff_ExceptionThrown() {
        var inactiveTariff = mock(Tariff.class);
        when(inactiveTariff.isActive()).thenReturn(false);
        when(inactiveTariff.getId()).thenReturn(UUID.randomUUID());
        assertThrows(TariffNotActiveException.class, () -> tariffValidationService.validateDeactivateTariff(inactiveTariff));
    }

    @Test
    void testValidateOrganizationPermission_MatchingOrganizations_Successful() {
        var matchingOrgId = UUID.randomUUID();
        var tariffId = UUID.randomUUID();
        tariffValidationService.validateOrganizationPermission(tariffId, matchingOrgId, matchingOrgId);
    }

    @Test
    void testValidateOrganizationPermission_DifferentOrganizations_ExceptionThrown() {
        var differentOrgIds = new UUID[]{UUID.randomUUID(), UUID.randomUUID()};
        var tariffId = UUID.randomUUID();
        assertThrows(OrganizationPermissionException.class, () -> tariffValidationService.validateOrganizationPermission(tariffId, differentOrgIds[0], differentOrgIds[1]));
    }

    @Test
    void testValidateCreateRepairTariff_NoExistingTariff_Successful() {
        Optional<RepairTariff> emptyOptional = Optional.empty();
        tariffValidationService.validateCreateRepairTariff(emptyOptional);
    }

    @Test
    void testValidateCreateRepairTariff_ExistingTariff_ExceptionThrown() {
        var existingTariff = mock(RepairTariff.class);
        var mockedTariff = mock(Tariff.class);
        when(existingTariff.getTariff()).thenReturn(mockedTariff);
        when(mockedTariff.getId()).thenReturn(UUID.randomUUID());
        when(mockedTariff.getContractId()).thenReturn(UUID.randomUUID());
        var presentOptional = Optional.of(existingTariff);
        assertThrows(TariffAlreadyExistedException.class, () -> tariffValidationService.validateCreateRepairTariff(presentOptional));
    }

    @Test
    void testValidateCreateFuelTariff_AllConditionsMet_Successful() {
        var orgId = UUID.randomUUID();
        Optional<FuelTariff> emptyOptional = Optional.empty();
        var activeDepartment = mock(Department.class);
        when(activeDepartment.isActive()).thenReturn(true);
        when(activeDepartment.getOrganizationId()).thenReturn(orgId);
        var contract = mock(Contract.class);
        var organization = mock(Organization.class);
        when(organization.getId()).thenReturn(orgId);
        tariffValidationService.validateCreateFuelTariff(emptyOptional, activeDepartment, contract, organization);
    }

    @Test
    void testValidateCreateFuelTariff_ExistingTariff_ExceptionThrown() {
        var existingTariff = mock(FuelTariff.class);
        var mockedTariff = mock(Tariff.class);
        when(existingTariff.getTariff()).thenReturn(mockedTariff);
        when(mockedTariff.getId()).thenReturn(UUID.randomUUID());
        when(mockedTariff.getContractId()).thenReturn(UUID.randomUUID());
        var dept = mock(Department.class);
        when(dept.getId()).thenReturn(UUID.randomUUID());
        var presentOptional = Optional.of(existingTariff);
        assertThrows(TariffAlreadyExistedException.class, () -> tariffValidationService.validateCreateFuelTariff(presentOptional, dept, null, null));
    }

    @Test
    void testValidateCreateFuelTariff_DepartmentInactive_ExceptionThrown() {
        Optional<FuelTariff> emptyOptional = Optional.empty();
        var inactiveDept = mock(Department.class);
        when(inactiveDept.isActive()).thenReturn(false);
        when(inactiveDept.getId()).thenReturn(UUID.randomUUID());
        assertThrows(DepartmentNotActiveException.class, () -> tariffValidationService.validateCreateFuelTariff(emptyOptional, inactiveDept, null, null));
    }

    @Test
    void testValidateCreateFuelTariff_DepartmentDifferentOrganization_ExceptionThrown() {
        Optional<FuelTariff> emptyOptional = Optional.empty();
        var dept = mock(Department.class);
        when(dept.isActive()).thenReturn(true);
        when(dept.getOrganizationId()).thenReturn(UUID.randomUUID());
        var contract = mock(Contract.class);
        var diffOrgId = UUID.randomUUID();
        var organization = mock(Organization.class);
        when(organization.getId()).thenReturn(diffOrgId);
        assertThrows(DepartmentIsNotAssignedToContractOrganizationException.class, () -> tariffValidationService.validateCreateFuelTariff(emptyOptional, dept, contract, organization));
    }
}
