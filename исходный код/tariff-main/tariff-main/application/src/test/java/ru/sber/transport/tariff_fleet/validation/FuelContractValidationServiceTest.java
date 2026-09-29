package ru.sber.transport.tariff_fleet.validation;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.constant.ContractorType;
import ru.sber.transport.tariff_fleet.constant.ServiceType;
import ru.sber.transport.tariff_fleet.database.dao.FuelContractRepository;
import ru.sber.transport.tariff_fleet.database.model.*;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;
import ru.sber.transport.tariff_fleet.exception.*;
import ru.sber.transport.tariff_fleet.model.PartiallyUpdateFuelContractModel;
import ru.sber.transport.tariff_fleet.service.tariff.TariffService;
import ru.sber.transport.tariff_fleet.service.validation.impl.FuelContractValidationServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.sber.transport.tariff_fleet.constant.ServiceType.CARGO_TRANSPORTATION;

@ExtendWith(MockitoExtension.class)
class FuelContractValidationServiceTest {

    @InjectMocks
    private FuelContractValidationServiceImpl validationService;

    @Mock
    private FuelContractRepository fuelContractRepository;
    @Mock
    private TariffService tariffService;

    @Test
    @DisplayName("Проверка разрешений организации - успешно")
    void testValidateOrganizationPermissionValid() {
        var contract = new FuelContract();
        contract.setOrganization(new Organization(UUID.randomUUID(), 1L, "Рога и копыта", true));
        validationService.validateOrganizationPermission(contract, contract.getOrganization().getId());
    }

    @Test
    @DisplayName("Проверка разрешений организации - ошибка")
    void testValidateOrganizationPermissionNotAuthorized() {
        var contract = new FuelContract();
        var organizationRandomId = UUID.randomUUID();
        contract.setOrganization(new Organization(UUID.randomUUID(), 1L, "Копыта и рога", true));
        assertThatThrownBy(() -> validationService.validateOrganizationPermission(contract, organizationRandomId)
        ).isInstanceOf(OrganizationPermissionException.class);
    }

    @Test
    @DisplayName("Проверка статуса подрядчика - активный")
    void testValidateContractorInactiveContractor() {
        var contractor = new Contractor();
        contractor.setActive(false);
        assertThatThrownBy(() -> validationService.validateContractor(null, contractor))
                .isInstanceOf(ContractorNotActiveException.class);
    }

    @Test
    void testValidateContractorServiceTypeMismatch() {
        var contractor = new Contractor();
        contractor.setServiceType(CARGO_TRANSPORTATION);
        assertThatThrownBy(() -> validationService.validateContractor(null, contractor))
                .isInstanceOf(ContractorServiceTypeMismatchException.class);
    }

    @Test
    void testValidateContractorTypeMismatchNoServicePoints() {
        var contractor = new Contractor();
        contractor.setServiceType(ServiceType.AUTOSERVICE);
        List<ServicePointDto> emptyList = List.of();
        assertThatThrownBy(() -> validationService.validateContractor(emptyList, contractor))
                .isInstanceOf(ServicePointsAbsentException.class);
    }

    @Test
    @DisplayName("Проверка уникальности ID подрядчика и номера договора")
    void testValidateContractorIdAndNumberUnique() {
        var organizationId = UUID.randomUUID();
        var contractorId = UUID.randomUUID();
        var contractId = UUID.randomUUID();
        String number = "ABC";
        given(fuelContractRepository.existsByOrganizationIdAndContractorIdAndContract_NumberAndContract_ActiveIsTrue(any(), any(), any()))
                .willReturn(true);
        assertThatThrownBy(() -> validationService.validateContractorIdAndNumberUnique(organizationId, contractorId, number, contractId))
                .isInstanceOf(ContractAlreadyExistsException.class);
    }

    @Test
    void testValidateUpdateContract_WhenContractIsInactive_ShouldThrowContractNotActiveException() {
        var fuelContract = mock(FuelContract.class);
        var contract = mock(Contract.class);
        when(fuelContract.getContract()).thenReturn(contract);
        when(contract.isActive()).thenReturn(false);
        when(fuelContract.getContract()).thenReturn(contract);

        assertThrows(
                ContractNotActiveException.class,
                () -> validationService.validateUpdateContract(fuelContract, mock(PartiallyUpdateFuelContractModel.class))
        );
    }

    @Test
    void testValidateUpdateContract_WhenApiTypeAndServicePointsProvided_ShouldThrowWrongContractTypeForServicePointsUpdatingException() {
        var fuelContract = mock(FuelContract.class);
        var contract = mock(Contract.class);
        var contractor = mock(Contractor.class);
        when(fuelContract.getContract()).thenReturn(contract);
        when(contractor.getContractorType()).thenReturn(ContractorType.API);
        when(fuelContract.getContractor()).thenReturn(contractor);
        when(contract.isActive()).thenReturn(true);

        var model = mock(PartiallyUpdateFuelContractModel.class);
        when(model.servicePointsOpt()).thenReturn(Optional.of(List.of()));

        assertThrows(
                WrongContractTypeForServicePointsUpdatingException.class,
                () -> validationService.validateUpdateContract(fuelContract, model)
        );
    }

    @Test
    void validateContractActive() {
        var contract = Instancio.of(Contract.class)
                .set(field(Contract::isActive), false)
                .create();
        assertThrows(ContractNotActiveException.class, () -> validationService.validateContractActive(contract));
    }

    @Test
    void validateContractTypeForServicePointUpdating() {
        var contractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getContractorType), ContractorType.API)
                .create();
        assertThrows(WrongContractTypeForServicePointsUpdatingException.class,
                () -> validationService.validateContractTypeForServicePointUpdating(contractor));
    }

    @Test
    void testValidateDeactivateContractWhenInactiveShouldThrowException() {
        var contractMock = mock(Contract.class);
        when(contractMock.isActive()).thenReturn(false);

        var fuelContract = new FuelContract();
        fuelContract.setContract(contractMock);
        assertThrows(
                ContractNotActiveException.class,
                () -> validationService.validateDeactivateContract(fuelContract),
                "Должно быть вызвано исключение, потому что контракт неактивен."
        );
    }

    @Test
    void testValidateDeactivateContractWhenActiveShouldDoNothing() {
        var contractMock = mock(Contract.class);
        var tariffMock = mock(Tariff.class);
        when(contractMock.isActive()).thenReturn(true);
        when(tariffMock.isActive()).thenReturn(false);
        when(tariffService.getAllByContractId(any())).thenReturn(List.of(tariffMock));

        var fuelContract = new FuelContract();
        fuelContract.setContract(contractMock);
        validationService.validateDeactivateContract(fuelContract);
    }

    @Test
    void validateDeactivateContractWithActiveTariffThrowException() {
        var contractMock = mock(Contract.class);
        var tariffMock = mock(Tariff.class);
        when(contractMock.isActive()).thenReturn(true);
        when(tariffMock.isActive()).thenReturn(true);
        when(tariffService.getAllByContractId(any())).thenReturn(List.of(tariffMock));

        var fuelContract = new FuelContract();
        fuelContract.setContract(contractMock);
        assertThrows(
                ContractNotActiveException.class,
                () -> validationService.validateDeactivateContract(fuelContract),
                "Должно быть вызвано исключение, потому что договор содержит активный тариф."
        );
    }
}
