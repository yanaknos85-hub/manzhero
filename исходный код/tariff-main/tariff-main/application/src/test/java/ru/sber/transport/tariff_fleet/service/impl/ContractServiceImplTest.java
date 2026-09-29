package ru.sber.transport.tariff_fleet.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.dao.ContractRepository;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.Employee;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.dto.*;
import ru.sber.transport.tariff_fleet.dto.ewb.*;
import ru.sber.transport.tariff_fleet.dto.fuel.FuelContractPostAllOrganizationsDto;
import ru.sber.transport.tariff_fleet.dto.fuel.FuelContractPostSelfOrganizationDto;
import ru.sber.transport.tariff_fleet.dto.repair.*;
import ru.sber.transport.tariff_fleet.exception.ContractNotActiveException;
import ru.sber.transport.tariff_fleet.exception.ContractNotFoundException;
import ru.sber.transport.tariff_fleet.exception.UnexpectedDocumentTypeValidationException;
import ru.sber.transport.tariff_fleet.exception.UnsupportedDocumentTypeException;
import ru.sber.transport.tariff_fleet.mapper.ContractMapper;
import ru.sber.transport.tariff_fleet.service.*;
import ru.sber.transport.tariff_fleet.service.tariff.TariffService;
import ru.sber.transport.tariff_fleet.service.validation.ContractValidationService;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.sber.transport.tariff_fleet.constant.DocumentType.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса по работе с договорами")
class ContractServiceImplTest {
    
    private final Clock fixedClock = Clock.fixed(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusMinutes(20)
                                                              .toInstant(ZoneOffset.UTC), ZoneId.of(ZoneOffset.UTC.getId()));
    @InjectMocks
    private ContractServiceImpl contractService;
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private ContractMapper contractMapper;
    @Mock
    private ContractValidationService contractValidationService;
    @Mock
    private RepairContractService repairContractService;
    @Mock
    private FuelContractService fuelContractService;
    @Mock
    private EwbContractService ewbContractService;
    @Mock
    private EmployeeService employeeService;
    @Mock
    private TariffService tariffService;
    @Mock
    private ContractGetInternalService contractGetInternalService;
    @Captor
    private ArgumentCaptor<Contract> contractArgumentCaptor;
    @Captor
    private ArgumentCaptor<AbstractContractPostDto> postDtoArgumentCaptor;
    @Captor
    private ArgumentCaptor<Boolean> isActiveArgumentCaptor;
    @Mock
    private Clock clock;
    
    @Test
    void create() {
        var dateRange1 = Instancio.of(DateRange.class)
                                  .set(field(DateRange::start), LocalDate.now(fixedClock).plusDays(1))
                                  .set(field(DateRange::end), LocalDate.now(fixedClock).plusYears(1))
                                  .create();
        var dateRange2 = Instancio.of(DateRange.class)
                                  .set(field(DateRange::start), LocalDate.now(fixedClock).minusMonths(1))
                                  .set(field(DateRange::end), LocalDate.now(fixedClock).minusDays(1))
                                  .create();
        var ewbContractPostDto1 = Instancio.of(EwbContractPostDto.class)
                                           .set(field(EwbContractPostDto::getDocumentType), EWB)
                                           .set(field(EwbContractPostDto::getPeriod), dateRange1)
                                           .create();
        var ewbContractPostDto2 = Instancio.of(EwbContractPostDto.class)
                                           .set(field(EwbContractPostDto::getDocumentType), EWB)
                                           .set(field(EwbContractPostDto::getPeriod), dateRange2)
                                           .create();
        var fuelContractPostDto3 = Instancio.of(EwbContractPostDto.class)
                                            .set(field(EwbContractPostDto::getDocumentType), FUEL)
                                            .set(field(EwbContractPostDto::getPeriod), dateRange2)
                                            .create();
        var contract = Instancio.of(Contract.class).create();
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        doNothing().when(contractValidationService).validateUvhdUnique(nullable(String.class));
        doNothing().when(contractValidationService).validateDateRange(any(LocalDate.class), any(LocalDate.class));
        doNothing().when(ewbContractService).create(any(EwbContractPostDto.class), any(Contract.class));
        doReturn(contract).when(contractMapper).abstractPostContractDtoToContract(ewbContractPostDto1, false);
        doReturn(contract).when(contractMapper).abstractPostContractDtoToContract(ewbContractPostDto2, false);
        doReturn(contract).when(contractRepository).save(contractArgumentCaptor.capture());
        contractService.create(ewbContractPostDto1);
        contractService.create(ewbContractPostDto2);
        var result = contractArgumentCaptor.getAllValues();
        assertThat(result).hasSize(2);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .isEqualTo(contract);
        assertThat(result.get(1))
                .usingRecursiveComparison()
                .isEqualTo(contract);
        assertThrows(UnsupportedDocumentTypeException.class, () -> contractService.create(fuelContractPostDto3));
    }
    
