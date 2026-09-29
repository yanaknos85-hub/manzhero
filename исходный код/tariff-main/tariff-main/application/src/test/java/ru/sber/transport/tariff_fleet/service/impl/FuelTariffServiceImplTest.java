package ru.sber.transport.tariff_fleet.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.constant.ActivationType;
import ru.sber.transport.tariff_fleet.database.dao.FuelTariffRepository;
import ru.sber.transport.tariff_fleet.database.dao.TariffRepository;
import ru.sber.transport.tariff_fleet.database.model.*;
import ru.sber.transport.tariff_fleet.dto.fuel.CreateFuelTariffDto;
import ru.sber.transport.tariff_fleet.dto.fuel.FuelTariffDto;
import ru.sber.transport.tariff_fleet.exception.DeactivateFuelCardTariffException;
import ru.sber.transport.tariff_fleet.exception.DepartmentNotActiveException;
import ru.sber.transport.tariff_fleet.exception.TariffNotFoundException;
import ru.sber.transport.tariff_fleet.mapper.TariffMapper;
import ru.sber.transport.tariff_fleet.messaging.sender.FuelTariffSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.FuelTariffMessage;
import ru.sber.transport.tariff_fleet.service.DepartmentService;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sber.transport.tariff_fleet.service.SubContractInternalService;
import ru.sber.transport.tariff_fleet.service.grpc.impl.FuelGrpcServiceImpl;
import ru.sber.transport.tariff_fleet.service.tariff.FuelTariffService;
import ru.sber.transport.tariff_fleet.service.tariff.TariffValidationService;
import ru.sber.transport.tariff_fleet.service.tariff.impl.FuelTariffServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuelTariffServiceImplTest {
    @Mock
    private SubContractInternalService<FuelContract> fuelContractInternalService;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private TariffValidationService tariffValidationService;

    @Mock
    private FuelTariffRepository fuelTariffRepository;

    @Mock
    private FuelTariffSender sender;

    @Mock
    private TariffRepository tariffRepository;

    @Mock
    private TariffMapper tariffMapper;

    @Mock
    private FuelGrpcServiceImpl fuelGrpcServiceImpl;

    private FuelTariffService service;

    @BeforeEach
    void setUp() {
        service = new FuelTariffServiceImpl(
                organizationService,
                departmentService,
                tariffValidationService,
                fuelTariffRepository,
                tariffMapper,
                tariffRepository,
                fuelContractInternalService,
                sender,
                fuelGrpcServiceImpl
        );
    }

    @Test
    void testCreateTariffSuccess() {
        var request = mock(CreateFuelTariffDto.class);
        var department = mock(Department.class);
        var tariff = mock(Tariff.class);
        var contract = mock(Contract.class);
        var orgId = UUID.randomUUID();
        var fuelContract = mock(FuelContract.class);
        doReturn(mock(Organization.class)).when(fuelContract).getOrganization();
        doReturn(fuelContract).when(fuelContractInternalService).getContract(any());

        when(departmentService.get(any())).thenReturn(Optional.of(department));
        when(fuelTariffRepository.findFirstByTariff_ContractIdAndDepartmentIdAndTariff_ActiveIsTrue(any(), any())).thenReturn(null);
        when(tariffMapper.createFuelTariffRequestToFuelTariff(request, tariff, department, contract, orgId)).thenReturn(mock(FuelTariff.class));

        service.createTariff(request, tariff, contract, orgId);

        verify(fuelTariffRepository, times(1)).save(any());
    }

    @Test
    void testCreateTariffFailureDueToExistingActiveTariff() {
        var request = mock(CreateFuelTariffDto.class);
        var tariff = mock(Tariff.class);
        var contract = mock(Contract.class);
        var orgId = UUID.randomUUID();

        assertThrows(DepartmentNotActiveException.class, () -> service.createTariff(request, tariff, contract, orgId));
    }

    @Test
    void testGetTariffSuccess() {
        var tariffId = UUID.randomUUID();
        var orgId = UUID.randomUUID();

        var fuelTariff = mock(FuelTariff.class);
        var fuelContract = mock(FuelContract.class);
        var organization = mock(Organization.class);
        var department = mock(Department.class);
        var tariff = mock(Tariff.class);

        when(fuelTariffRepository.findById(tariffId)).thenReturn(Optional.of(fuelTariff));
        when(fuelContractInternalService.getContract(any())).thenReturn(fuelContract);
        when(departmentService.get(any())).thenReturn(Optional.of(department));
        doReturn(tariff).when(fuelTariff).getTariff();
        doReturn(UUID.randomUUID()).when(tariff).getContractId();
        when(organizationService.get(fuelTariff.getOrganizationId())).thenReturn(Optional.of(organization));
        when(organization.getOfficialName()).thenReturn("Офиц. наименование");
        when(tariffMapper.fuelTariffToFuelTariffDto(fuelTariff, fuelContract, "Офиц. наименование", department)).thenReturn(mock(FuelTariffDto.class));

        var result = service.getTariff(tariffId, orgId);

        assertNotNull(result);
    }

    @Test
    void testGetTariffFailureWhenTariffNotFound() {
        var tariffId = UUID.randomUUID();
        var orgId = UUID.randomUUID();

        when(fuelTariffRepository.findById(tariffId)).thenReturn(Optional.empty());
        assertThrows(TariffNotFoundException.class, () -> service.getTariff(tariffId, orgId));
    }

    @Test
    void testDeactivateTariffSuccess() {
        var tariffId = UUID.randomUUID();
        var orgId = UUID.randomUUID();

        var fuelTariff = mock(FuelTariff.class);
        var tariff = mock(Tariff.class);

        when(fuelTariffRepository.findById(tariffId)).thenReturn(Optional.of(fuelTariff));
        when(fuelTariff.getTariff()).thenReturn(tariff);
        doNothing().when(fuelGrpcServiceImpl).deactivateFuelCardByContractAndDepartmentId(any(), any());

        service.deactivateTariff(tariffId, orgId);

        assertFalse(tariff.isActive());
        verify(fuelTariffRepository, times(1)).save(fuelTariff);
    }

    @Test
    void testDeactivateTariffFailureWhenTariffNotFound() {
        var tariffId = UUID.randomUUID();
        var orgId = UUID.randomUUID();

        when(fuelTariffRepository.findById(tariffId)).thenReturn(Optional.empty());

        assertThrows(TariffNotFoundException.class, () -> service.deactivateTariff(tariffId, orgId));
    }

    @Test
    void testDeactivateTariffFailureWhenFuelCardDeactivationFailed() {
        var tariffId = UUID.randomUUID();
        var orgId = UUID.randomUUID();

        var fuelTariff = mock(FuelTariff.class);
        var tariff = mock(Tariff.class);

        when(fuelTariffRepository.findById(tariffId)).thenReturn(Optional.of(fuelTariff));
        when(fuelTariff.getTariff()).thenReturn(tariff);
        doThrow(DeactivateFuelCardTariffException.class).when(fuelGrpcServiceImpl).deactivateFuelCardByContractAndDepartmentId(any(), any());

        assertThrows(DeactivateFuelCardTariffException.class, () -> service.deactivateTariff(tariffId, orgId));
    }

    @Test
    void testDeactivateTariffByTariffIdFailureWhenFuelCardDeactivationFailed() {
        var tariffId = UUID.randomUUID();

        var fuelTariff = mock(FuelTariff.class);
        var tariff = mock(Tariff.class);

        when(fuelTariffRepository.findById(tariffId)).thenReturn(Optional.of(fuelTariff));
        when(fuelTariff.getTariff()).thenReturn(tariff);
        doThrow(DeactivateFuelCardTariffException.class).when(fuelGrpcServiceImpl).deactivateFuelCardByContractAndDepartmentId(any(), any());

        assertThrows(DeactivateFuelCardTariffException.class, () -> service.deactivateTariff(tariffId));
    }

    @Test
    void activateByContractId() {
        var fuelTariff = Instancio.create(FuelTariff.class);
        var id = fuelTariff.getTariff().getContractId();
        var message = Instancio.create(FuelTariffMessage.class);

        doReturn(List.of(fuelTariff)).when(fuelTariffRepository)
                .findAllByTariff_ContractIdAndTariff_ActivationTypeNot(id, ActivationType.MANUAL);
        doReturn(message).when(tariffMapper).toMessage(fuelTariff);

        service.activateByContractId(id);
        verify(tariffRepository).activateByContractId(id);
        verify(sender).send(message);
    }

    @Test
    void deactivateByContractId() {
        var fuelTariff = Instancio.create(FuelTariff.class);
        var id = fuelTariff.getTariff().getContractId();
        var message = Instancio.create(FuelTariffMessage.class);

        doReturn(List.of(fuelTariff)).when(fuelTariffRepository).findAllByTariff_ContractId(id);
        doReturn(message).when(tariffMapper).toMessage(fuelTariff);

        service.deactivateByContractId(id);

        verify(tariffRepository).deactivateByContractId(id);
        verify(sender).send(message);
    }

    @Test
    void getOrganizationByContract() {
        var contractId = UUID.randomUUID();
        var contract = Instancio.create(FuelContract.class);

        doReturn(contract).when(fuelContractInternalService).getContract(contractId);

        assertThat(service.getOrganizationByContract(contractId)).usingRecursiveComparison()
                .isEqualTo(contract.getOrganization());
    }
}
