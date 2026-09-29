package ru.sber.transport.tariff_fleet.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.database.dao.RepairTariffRepository;
import ru.sber.transport.tariff_fleet.database.model.*;
import ru.sber.transport.tariff_fleet.dto.repair.CreateRepairTariffDto;
import ru.sber.transport.tariff_fleet.dto.repair.RepairTariffDto;
import ru.sber.transport.tariff_fleet.exception.TariffAlreadyExistedException;
import ru.sber.transport.tariff_fleet.exception.TariffNotFoundException;
import ru.sber.transport.tariff_fleet.mapper.RepairTariffMapper;
import ru.sber.transport.tariff_fleet.mapper.TariffMapper;
import ru.sber.transport.tariff_fleet.messaging.sender.RepairTariffSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.RepairTariffMessage;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sber.transport.tariff_fleet.service.SubContractInternalService;
import ru.sber.transport.tariff_fleet.service.tariff.RepairTariffService;
import ru.sber.transport.tariff_fleet.service.tariff.TariffValidationService;
import ru.sber.transport.tariff_fleet.service.tariff.impl.RepairTariffServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairTariffServiceImplTest {

    @Mock
    private SubContractInternalService<RepairContract> repairContractInternalService;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private TariffValidationService tariffValidationService;

    @Mock
    private RepairTariffRepository repairTariffRepository;

    @Mock
    private TariffMapper tariffMapper;

    @Mock
    private RepairTariffMapper repairTariffMapper;

    @Mock
    private RepairTariffSender sender;

    private RepairTariffService service;

    @BeforeEach
    void setUp() {
        service = new RepairTariffServiceImpl(
                repairContractInternalService,
                organizationService,
                tariffValidationService,
                repairTariffRepository,
                tariffMapper,
                repairTariffMapper,
                sender
        );
    }

    @Test
    void testCreateTariffSuccess() {
        var request = mock(CreateRepairTariffDto.class);
        var tariff = mock(Tariff.class);
        var contract = mock(Contract.class);
        var repairTariff = mock(RepairTariff.class);
        var orgId = UUID.randomUUID();
        var message = mock(RepairTariffMessage.class);

        when(repairTariffRepository.findFirstByTariff_ContractIdAndTariff_ActiveIsTrue(any())).thenReturn(null);
        when(tariffMapper.createRepairTariffRequestToRepairTariff(request, tariff, contract, orgId)).thenReturn(repairTariff);
        doReturn(message).when(repairTariffMapper).toRepairTariffMessage(repairTariff, true);

        service.createTariff(request, tariff, contract, orgId);

        verify(repairTariffRepository, times(1)).save(any());
        verify(tariffValidationService, times(1)).validateCreateRepairTariff(any());
        verify(sender).send(message);
    }

    @Test
    void testCreateTariffFailureDueToExistingActiveTariff() {
        var request = mock(CreateRepairTariffDto.class);
        var tariff = mock(Tariff.class);
        var contract = mock(Contract.class);
        var orgId = UUID.randomUUID();
        doThrow(TariffAlreadyExistedException.class).when(tariffValidationService).validateCreateRepairTariff(any());
        when(repairTariffRepository.findFirstByTariff_ContractIdAndTariff_ActiveIsTrue(any())).thenReturn(Optional.of(mock(RepairTariff.class)));

        assertThrows(TariffAlreadyExistedException.class, () -> service.createTariff(request, tariff, contract, orgId));
    }

    @Test
    void testGetTariffSuccess() {
        var tariffId = UUID.randomUUID();
        var orgId = UUID.randomUUID();

        var repairTariff = mock(RepairTariff.class);
        var repairContract = mock(RepairContract.class);
        var organization = mock(Organization.class);
        var tariff = mock(Tariff.class);

        when(repairTariffRepository.findById(tariffId)).thenReturn(Optional.of(repairTariff));
        when(repairContractInternalService.getContract(any())).thenReturn(repairContract);
        doReturn(tariff).when(repairTariff).getTariff();
        doReturn(UUID.randomUUID()).when(tariff).getContractId();
        when(organizationService.get(repairTariff.getOrganizationId())).thenReturn(Optional.of(organization));
        when(organization.getOfficialName()).thenReturn("Офиц. наименование");
        when(tariffMapper.repairTariffToRepairTariffDto(repairTariff, repairContract, "Офиц. наименование")).thenReturn(mock(RepairTariffDto.class));

        var result = service.getTariff(tariffId, orgId);

        assertNotNull(result);
    }

    @Test
    void testGetTariffFailureWhenTariffNotFound() {
        var tariffId = UUID.randomUUID();
        var orgId = UUID.randomUUID();

        when(repairTariffRepository.findById(tariffId)).thenReturn(Optional.empty());
        assertThrows(TariffNotFoundException.class, () -> service.getTariff(tariffId, orgId));
    }

    @Test
    void testDeactivateTariffSuccess() {
        var tariffId = UUID.randomUUID();
        var orgId = UUID.randomUUID();

        var repairTariff = mock(RepairTariff.class);
        var tariff = mock(Tariff.class);

        var message = mock(RepairTariffMessage.class);

        when(repairTariffRepository.findById(tariffId)).thenReturn(Optional.of(repairTariff));
        when(repairTariff.getTariff()).thenReturn(tariff);
        doReturn(message).when(repairTariffMapper).toRepairTariffMessage(repairTariff, false);

        service.deactivateTariff(tariffId, orgId);

        assertFalse(tariff.isActive());
        verify(repairTariffRepository, times(1)).save(repairTariff);
        verify(sender).send(message);
    }

    @Test
    void testDeactivateTariffFailureWhenTariffNotFound() {
        var tariffId = UUID.randomUUID();
        var orgId = UUID.randomUUID();

        when(repairTariffRepository.findById(tariffId)).thenReturn(Optional.empty());

        assertThrows(TariffNotFoundException.class, () -> service.deactivateTariff(tariffId, orgId));
    }

    @Test
    void getOrganizationByContract() {
        var contractId = UUID.randomUUID();
        var contract = Instancio.create(RepairContract.class);

        doReturn(contract).when(repairContractInternalService).getContract(contractId);

        assertThat(service.getOrganizationByContract(contractId)).usingRecursiveComparison()
                .isEqualTo(contract.getOrganization());
    }
}