    @Test
    void createWhenStartIsToday() {
        var dateRange = Instancio.of(DateRange.class)
                                 .set(field(DateRange::start), LocalDate.now(fixedClock))
                                 .set(field(DateRange::end), LocalDate.now(fixedClock).plusYears(1))
                                 .create();
        var repairContractPostDto = Instancio.of(EwbContractPostDto.class)
                                             .set(field(EwbContractPostDto::getDocumentType), EWB)
                                             .set(field(EwbContractPostDto::getPeriod), dateRange)
                                             .create();
        var contract = Instancio.create(Contract.class);
        
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doNothing().when(contractValidationService).validateUvhdUnique(nullable(String.class));
        doNothing().when(contractValidationService).validateDateRange(any(LocalDate.class), any(LocalDate.class));
        doNothing().when(ewbContractService).create(any(EwbContractPostDto.class), any(Contract.class));
        doReturn(contract).when(contractRepository).save(contractArgumentCaptor.capture());
        doReturn(contract).when(contractMapper).abstractPostContractDtoToContract(postDtoArgumentCaptor.capture(), isActiveArgumentCaptor.capture());
        contractService.create(repairContractPostDto);
        
        assertThat(postDtoArgumentCaptor.getValue().getPeriod().start()).isEqualTo(dateRange.start());
        assertThat(isActiveArgumentCaptor.getValue()).isTrue();
    }
    
    @Test
    void createWhenStartIsAfterToday() {
        var dateRange = Instancio.of(DateRange.class)
                                 .set(field(DateRange::start), LocalDate.now(fixedClock).plusDays(10))
                                 .set(field(DateRange::end), LocalDate.now(fixedClock).plusYears(1))
                                 .create();
        var repairContractPostDto = Instancio.of(EwbContractPostDto.class)
                                             .set(field(EwbContractPostDto::getDocumentType), EWB)
                                             .set(field(EwbContractPostDto::getPeriod), dateRange)
                                             .create();
        var contract = Instancio.create(Contract.class);
        
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doNothing().when(contractValidationService).validateUvhdUnique(nullable(String.class));
        doNothing().when(contractValidationService).validateDateRange(any(LocalDate.class), any(LocalDate.class));
        doNothing().when(ewbContractService).create(any(EwbContractPostDto.class), any(Contract.class));
        doReturn(contract).when(contractMapper).abstractPostContractDtoToContract(postDtoArgumentCaptor.capture(), isActiveArgumentCaptor.capture());
        doReturn(contract).when(contractRepository).save(contractArgumentCaptor.capture());
        contractService.create(repairContractPostDto);
        
        assertThat(postDtoArgumentCaptor.getValue().getPeriod().start()).isEqualTo(dateRange.start());
        assertThat(isActiveArgumentCaptor.getValue()).isFalse();
    }

    @Test
    void createWhenEndIsToday() {
        var dateRange = Instancio.of(DateRange.class)
                .set(field(DateRange::start), LocalDate.now(fixedClock).minusDays(5))
                .set(field(DateRange::end), LocalDate.now(fixedClock))
                .create();
        var repairContractPostDto = Instancio.of(EwbContractPostDto.class)
                .set(field(EwbContractPostDto::getDocumentType), EWB)
                .set(field(EwbContractPostDto::getPeriod), dateRange)
                .create();
        var contract = Instancio.create(Contract.class);

        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doNothing().when(contractValidationService).validateUvhdUnique(nullable(String.class));
        doNothing().when(contractValidationService).validateDateRange(any(LocalDate.class), any(LocalDate.class));
        doNothing().when(ewbContractService).create(any(EwbContractPostDto.class), any(Contract.class));
        doReturn(contract).when(contractRepository).save(contractArgumentCaptor.capture());
        doReturn(contract).when(contractMapper).abstractPostContractDtoToContract(postDtoArgumentCaptor.capture(), isActiveArgumentCaptor.capture());
        contractService.create(repairContractPostDto);

        assertThat(postDtoArgumentCaptor.getValue().getPeriod().start()).isEqualTo(dateRange.start());
        assertThat(isActiveArgumentCaptor.getValue()).isTrue();
    }
    
