package ru.sber.transport.tariff_fleet.validation;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.constant.ContractorType;
import ru.sber.transport.tariff_fleet.constant.ServiceType;
import ru.sber.transport.tariff_fleet.database.dao.RepairContractRepository;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.RepairContract;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;
import ru.sber.transport.tariff_fleet.exception.*;
import ru.sber.transport.tariff_fleet.model.PartiallyUpdateRepairContractModel;
import ru.sber.transport.tariff_fleet.service.validation.impl.RepairContractValidationServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.sber.transport.tariff_fleet.constant.ServiceType.CARGO_TRANSPORTATION;

@ExtendWith(MockitoExtension.class)
class RepairContractValidationServiceTest {

    @InjectMocks
    private RepairContractValidationServiceImpl validationService;

    @Mock
    private RepairContractRepository repairContractRepository;


    @Test
    @DisplayName("Проверка разрешений организации - успешно")
    void testValidateOrganizationPermissionValid() {
        var contract = new RepairContract();
        contract.setOrganization(new Organization(UUID.randomUUID(), 1L, "Рога и копыта", true));
        validationService.validateOrganizationPermission(contract, contract.getOrganization().getId());
    }

    @Test
    @DisplayName("Проверка разрешений организации - ошибка")
    void testValidateOrganizationPermissionNotAuthorized() {
        var contract = new RepairContract();
        var organizationRandomId = UUID.randomUUID();
        contract.setOrganization(new Organization(UUID.randomUUID(), 1L, "Копыта и рога", true));
        assertThatThrownBy(() ->
                validationService.validateOrganizationPermission(
                        contract, organizationRandomId)
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

    @ParameterizedTest
    @EnumSource(value = ContractorType.class,
            names = {"API", "AUTOSERVICE_EXTERNAL"})
    void testValidateContractorServicePointsAbsentNoException(ContractorType contractorType) {
        var contractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getServiceType), ServiceType.AUTOSERVICE)
                .set(field(Contractor::isActive), true)
                .set(field(Contractor::getContractorType), contractorType)
                .create();
        assertDoesNotThrow(() -> validationService.validateContractor(Collections.emptyList(), contractor));
    }

    @ParameterizedTest
    @EnumSource(value = ContractorType.class,
            names = {"API", "AUTOSERVICE_EXTERNAL"},
            mode = EnumSource.Mode.EXCLUDE)
    void testValidateContractorServicePointsAbsent(ContractorType contractorType) {
        var contractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getServiceType), ServiceType.AUTOSERVICE)
                .set(field(Contractor::isActive), true)
                .set(field(Contractor::getContractorType), contractorType)
                .create();
        List<ServicePointDto> servicePointDtoList = Collections.emptyList();
        assertThatExceptionOfType(ServicePointsAbsentException.class)
                .isThrownBy(() -> validationService.validateContractor(servicePointDtoList, contractor))
                .withMessage("Договор не создан. Для выбранного контрагента добавление сервисных точек обязательно");
    }

    @Test
    @DisplayName("Проверка уникальности ID подрядчика и номера договора")
    void testValidateContractorIdAndNumberUnique() {
        var organizationId = UUID.randomUUID();
        var contractorId = UUID.randomUUID();
        var contractId = UUID.randomUUID();
        String number = "ABC";
        given(repairContractRepository.existsByOrganizationIdAndContractorIdAndContract_NumberAndContract_ActiveIsTrue(any(), any(), any()))
                .willReturn(true);
        assertThatThrownBy(() -> validationService.validateContractorIdAndNumberUnique(organizationId, contractorId, number, contractId))
                .isInstanceOf(ContractAlreadyExistsException.class);
    }

    @Test
    void testValidateUpdateContract_WhenContractIsInactive_ShouldThrowContractNotActiveException() {
        var repairContract = mock(RepairContract.class);
        var contract = mock(Contract.class);
        when(repairContract.getContract()).thenReturn(contract);
        when(contract.isActive()).thenReturn(false);
        when(repairContract.getContract()).thenReturn(contract);

        assertThrows(
                ContractNotActiveException.class,
                () -> validationService.validateUpdateContract(repairContract, mock(PartiallyUpdateRepairContractModel.class))
        );
    }

    @Test
    void testValidateUpdateContract_WhenApiTypeAndServicePointsProvided_ShouldThrowWrongContractTypeForServicePointsUpdatingException() {
        var repairContract = mock(RepairContract.class);
        var contract = mock(Contract.class);
        var contractor = mock(Contractor.class);
        when(repairContract.getContract()).thenReturn(contract);
        when(contractor.getContractorType()).thenReturn(ContractorType.API);
        when(repairContract.getContractor()).thenReturn(contractor);
        when(contract.isActive()).thenReturn(true);

        var model = mock(PartiallyUpdateRepairContractModel.class);
        when(model.servicePointsOpt()).thenReturn(Optional.of(List.of()));

        assertThrows(
                WrongContractTypeForServicePointsUpdatingException.class,
                () -> validationService.validateUpdateContract(repairContract, model)
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

        var repairContract = new RepairContract();
        repairContract.setContract(contractMock);
        assertThrows(
                ContractNotActiveException.class,
                () -> validationService.validateDeactivateContract(repairContract),
                "Должно быть вызвано исключение, потому что контракт неактивен."
        );
    }

    @Test
    void testValidateDeactivateContractWhenActiveShouldDoNothing() {
        var contractMock = mock(Contract.class);
        when(contractMock.isActive()).thenReturn(true);

        var repairContract = new RepairContract();
        repairContract.setContract(contractMock);
        validationService.validateDeactivateContract(repairContract);
    }
}