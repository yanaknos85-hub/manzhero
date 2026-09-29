package ru.sber.transport.tariff_fleet.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.dao.TariffRepository;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.Employee;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffPostDto;
import ru.sber.transport.tariff_fleet.dto.AbstractTariffResponse;
import ru.sber.transport.tariff_fleet.dto.PageSetting;
import ru.sber.transport.tariff_fleet.dto.SearchTariffRequest;
import ru.sber.transport.tariff_fleet.dto.ewb.*;
import ru.sber.transport.tariff_fleet.dto.repair.CreateRepairTariffDto;
import ru.sber.transport.tariff_fleet.exception.DeactivateFuelCardTariffException;
import ru.sber.transport.tariff_fleet.exception.TariffNotFoundException;
import ru.sber.transport.tariff_fleet.exception.UnsupportedDocumentTypeException;
import ru.sber.transport.tariff_fleet.mapper.TariffMapper;
import ru.sber.transport.tariff_fleet.service.EmployeeService;
import ru.sber.transport.tariff_fleet.service.HumanReadableIdService;
import ru.sber.transport.tariff_fleet.service.grpc.FuelGrpcService;
import ru.sber.transport.tariff_fleet.service.tariff.EwbTariffService;
import ru.sber.transport.tariff_fleet.service.tariff.FuelTariffService;
import ru.sber.transport.tariff_fleet.service.tariff.RepairTariffService;
import ru.sber.transport.tariff_fleet.service.tariff.TariffValidationService;
import ru.sber.transport.tariff_fleet.service.tariff.impl.TariffServiceImpl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.field;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static ru.sber.transport.tariff_fleet.constant.DocumentType.EWB;
import static ru.sber.transport.tariff_fleet.constant.DocumentType.REPAIR_AND_MAINTENANCE;

@ExtendWith(MockitoExtension.class)
class TariffServiceImplTest {
    @InjectMocks
    private TariffServiceImpl tariffService;
    @Mock
    private EwbTariffService ewbTariffService;
    @Mock
    private TariffRepository tariffRepository;
    @Mock
    private TariffMapper tariffMapper;
    @Mock
    private ContractGetInternalServiceImpl contractGetInternalService;
    @Mock
    private RepairTariffService repairTariffService;
    @Mock
    private FuelTariffService fuelTariffService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private TariffValidationService tariffValidationService;
    @Mock
    private HumanReadableIdService humanReadableIdService;
    @Mock
    private FuelGrpcService fuelGrpcService;

    @Test
    void create() {
        var ewbPostTariffDto = Instancio.create(EwbTariffPostDto.class);
        var humanReadableId = "TF-0008-00000002";
        var tariff = Instancio.create(Tariff.class);
        doReturn(tariff).when(tariffMapper).abstractPostTariffDtoToTariff(any(AbstractTariffPostDto.class), anyString(), anyBoolean());
        doReturn(tariff).when(tariffRepository).save(any(Tariff.class));
        assertThat(tariffService.create(ewbPostTariffDto, humanReadableId, true))
                .usingRecursiveComparison()
                .isEqualTo(tariff);
    }

    @Test
    void search() {
        var pageSetting = new PageSetting(0, 10);
        var ewbSearchTariffDto = Instancio.of(EwbSearchTariffDto.class)
                .set(field(EwbSearchTariffDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbSearchTariffDto::getPageSetting), pageSetting)
                .create();
        var abstractSearchTariffDto = Instancio.of(EwbSearchTariffDto.class)
                .set(field(EwbSearchTariffDto::getDocumentType), DocumentType.REPAIR_AND_MAINTENANCE)
                .set(field(EwbSearchTariffDto::getPageSetting), pageSetting)
                .create();
        var expected1 = Instancio.create(EwbTariffGetDto.class);
        var expected2 = Instancio.create(EwbTariffGetDto.class);
        var pageRequest = PageRequest.of(0, 10);
        var expected = new PageImpl<>(List.of(expected1, expected2), pageRequest, 2);
        doReturn(expected).when(ewbTariffService).search(any());
        var actual1 = tariffService.search(ewbSearchTariffDto);
        assertThat(actual1)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThatThrownBy(() -> tariffService.search(abstractSearchTariffDto))
                .isInstanceOf(UnsupportedDocumentTypeException.class);
        verify(ewbTariffService).search(any(EwbSearchTariffDto.class));
    }