    @Test
    void createAllOrganizations() {
        var repairContractPostDto = Instancio.of(RepairContractPostAllOrganizationsDto.class)
                                             .set(field(RepairContractPostAllOrganizationsDto::getDocumentType), REPAIR_AND_MAINTENANCE)
                                             .create();
        var fuelContractPostDto = Instancio.of(FuelContractPostAllOrganizationsDto.class)
                                           .set(field(FuelContractPostAllOrganizationsDto::getDocumentType), FUEL)
                                           .create();
        var ewbContractPostDto = Instancio.of(RepairContractPostAllOrganizationsDto.class)
                                          .set(field(RepairContractPostAllOrganizationsDto::getDocumentType), EWB)
                                          .create();
        var contract = Instancio.create(Contract.class);
        
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        doNothing().when(contractValidationService).validateDateRange(any(LocalDate.class), any(LocalDate.class));
        doReturn(contract).when(contractMapper)
                          .abstractContractPostAllOrganizationsDtoToContract(any(AbstractContractPostAllOrganizationsDto.class), anyBoolean());
        
        doNothing().when(repairContractService).createAllOrganizations(repairContractPostDto, contract);
        contractService.createAllOrganizations(repairContractPostDto);
        verify(contractValidationService).validateDateRange(repairContractPostDto.getStart(), repairContractPostDto.getEnd());
        
        doNothing().when(fuelContractService).createAllOrganizations(fuelContractPostDto, contract);
        contractService.createAllOrganizations(fuelContractPostDto);
        verify(contractValidationService).validateDateRange(fuelContractPostDto.getStart(), fuelContractPostDto.getEnd());
        
        verify(contractRepository, times(2)).save(contract);
        assertThrows(UnsupportedDocumentTypeException.class, () -> contractService.createAllOrganizations(ewbContractPostDto));
    }
    
    @Test
    void createSelfOrganizations() {
        var repairContractPostSelfOrganizationDto = Instancio.of(RepairContractPostSelfOrganizationDto.class)
                                                             .set(field(RepairContractPostSelfOrganizationDto::getDocumentType),
                                                                  REPAIR_AND_MAINTENANCE)
                                                             .create();
        var fuelContractPostSelfOrganizationDto = Instancio.of(FuelContractPostSelfOrganizationDto.class)
                                                           .set(field(FuelContractPostSelfOrganizationDto::getDocumentType), FUEL)
                                                           .create();
        var ewbContractPostSelfOrganizationDto = Instancio.of(FuelContractPostSelfOrganizationDto.class)
                                                          .set(field(FuelContractPostSelfOrganizationDto::getDocumentType), EWB)
                                                          .create();
        var contract = Instancio.create(Contract.class);
        var userid = UUID.randomUUID();
        var employee = Instancio.of(Employee.class)
                                .set(field(Employee::getOrganization), Instancio.create(Organization.class))
                                .create();
        
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(employee).when(employeeService).getByUserId(userid);
        doNothing().when(contractValidationService).validateDateRange(any(LocalDate.class), any(LocalDate.class));
        doReturn(contract).when(contractMapper)
                          .abstractContractPostSelfOrganizationDtoToContract(any(AbstractContractPostSelfOrganizationDto.class), anyBoolean());
        
        doNothing().when(repairContractService)
                   .createSelfOrganization(repairContractPostSelfOrganizationDto, contract, employee.getOrganization().getId());
        contractService.createSelfOrganization(repairContractPostSelfOrganizationDto, userid);
        verify(contractValidationService).validateDateRange(repairContractPostSelfOrganizationDto.getStart(),
                                                            repairContractPostSelfOrganizationDto.getEnd());
        
        doNothing().when(fuelContractService)
                   .createSelfOrganization(fuelContractPostSelfOrganizationDto, contract, employee.getOrganization().getId());
        contractService.createSelfOrganization(fuelContractPostSelfOrganizationDto, userid);
        verify(contractValidationService).validateDateRange(fuelContractPostSelfOrganizationDto.getStart(),
                                                            fuelContractPostSelfOrganizationDto.getEnd());
        
        verify(contractRepository, times(2)).save(contract);
        assertThrows(UnsupportedDocumentTypeException.class,
                     () -> contractService.createSelfOrganization(ewbContractPostSelfOrganizationDto, userid));
    }
    
    @Test
    void ewbSearch() {
        var ewbSearchContractDto1 = Instancio.of(EwbSearchContractDto.class)
                                             .set(field(EwbSearchContractDto::getDocumentType), EWB)
                                             .set(field(EwbSearchContractDto::getPeriod),
                                                  new DateRange(LocalDate.now(), LocalDate.now().plusYears(1)))
                                             .create();
        var ewbSearchContractDto2 = Instancio.of(EwbSearchContractDto.class)
                                             .set(field(EwbSearchContractDto::getDocumentType), EWB)
                                             .set(field(EwbSearchContractDto::getPeriod), new DateRange(LocalDate.now(), null))
                                             .create();
        var expected1 = Instancio.create(EwbContractGetDto.class);
        var expected2 = Instancio.create(EwbContractGetDto.class);
        var pageRequest = PageRequest.of(0, 10);
        var expected = new PageImpl<>(List.of(expected1, expected2), pageRequest, 2);
        doReturn(LocalDate.now())
                .when(contractValidationService).validateStartAndGet(ewbSearchContractDto1.getPeriod(), ewbSearchContractDto1.getActive());
        doReturn(LocalDate.now())
                .when(contractValidationService).validateStartAndGet(ewbSearchContractDto2.getPeriod(), ewbSearchContractDto2.getActive());
        doReturn(LocalDate.now())
                .when(contractValidationService).validateEndAndGet(ewbSearchContractDto1.getPeriod(), ewbSearchContractDto1.getActive());
        doReturn(null)
                .when(contractValidationService).validateEndAndGet(ewbSearchContractDto2.getPeriod(), ewbSearchContractDto2.getActive());
        doNothing().when(contractValidationService).validateDateRange(any(LocalDate.class), any(LocalDate.class));
        doReturn(expected).when(ewbContractService).search(any(), any(), any());
        var actual1 = contractService.search(ewbSearchContractDto1);
        assertThat(actual1)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        var actual2 = contractService.search(ewbSearchContractDto2);
        assertThat(actual2)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        verify(contractValidationService, times(2)).validateStartAndGet(any(DateRange.class), any(Boolean.class));
        verify(contractValidationService, times(2)).validateEndAndGet(any(DateRange.class), any(Boolean.class));
        verify(contractValidationService, times(1)).validateDateRange(any(LocalDate.class), any(LocalDate.class));
        verify(ewbContractService).search(any(EwbSearchContractDto.class), any(LocalDate.class), any(LocalDate.class));
    }
    
    @Test
    void repairSearch() {
        var repairSearchContractDto1 = Instancio.of(RepairSearchContractDto.class)
                                                .set(field(RepairSearchContractDto::getDocumentType), REPAIR_AND_MAINTENANCE)
                                                .set(field(RepairSearchContractDto::getPeriod),
                                                     new DateRange(LocalDate.now(), LocalDate.now().plusYears(1)))
                                                .create();
        var repairSearchContractDto2 = Instancio.of(RepairSearchContractDto.class)
                                                .set(field(RepairSearchContractDto::getDocumentType), REPAIR_AND_MAINTENANCE)
                                                .set(field(RepairSearchContractDto::getPeriod), new DateRange(LocalDate.now(), null))
                                                .create();
        var expected1 = Instancio.create(RepairContractGetDto.class);
        var expected2 = Instancio.create(RepairContractGetDto.class);
        var pageRequest = PageRequest.of(0, 10);
        var expected = new PageImpl<>(List.of(expected1, expected2), pageRequest, 2);
        doReturn(LocalDate.now())
                .when(contractValidationService).validateStartAndGet(repairSearchContractDto1.getPeriod(), repairSearchContractDto1.getActive());
        doReturn(LocalDate.now())
                .when(contractValidationService).validateStartAndGet(repairSearchContractDto2.getPeriod(), repairSearchContractDto2.getActive());
        doReturn(LocalDate.now())
                .when(contractValidationService).validateEndAndGet(repairSearchContractDto1.getPeriod(), repairSearchContractDto1.getActive());
        doReturn(null)
                .when(contractValidationService).validateEndAndGet(repairSearchContractDto2.getPeriod(), repairSearchContractDto2.getActive());
        doNothing().when(contractValidationService).validateDateRange(any(LocalDate.class), any(LocalDate.class));
        doReturn(expected).when(repairContractService).search(any(), any(), any());
        var actual1 = contractService.search(repairSearchContractDto1);
        assertThat(actual1)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        var actual2 = contractService.search(repairSearchContractDto2);
        assertThat(actual2)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        verify(contractValidationService, times(2)).validateStartAndGet(any(DateRange.class), any(Boolean.class));
        verify(contractValidationService, times(2)).validateEndAndGet(any(DateRange.class), any(Boolean.class));
        verify(contractValidationService, times(1)).validateDateRange(any(LocalDate.class), any(LocalDate.class));
        verify(repairContractService).search(any(RepairSearchContractDto.class), any(LocalDate.class), any(LocalDate.class));
    }
    