    @Test
    void get() {
        var tariffId = UUID.randomUUID();
        var tariff = Instancio.create(Tariff.class);
        when(tariffRepository.findById(tariffId))
                .thenReturn(Optional.of(tariff));
        var actual = tariffService.get(tariffId);
        verify(tariffRepository).findById(tariffId);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(tariff);
    }

    @Test
    void getException() {
        var tariffId = UUID.randomUUID();
        when(tariffRepository.findById(tariffId))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> tariffService.get(tariffId))
                .isInstanceOf(TariffNotFoundException.class)
                .hasMessage("Не найден тариф ID:%s", tariffId);
    }

    @Test
    void getById() {
        var tariffId = UUID.randomUUID();
        var expected = Instancio.create(EwbTariffGetByIdDto.class);

        when(tariffRepository.getTypeById(tariffId)).thenReturn(Optional.of(EWB));
        when(ewbTariffService.get(tariffId)).thenReturn(expected);

        var tariff = tariffService.getById(tariffId);

        verify(ewbTariffService).get(tariffId);
        assertThat(tariff)
                .usingRecursiveAssertion()
                .isEqualTo(expected);
    }

    @Test
    void getByIdExceptions() {
        var tariffId = UUID.randomUUID();
        assertThatThrownBy(() -> tariffService.getById(tariffId))
                .isInstanceOf(TariffNotFoundException.class);

        when(tariffRepository.getTypeById(tariffId)).thenReturn(Optional.of(REPAIR_AND_MAINTENANCE));
        assertThatThrownBy(() -> tariffService.getById(tariffId))
                .isInstanceOf(UnsupportedDocumentTypeException.class);
    }

    @Test
    void edit() {
        var tariffId = UUID.randomUUID();
        var abstractTariffPatchDto = Instancio.of(EwbTariffPatchDto.class)
                .set(field(EwbTariffPatchDto::getDocumentType), EWB)
                .create();
        doNothing().when(ewbTariffService).edit(tariffId, abstractTariffPatchDto);
        tariffService.edit(tariffId, abstractTariffPatchDto);
        verify(ewbTariffService).edit(tariffId, abstractTariffPatchDto);
    }


    @Test
    void editExceptions() {
        var tariffId = UUID.randomUUID();
        var abstractTariffPatchDto = Instancio.of(EwbTariffPatchDto.class)
                .set(field(EwbTariffPatchDto::getDocumentType), REPAIR_AND_MAINTENANCE)
                .create();
        assertThatThrownBy(() -> tariffService.edit(tariffId, abstractTariffPatchDto))
                .isInstanceOf(UnsupportedDocumentTypeException.class);
    }

    @Test
    void deactivate() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var id3 = UUID.randomUUID();
        doReturn(Optional.of(EWB)).when(tariffRepository).getTypeById(id1);
        doReturn(Optional.of(REPAIR_AND_MAINTENANCE)).when(tariffRepository).getTypeById(id2);
        doReturn(Optional.empty()).when(tariffRepository).getTypeById(id3);
        doNothing().when(ewbTariffService).deactivate(id1);
        tariffService.deactivate(id1);
        assertThatExceptionOfType(UnsupportedDocumentTypeException.class)
                .isThrownBy(() -> tariffService.deactivate(id2));
        assertThatExceptionOfType(TariffNotFoundException.class)
                .isThrownBy(() -> tariffService.deactivate(id3))
                .withMessage("Не найден тариф ID:%s", id3);
        verify(tariffRepository, times(3)).getTypeById(any(UUID.class));
        verify(ewbTariffService).deactivate(any(UUID.class));
    }

    @Test
    void createTariffSelfOrganization() {
        var userId = UUID.randomUUID();
        var humanReadableId = Instancio.create(String.class);
        var request = Instancio.of(CreateRepairTariffDto.class)
                .set(field(CreateRepairTariffDto::getDocumentType), REPAIR_AND_MAINTENANCE)
                .create();
        var contract = Instancio.of(Contract.class)
                .set(field(Contract::isActive), true)
                .create();
        var employee = Instancio.create(Employee.class);
        var tariff = Instancio.create(Tariff.class);
        var savedTariff = Instancio.create(Tariff.class);

        doReturn(Optional.of(contract)).when(contractGetInternalService).getContract(request.getContractId());
        doReturn(employee).when(employeeService).getByUserId(userId);
        doNothing().when(tariffValidationService).validateCreateTariff(contract);
        doReturn(humanReadableId).when(humanReadableIdService).createHumanReadableIdByUserId(userId);
        doReturn(tariff).when(tariffMapper).createTariffRequestToTariff(request, humanReadableId);
        doReturn(savedTariff).when(tariffRepository).save(tariff);
        doNothing().when(repairTariffService).createTariff(request,
                savedTariff,
                contract,
                employee.getOrganization().getId()
        );

        tariffService.createTariffSelfOrganization(request, userId);

        verify(contractGetInternalService).getContract(request.getContractId());
        verify(employeeService).getByUserId(userId);
        verify(tariffValidationService).validateCreateTariff(contract);
        verify(humanReadableIdService).createHumanReadableIdByUserId(userId);
        verify(tariffMapper).createTariffRequestToTariff(request, humanReadableId);
        verify(tariffRepository).save(tariff);
        verify(repairTariffService).createTariff(request, savedTariff, contract, employee.getOrganization().getId());

        verifyNoMoreInteractions(contractGetInternalService,
                employeeService,
                tariffValidationService,
                humanReadableIdService,
                tariffMapper,
                tariffRepository,
                repairTariffService,
                fuelTariffService);
    }


    @Test
    void createTariffAllOrganizations() {
        var humanReadableId = Instancio.create(String.class);
        var request = Instancio.of(CreateRepairTariffDto.class)
                .set(field(CreateRepairTariffDto::getDocumentType), REPAIR_AND_MAINTENANCE)
                .create();
        var contract = Instancio.of(Contract.class)
                .set(field(Contract::isActive), true)
                .create();
        var organization = Instancio.create(Organization.class);
        var tariff = Instancio.create(Tariff.class);
        var savedTariff = Instancio.create(Tariff.class);

        doReturn(Optional.of(contract)).when(contractGetInternalService).getContract(request.getContractId());
        doNothing().when(tariffValidationService).validateCreateTariff(contract);
        doReturn(organization).when(repairTariffService).getOrganizationByContract(request.getContractId());
        doReturn(humanReadableId).when(humanReadableIdService).createHumanReadableIdByDigitId(organization.getDigitId());
        doReturn(tariff).when(tariffMapper).createTariffRequestToTariff(request, humanReadableId);
        doReturn(savedTariff).when(tariffRepository).save(tariff);
        doNothing().when(repairTariffService).createTariff(request,
                savedTariff,
                contract,
                organization.getId()
        );

        tariffService.createTariffAllOrganizations(request);

        verify(contractGetInternalService).getContract(request.getContractId());
        verify(tariffValidationService).validateCreateTariff(contract);
        verify(repairTariffService).getOrganizationByContract(request.getContractId());
        verify(humanReadableIdService).createHumanReadableIdByDigitId(organization.getDigitId());
        verify(tariffMapper).createTariffRequestToTariff(request, humanReadableId);
        verify(tariffRepository).save(tariff);
        verify(repairTariffService).createTariff(request, savedTariff, contract, organization.getId());

        verifyNoMoreInteractions(contractGetInternalService,
                employeeService,
                tariffValidationService,
                humanReadableIdService,
                tariffMapper,
                tariffRepository,
                repairTariffService,
                fuelTariffService);
    }

    @Test
    void testGetTariffSelfOrganization() {
        var userId = UUID.randomUUID();
        var tariffId = UUID.randomUUID();
        var employee = mock(Employee.class);
        var organizationId = UUID.randomUUID();
        Organization organization = mock(Organization.class);
        when(employeeService.getByUserId(userId)).thenReturn(employee);
        when(employee.getOrganization()).thenReturn(organization);
        when(organization.getId()).thenReturn(organizationId);
        when(tariffRepository.getTypeById(tariffId)).thenReturn(Optional.of(DocumentType.REPAIR_AND_MAINTENANCE));
        when(repairTariffService.getTariff(tariffId, organization.getId())).thenReturn(mock(AbstractTariffResponse.class));
        AbstractTariffResponse result = tariffService.getTariffSelfOrganization(tariffId, userId);
        assertNotNull(result);
        verify(repairTariffService).getTariff(tariffId, organization.getId());
    }

    @Test
    void testGetTariffAllOrganizations() {
        var tariffId = UUID.randomUUID();
        doReturn(Optional.of(DocumentType.FUEL)).when(tariffRepository).getTypeById(tariffId);
        doReturn(mock(AbstractTariffResponse.class)).when(fuelTariffService).getTariff(tariffId);
        var result = tariffService.getTariffAllOrganizations(tariffId);
        assertNotNull(result);
        verify(fuelTariffService).getTariff(tariffId);
    }

    @Test
    void testSearchTariffAllOrganizationsRepairAndMaintenance() {
        var request = createValidRequest(DocumentType.REPAIR_AND_MAINTENANCE);
        doReturn(Page.empty()).when(repairTariffService).searchTariff(any(), any());
        assertThat(tariffService.searchTariffAllOrganizations(request)).isEmpty();
    }

    @Test
    void testSearchTariffAllOrganizationsFuel() {
        var request = createValidRequest(DocumentType.FUEL);
        doReturn(Page.empty()).when(fuelTariffService).searchTariff(any(), any());
        assertThat(tariffService.searchTariffAllOrganizations(request)).isEmpty();
    }

    @Test
    void testSearchTariffSelfOrganizationRepairAndMaintenance() {
        var request = createValidRequest(DocumentType.REPAIR_AND_MAINTENANCE);
        var userId = UUID.randomUUID();
        var user = new Employee();
        var organization = new Organization();
        organization.setId(UUID.randomUUID());
        user.setOrganization(organization);

        doReturn(user).when(employeeService).getByUserId(userId);
        doReturn(Page.empty()).when(repairTariffService).searchTariff(any(), any());

        assertThat(tariffService.searchTariffSelfOrganization(request, userId)).isEmpty(); // Ожидаемый пустой результат
    }

    @Test
    void testSearchTariffSelfOrganizationFuel() {
        var request = createValidRequest(DocumentType.FUEL);
        var userId = UUID.randomUUID();
        var user = new Employee();
        var organization = new Organization();
        organization.setId(UUID.randomUUID());
        user.setOrganization(organization);

        doReturn(user).when(employeeService).getByUserId(userId);
        doReturn(Page.empty()).when(fuelTariffService).searchTariff(any(), any());

        assertThat(tariffService.searchTariffSelfOrganization(request, userId)).isEmpty();
    }

    private SearchTariffRequest createValidRequest(DocumentType type) {
        SearchTariffRequest request = new SearchTariffRequest();
        request.setDocumentType(type);
        request.setPageSetting(new SearchTariffRequest.PageSetting());
        return request;
    }

    @Test
    void testGetTariffUnsupportedDocumentType() {
        var id = UUID.randomUUID();
        doReturn(Optional.of(DocumentType.ELECTRIC_FUEL)).when(tariffRepository).getTypeById(id);
        assertThrows(UnsupportedDocumentTypeException.class, () -> tariffService.getTariffAllOrganizations(id));
    }

    @Test
    void testGetTariffSelfUnsupportedDocumentType() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        doReturn(mock(Employee.class)).when(employeeService).getByUserId(userId);
        doReturn(Optional.of(DocumentType.ELECTRIC_FUEL)).when(tariffRepository).getTypeById(id);
        assertThrows(UnsupportedDocumentTypeException.class, () -> tariffService.getTariffSelfOrganization(id, userId));
    }


    @Test
    void testDeactivateTariffUnsupportedDocumentType() {
        var tariff = mock(Tariff.class);
        var id = UUID.randomUUID();
        when(tariff.getId()).thenReturn(id);
        when(tariffRepository.findById(tariff.getId())).thenReturn(Optional.of(tariff));
        doNothing().when(tariffValidationService).validateDeactivateTariff(tariff);
        doReturn(Optional.of(DocumentType.ELECTRIC_FUEL)).when(tariffRepository).getTypeById(id);
        assertThrows(UnsupportedDocumentTypeException.class, () -> tariffService.deactivateTariffAllOrganizations(id));
    }

    @Test
    void testDeactivateTariffByContractIdFuelCardException() {
        var id = UUID.randomUUID();
        doThrow(DeactivateFuelCardTariffException.class).when(fuelGrpcService).deactivateFuelCardByContractId(id);

        assertThrows(DeactivateFuelCardTariffException.class, () -> tariffService.deactivateByContractId(id));
    }

    @Test
    void testSearchTariffUnsupportedDocumentType() {
        var request = mock(SearchTariffRequest.class);
        when(request.getDocumentType()).thenReturn(DocumentType.EWB);
        assertThrows(UnsupportedDocumentTypeException.class, () -> tariffService.searchTariffAllOrganizations(request));
    }

    @Test
    void testSearchTariffSelfUnsupportedDocumentType() {
        var request = mock(SearchTariffRequest.class);
        var userId = UUID.randomUUID();
        doReturn(mock(Employee.class)).when(employeeService).getByUserId(userId);
        when(request.getDocumentType()).thenReturn(DocumentType.EWB);
        assertThrows(UnsupportedDocumentTypeException.class, () -> tariffService.searchTariffSelfOrganization(request, userId));
    }

}