    @Test
    void repairSearchAllOrganizations() {
        var searchContractDto = Instancio.of(RepairSearchContractAllOrganizationsDto.class)
                                         .set(field(RepairSearchContractAllOrganizationsDto::getDocumentType), REPAIR_AND_MAINTENANCE)
                                         .create();
        var expected = new PageImpl<>(List.of(Instancio.create(RepairContractGetAllOrganizationsDto.class)));
        doNothing().when(contractValidationService).validateDateRange(searchContractDto.getStart(), searchContractDto.getEnd());
        doReturn(expected).when(repairContractService).searchAllOrganizations(searchContractDto);
        
        var actual = contractService.searchAllOrganizations(searchContractDto);
        
        verify(contractValidationService).validateDateRange(searchContractDto.getStart(), searchContractDto.getEnd());
        verify(repairContractService).searchAllOrganizations(searchContractDto);
        assertThat(actual).isEqualTo(expected);
    }
    
    @Test
    void repairSearchAllOrganizationsException() {
        var searchContractDto = Instancio.of(RepairSearchContractAllOrganizationsDto.class)
                                         .set(field(RepairSearchContractAllOrganizationsDto::getDocumentType), EWB)
                                         .create();
        assertThrows(UnsupportedDocumentTypeException.class, () -> contractService.searchAllOrganizations(searchContractDto));
    }
    
    @Test
    void repairSearchSelfOrganization() {
        var searchContractDto = Instancio.of(RepairSearchContractSelfOrganizationDto.class)
                                         .set(field(RepairSearchContractSelfOrganizationDto::getDocumentType), REPAIR_AND_MAINTENANCE)
                                         .create();
        var expected = new PageImpl<>(List.of(Instancio.create(RepairContractGetSelfOrganizationDto.class)));
        var userid = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        doNothing().when(contractValidationService).validateDateRange(searchContractDto.getStart(), searchContractDto.getEnd());
        doReturn(Instancio.of(Employee.class)
                          .set(field(Employee::getOrganization), Instancio.of(Organization.class)
                                                                          .set(field(Organization::getId), organizationId)
                                                                          .create())
                          .create()).when(employeeService).getByUserId(userid);
        doReturn(expected).when(repairContractService).searchSelfOrganization(searchContractDto, organizationId);
        
        var actual = contractService.searchSelfOrganization(searchContractDto, userid);
        
        verify(contractValidationService).validateDateRange(searchContractDto.getStart(), searchContractDto.getEnd());
        verify(repairContractService).searchSelfOrganization(searchContractDto, organizationId);
        assertThat(actual).isEqualTo(expected);
    }
    
    @Test
    void repairSearchSelfOrganizationException() {
        var searchContractDto = Instancio.of(RepairSearchContractSelfOrganizationDto.class)
                                         .set(field(RepairSearchContractSelfOrganizationDto::getDocumentType), EWB)
                                         .create();
        var userId = UUID.randomUUID();
        assertThrows(UnsupportedDocumentTypeException.class, () -> contractService.searchSelfOrganization(searchContractDto, userId));
    }
    
    @Test
    void createTariff() {
        var humanReadableId = UUID.randomUUID().toString();
        var ewbTariffPostDto1 = Instancio.of(EwbTariffPostDto.class)
                                         .set(field(EwbTariffPostDto::getDocumentType), EWB)
                                         .create();
        var ewbTariffPostDto2 = Instancio.of(EwbTariffPostDto.class)
                                         .set(field(EwbTariffPostDto::getDocumentType), REPAIR_AND_MAINTENANCE)
                                         .create();
        var tariff = Instancio.of(Tariff.class).create();
        doReturn(Optional.of(Instancio.of(Contract.class)
                                      .set(field(Contract::getId), tariff.getContractId())
                                      .set(field(Contract::getStart), LocalDate.now().minusDays(1))
                                      .set(field(Contract::getEnd), LocalDate.now().plusDays(1))
                                      .set(field(Contract::isActive), true)
                                      .create())).when(contractRepository).findById(ewbTariffPostDto1.getContractId());
        doReturn(Optional.of(Instancio.of(Contract.class)
                                      .set(field(Contract::getId), tariff.getContractId())
                                      .set(field(Contract::getStart), LocalDate.now().minusDays(1))
                                      .set(field(Contract::getEnd), LocalDate.now().plusDays(1))
                                      .set(field(Contract::isActive), true)
                                      .create())).when(contractRepository).findById(ewbTariffPostDto2.getContractId());
        doReturn(tariff).when(tariffService).create(any(AbstractTariffPostDto.class), anyString(), anyBoolean());
        doNothing().when(ewbContractService).createTariff(any(EwbTariffPostDto.class), any(Tariff.class));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        contractService.createTariff(ewbTariffPostDto1, humanReadableId);
        assertThatThrownBy(() -> contractService.createTariff(ewbTariffPostDto2, humanReadableId))
                .isInstanceOf(UnsupportedDocumentTypeException.class);
        verify(tariffService, times(2)).create(any(AbstractTariffPostDto.class), anyString(), anyBoolean());
        verify(ewbContractService).createTariff(any(EwbTariffPostDto.class), any(Tariff.class));
    }
    
    @Test
    void edit() {
        var contractId = UUID.randomUUID();
        var contractPatchDto1 = Instancio.of(EwbContractPatchDto.class)
                                         .set(field(EwbContractPatchDto::getDocumentType), EWB)
                                         .create();
        var contractPatchDto2 = Instancio.of(EwbContractPatchDto.class)
                                         .set(field(EwbContractPatchDto::getDocumentType), EWB)
                                         .set(field(EwbContractPatchDto::getUvhd), null)
                                         .create();
        var contractPatchDto3 = Instancio.of(EwbContractPatchDto.class)
                                         .set(field(EwbContractPatchDto::getDocumentType), REPAIR_AND_MAINTENANCE)
                                         .create();
        doNothing().when(contractValidationService).validateUvhdUnique(contractPatchDto1.getUvhd());
        doNothing().when(contractValidationService).validateUvhdUnique(contractPatchDto3.getUvhd());
        doNothing().when(ewbContractService).edit(contractId, contractPatchDto1);
        doNothing().when(ewbContractService).edit(contractId, contractPatchDto2);
        contractService.edit(contractId, contractPatchDto1);
        contractService.edit(contractId, contractPatchDto2);
        assertThatThrownBy(() -> contractService.edit(contractId, contractPatchDto3))
                .isInstanceOf(UnsupportedDocumentTypeException.class);
        verify(contractValidationService, times(2)).validateUvhdUnique(any());
        verify(ewbContractService, times(2)).edit(any(UUID.class), any(EwbContractPatchDto.class));
    }
    
    @Test
    void getEwb() {
        var contractId = UUID.randomUUID();
        var expected = Instancio.create(EwbGetContractByIdDto.class);
        when(contractGetInternalService.getContractType(contractId)).thenReturn(EWB);
        when(ewbContractService.get(contractId)).thenReturn(expected);
        
        var actual = contractService.get(contractId);
        
        verify(contractGetInternalService).getContractType(contractId);
        verify(ewbContractService).get(contractId);
        assertEquals(expected, actual);
    }
    
    @Test
    void getExceptions() {
        var uuid1 = UUID.randomUUID();
        var uuid2 = UUID.randomUUID();
        when(contractGetInternalService.getContractType(uuid1)).thenThrow(new ContractNotFoundException(uuid1));
        assertThatThrownBy(() -> contractService.get(uuid1))
                .isInstanceOf(ContractNotFoundException.class)
                .hasMessage("Не найден договор ID:%s", uuid1);
        when(contractGetInternalService.getContractType(uuid2)).thenReturn(FUEL);
        assertThatThrownBy(() -> contractService.get(uuid2))
                .isInstanceOf(UnsupportedDocumentTypeException.class);
    }
    
    @Test
    void editTariff() {
        var tariffId = UUID.randomUUID();
        var abstractTariffPatchDto = Instancio.of(EwbTariffPatchDto.class)
                                              .set(field(EwbTariffPatchDto::getDocumentType), EWB)
                                              .create();
        var tariff = Instancio.of(Tariff.class)
                              .set(field(Tariff::getId), tariffId)
                              .create();
        when(tariffService.get(tariffId)).thenReturn(tariff);
        when(contractRepository.findById(tariff.getContractId()))
                .thenReturn(Optional.of(Instancio.of(Contract.class)
                                                 .set(field(Contract::getId), tariff.getContractId())
                                                 .set(field(Contract::getStart), LocalDate.now().minusDays(1))
                                                 .set(field(Contract::getEnd), LocalDate.now().plusDays(1))
                                                 .set(field(Contract::isActive), true)
                                                 .create()));
        contractService.editTariff(tariffId, abstractTariffPatchDto);
        verify(tariffService).edit(tariffId, abstractTariffPatchDto);
    }
    
    
    @Test
    void editExceptions() {
        var contractId = UUID.randomUUID();
        var tariffId = UUID.randomUUID();
        var abstractTariffPatchDto = Instancio.of(EwbTariffPatchDto.class)
                                              .set(field(EwbTariffPatchDto::getDocumentType), REPAIR_AND_MAINTENANCE)
                                              .create();
        when(tariffService.get(tariffId))
                .thenReturn(Instancio.of(Tariff.class)
                                     .set(field(Tariff::getId), tariffId)
                                     .set(field(Tariff::getContractId), contractId)
                                     .create());
        assertThatThrownBy(() -> contractService.editTariff(tariffId, abstractTariffPatchDto))
                .isInstanceOf(ContractNotFoundException.class)
                .hasMessageContaining("Не найден договор ID:%s", contractId);
        
        when(contractRepository.findById(contractId))
                .thenReturn(Optional.of(Instancio.of(Contract.class)
                                                 .set(field(Contract::getStart), LocalDate.now().plusDays(1))
                                                 .set(field(Contract::isActive), false)
                                                 .create()));
        assertThatThrownBy(() -> contractService.editTariff(tariffId, abstractTariffPatchDto))
                .isInstanceOf(ContractNotActiveException.class)
                .hasMessageContaining("Нельзя отредактировать тариф по неактивному договору ID:%s", contractId);
    }
    
    @Test
    void deactivate() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var id3 = UUID.randomUUID();
        doReturn(EWB).when(contractGetInternalService).getContractType(id1);
        doReturn(REPAIR_AND_MAINTENANCE).when(contractGetInternalService).getContractType(id2);
        doThrow(new ContractNotFoundException(id3)).when(contractGetInternalService).getContractType(id3);
        doNothing().when(ewbContractService).deactivate(id1);
        contractService.deactivate(id1);
        assertThatExceptionOfType(UnsupportedDocumentTypeException.class)
                .isThrownBy(() -> contractService.deactivate(id2));
        assertThatExceptionOfType(ContractNotFoundException.class)
                .isThrownBy(() -> contractService.deactivate(id3))
                .withMessage("Не найден договор ID:%s", id3);
        verify(contractGetInternalService, times(3)).getContractType(any(UUID.class));
        verify(ewbContractService).deactivate(any(UUID.class));
    }

    @Test
    void deactivateContractWithFuelCardsAllOrg() {
        var contractId = UUID.randomUUID();
        doReturn(FUEL).when(contractGetInternalService).getContractType(contractId);
        doNothing().when(fuelContractService).deactivateAllOrganizations(contractId);

        assertDoesNotThrow(() -> contractService.deactivateAllOrganizations(contractId));
    }

    @Test
    void deactivateContractWithFuelCardsSelfOrg() {
        var contractId = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var employeeId = UUID.randomUUID();
        doReturn(FUEL).when(contractGetInternalService).getContractType(contractId);
        doNothing().when(fuelContractService).deactivateSelfOrganization(contractId, organizationId);
        doReturn(Instancio.of(Employee.class)
                .set(field(Employee::getOrganization), Instancio.of(Organization.class)
                        .set(field(Organization::getId), organizationId)
                        .create())
                .create()).when(employeeService).getByUserId(employeeId);

        assertDoesNotThrow(() -> contractService.deactivateSelfOrganization(contractId, employeeId));
    }
    
    @Test
    void getAllStarted() {
        contractService.getAllStarted();
        verify(contractRepository).findStartedContracts();
    }
    
    @Test
    void getAllEnded() {
        contractService.getAllEnded();
        verify(contractRepository).findEndedContracts();
    }
    
    @Test
    void testGetContractSelfWithUnknownType() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        
        when(contractGetInternalService.getContractType(id)).thenReturn(EWB);
        
        var exception = assertThrows(
                UnexpectedDocumentTypeValidationException.class,
                () -> contractService.getContractSelfWithFuelStationPointsFile(id, userId),
                "Ожидалось возникновение исключения UnexpectedDocumentTypeValidationException"
                                    );
        
        verify(employeeService).getByUserId(userId);
        verify(contractGetInternalService).getContractType(id);
        assertTrue(exception.getMessage().contains("Неподдерживаемый операцией тип договора"));
    }
    
    @Test
    void testGetContractAllWithUnknownType() {
        var id = UUID.randomUUID();
        
        when(contractGetInternalService.getContractType(id)).thenReturn(EWB);
        
        var exception = assertThrows(
                UnexpectedDocumentTypeValidationException.class,
                () -> contractService.getContractAllWithFuelStationPointsFile(id),
                "Ожидалось возникновение исключения UnexpectedDocumentTypeValidationException"
                                    );
        
        verify(contractGetInternalService).getContractType(id);
        assertTrue(exception.getMessage().contains("Неподдерживаемый операцией тип договора"));
    }
    
    @Test
    void testGetContractSelfWhenContractNotFound() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        
        when(contractGetInternalService.getContractType(id)).thenThrow(new ContractNotFoundException(FUEL, id));
        var exception = assertThrows(
                ContractNotFoundException.class,
                () -> contractService.getContractSelfWithFuelStationPointsFile(id, userId),
                "Ожидалось возникновение исключения ContractNotFoundException"
                                    );
        
        verify(employeeService).getByUserId(userId);
        verify(contractGetInternalService).getContractType(id);
        assertTrue(exception.getMessage().contains(id.toString()));
    }
    
    @Test
    void testGetContractAllWhenContractNotFound() {
        var id = UUID.randomUUID();
        
        when(contractGetInternalService.getContractType(id)).thenThrow(new ContractNotFoundException(FUEL, id));
        var exception = assertThrows(
                ContractNotFoundException.class,
                () -> contractService.getContractAllWithFuelStationPointsFile(id),
                "Ожидалось возникновение исключения ContractNotFoundException"
                                    );
        
        assertTrue(exception.getMessage().contains(id.toString()));
    }
    
    @Test
    void validateServicePointsFileAllOrganizationsRepair() {
        var contractId = UUID.randomUUID();
        var file = new MockMultipartFile("file.xlsx", new byte[1]);
        contractService.validateServicePointsFileAllOrganizations(contractId, REPAIR_AND_MAINTENANCE, file);
        verify(repairContractService).validateServicePointsFileAllOrganizations(contractId, file);
    }
    
    @Test
    void validateServicePointsFileAllOrganizationsFuel() {
        var contractId = UUID.randomUUID();
        var file = new MockMultipartFile("file.xlsx", new byte[1]);
        contractService.validateServicePointsFileAllOrganizations(contractId, FUEL, file);
        verify(fuelContractService).validateServicePointsFileAllOrganizations(contractId, file);
    }
    
    @Test
    void validateServicePointsFileAllOrganizationsUnsupported() {
        assertThrows(UnsupportedDocumentTypeException.class,
                     () -> contractService.validateServicePointsFileAllOrganizations(null, DocumentType.EWB, null));
    }
    
    @Test
    void validateServicePointsFileSelfOrganizationRepair() {
        var userId = UUID.randomUUID();
        var contractId = UUID.randomUUID();
        var file = new MockMultipartFile("file.xlsx", new byte[1]);
        var organizationId = UUID.randomUUID();
        doReturn(Instancio.of(Employee.class)
                          .set(field(Employee::getOrganization), Instancio.of(Organization.class)
                                                                          .set(field(Organization::getId), organizationId)
                                                                          .create())
                          .create()).when(employeeService).getByUserId(userId);
        contractService.validateServicePointsFileSelfOrganization(contractId, REPAIR_AND_MAINTENANCE, file, userId);
        verify(employeeService).getByUserId(userId);
        verify(repairContractService).validateServicePointsFileSelfOrganization(contractId, file, organizationId);
    }
    
    @Test
    void validateServicePointsFileSelfOrganizationFuel() {
        var userId = UUID.randomUUID();
        var contractId = UUID.randomUUID();
        var file = new MockMultipartFile("file.xlsx", new byte[1]);
        var organizationId = UUID.randomUUID();
        doReturn(Instancio.of(Employee.class)
                          .set(field(Employee::getOrganization), Instancio.of(Organization.class)
                                                                          .set(field(Organization::getId), organizationId)
                                                                          .create())
                          .create()).when(employeeService).getByUserId(userId);
        contractService.validateServicePointsFileSelfOrganization(contractId, FUEL, file, userId);
        verify(employeeService).getByUserId(userId);
        verify(fuelContractService).validateServicePointsFileSelfOrganization(contractId, file, organizationId);
    }
    
    @Test
    void validateServicePointsFileSelfOrganizationUnsupported() {
        doReturn(Instancio.of(Employee.class)
                          .set(field(Employee::getOrganization), Instancio.of(Organization.class)
                                                                          .set(field(Organization::getId), UUID.randomUUID())
                                                                          .create())
                          .create()).when(employeeService).getByUserId(null);
        assertThrows(UnsupportedDocumentTypeException.class,
                     () -> contractService.validateServicePointsFileSelfOrganization(null, DocumentType.EWB, null, null));
    }

    @Test
    void haveActiveContractsByContractorId() {
        var contractorId = UUID.randomUUID();
        doReturn(false).when(repairContractService).haveActiveContractsByContractorId(contractorId);
        doReturn(true).when(fuelContractService).haveActiveContractsByContractorId(contractorId);
        var actual = contractService.haveActiveContractsByContractorId(contractorId);
        verify(repairContractService).haveActiveContractsByContractorId(contractorId);
        verify(fuelContractService).haveActiveContractsByContractorId(contractorId);
        assertThat(actual).isTrue();
    }
}
