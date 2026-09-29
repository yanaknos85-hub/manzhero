package ru.sber.transport.tariff_fleet.service.impl;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.mock.web.MockMultipartFile;
import ru.sber.transport.tariff_fleet.constant.ContractorType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.constant.ServiceType;
import ru.sber.transport.tariff_fleet.database.dao.RepairContractRepository;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.RepairContract;
import ru.sber.transport.tariff_fleet.database.projection.GetRepairContractProjection;
import ru.sber.transport.tariff_fleet.dto.ContractWithServicePointsFileDto;
import ru.sber.transport.tariff_fleet.dto.DateRange;
import ru.sber.transport.tariff_fleet.dto.FileData;
import ru.sber.transport.tariff_fleet.dto.PageSetting;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbSearchContractDto;
import ru.sber.transport.tariff_fleet.dto.repair.*;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;
import ru.sber.transport.tariff_fleet.exception.ContractAlreadyExistsException;
import ru.sber.transport.tariff_fleet.exception.ContractNotFoundException;
import ru.sber.transport.tariff_fleet.exception.FileDownloadException;
import ru.sber.transport.tariff_fleet.exception.ServicePointsUniqueException;
import ru.sber.transport.tariff_fleet.mapper.RepairContractMapper;
import ru.sber.transport.tariff_fleet.messaging.sender.RepairContractSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.RepairContractMessage;
import ru.sber.transport.tariff_fleet.model.PartiallyUpdateRepairContractModel;
import ru.sber.transport.tariff_fleet.service.ContractorService;
import ru.sber.transport.tariff_fleet.service.FileService;
import ru.sber.transport.tariff_fleet.service.RepairAndFuelServicePointService;
import ru.sber.transport.tariff_fleet.service.ServicePointService;
import ru.sber.transport.tariff_fleet.service.validation.RepairContractValidationService;
import ru.sber.transport.tariff_fleet.service.validation.ServicePointValidationService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairContractServiceImplTest {
    @InjectMocks
    private RepairContractServiceImpl repairContractService;
    @Mock
    private RepairContractValidationService repairContractValidationService;
    @Mock
    private FileService fileService;
    @Mock
    private RepairContractRepository repairContractRepository;
    @Mock
    private RepairContractMapper repairContractMapper;
    @Mock
    private ContractorService contractorService;
    @Mock
    private ServicePointService servicePointService;
    @Mock
    private ServicePointValidationService servicePointValidationService;
    @Mock
    private RepairAndFuelServicePointService repairAndFuelServicePointService;
    @Mock
    private RepairContractSender repairContractSender;
    @Captor
    private ArgumentCaptor<RepairContract> repairContractArgumentCaptor;
    @Captor
    private ArgumentCaptor<RepairContractPostAllOrganizationsDto> repairPostContractDtoArgumentCaptor;
    @Captor
    private ArgumentCaptor<RepairContractPostSelfOrganizationDto> repairContractPostSelfOrganizationDtoArgumentCaptor;
    @Captor
    private ArgumentCaptor<RepairContractMessage> repairContractMessageArgumentCaptor;

    @Test
    void findWithContract() {
        var contractId = UUID.randomUUID();
        var repairContract = Instancio.create(RepairContract.class);
        doReturn(Optional.empty()).when(repairContractRepository).findWithContractByContractId(contractId);
        assertThrows(ContractNotFoundException.class, () -> repairContractService.findWithContract(contractId));

        doReturn(Optional.of(repairContract)).when(repairContractRepository).findWithContractByContractId(contractId);
        var actual = repairContractService.findWithContract(contractId);
        verify(repairContractRepository, times(2)).findWithContractByContractId(contractId);
        assertThat(actual).isEqualTo(repairContract);
    }

    @SneakyThrows
    @Test
    void createAllOrganizations() {
        var contractDto = Instancio.of(RepairContractPostAllOrganizationsDto.class)
                .set(field(RepairContractPostAllOrganizationsDto::getLogo), null)
                .create();
        contractDto.getServicePoints().add(new ServicePointDto(
                "address",
                BigDecimal.valueOf(55.755826),
                BigDecimal.valueOf(37.617298)
        ));
        var contract = Instancio.of(Contract.class).create();
        var repairContract = Instancio.of(RepairContract.class).create();
        var expectedMessage = toRepairContractMessage(repairContract);

        doReturn(Instancio.of(Contractor.class)
                .set(field(Contractor::isActive), true)
                .set(field(Contractor::getServiceType), ServiceType.AUTOSERVICE)
                .create()).when(contractorService).getById(any(UUID.class));
        doReturn(repairContract)
                .when(repairContractMapper).repairContractPostDtoToRepairContract(repairPostContractDtoArgumentCaptor.capture(), any(Contract.class));
        doReturn(repairContract).when(repairContractRepository).save(repairContractArgumentCaptor.capture());
        doReturn(expectedMessage).when(repairContractMapper).toRepairContractMessage(repairContract);

        repairContractService.createAllOrganizations(contractDto, contract);

        var actual = repairContractArgumentCaptor.getValue();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(repairContract);
        var actualDto = repairPostContractDtoArgumentCaptor.getValue();
        assertThat(actualDto)
                .usingRecursiveComparison()
                .ignoringFields("serviceStations.logoS3Id")
                .isEqualTo(contractDto);
        verify(repairContractSender, times(1)).send(repairContractMessageArgumentCaptor.capture());
        assertThat(repairContractMessageArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(expectedMessage);
    }
    @SneakyThrows
    @Test
    void createAllOrganizationsExceptions() {
        var contractDto1 = Instancio.of(RepairContractPostAllOrganizationsDto.class)
                .set(field(RepairContractPostAllOrganizationsDto::getLogo), null)
                .create();
        var contractDto2 = Instancio.of(RepairContractPostAllOrganizationsDto.class).set(field(RepairContractPostAllOrganizationsDto::getLogo), null)
                .create();
        contractDto2.getServicePoints().add(contractDto2.getServicePoints().get(0));
        var contract = Instancio.of(Contract.class).create();
        doThrow(new ContractAlreadyExistsException(contract.getId())).when(repairContractValidationService)
                .validateContractorIdAndNumberUnique(
                        contractDto1.getOrganizationId(),
                        contractDto1.getContractorId(),
                        contractDto1.getNumber(), contract.getId());
        doThrow(new ServicePointsUniqueException()).when(servicePointValidationService).validateServicePoint(contractDto2.getServicePoints());
        assertThatThrownBy(() -> repairContractService.createAllOrganizations(contractDto1, contract))
                .isInstanceOf(ContractAlreadyExistsException.class)
                .hasMessageContaining("В системе уже есть договор с такими же Организацией Контрагентом и Номером. ИД Договора");
        assertThatThrownBy(() -> repairContractService.createAllOrganizations(contractDto2, contract))
                .isInstanceOf(ServicePointsUniqueException.class)
                .hasMessage("Договор не создан. Не должно быть Автосервисов с одинаковыми адресами и координатами");
        verify(repairContractSender, never()).send(any(RepairContractMessage.class));
    }

    @SneakyThrows
    @Test
    void createSelfOrganization() {
        var contractDto = Instancio.of(RepairContractPostSelfOrganizationDto.class)
                .set(field(RepairContractPostSelfOrganizationDto::getLogo), null)
                .create();
        var servicePointDto = new ServicePointDto(
                "address",
                BigDecimal.valueOf(55.755826),
                BigDecimal.valueOf(37.617298)
        );
        contractDto.getServicePoints().add(servicePointDto);
        var contract = Instancio.create(Contract.class);
        var repairContract = Instancio.create(RepairContract.class);
        var organizationId = UUID.randomUUID();
        var expectedMessage = toRepairContractMessage(repairContract);

        doReturn(Instancio.of(Contractor.class)
                .set(field(Contractor::isActive), true)
                .set(field(Contractor::getServiceType), ServiceType.AUTOSERVICE)
                .create()).when(contractorService).getById(contractDto.getContractorId());
        doReturn(repairContract)
                .when(repairContractMapper)
                .repairContractPostSelfOrganizationDtoToRepairContract(repairContractPostSelfOrganizationDtoArgumentCaptor.capture(),
                        eq(contract),
                        eq(organizationId));
        doReturn(repairContract).when(repairContractRepository).save(repairContractArgumentCaptor.capture());
        doReturn(expectedMessage).when(repairContractMapper).toRepairContractMessage(repairContract);

        repairContractService.createSelfOrganization(contractDto, contract, organizationId);

        var actual = repairContractArgumentCaptor.getValue();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(repairContract);
        var actualDto = repairContractPostSelfOrganizationDtoArgumentCaptor.getValue();
        assertThat(actualDto)
                .usingRecursiveComparison()
                .ignoringFields("serviceStations.logoS3Id")
                .isEqualTo(contractDto);
        verify(repairContractSender, times(1)).send(repairContractMessageArgumentCaptor.capture());
        assertThat(repairContractMessageArgumentCaptor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(expectedMessage);
    }

    @SneakyThrows
    @Test
    void createSelfOrganizationExceptions() {
        var contractDto1 = Instancio.create(RepairContractPostSelfOrganizationDto.class);
        var contractDto2 = Instancio.create(RepairContractPostSelfOrganizationDto.class);
        contractDto2.getServicePoints().add(contractDto2.getServicePoints().get(0));
        var contract = Instancio.of(Contract.class).create();
        var organizationId = UUID.randomUUID();
        doThrow(new ContractAlreadyExistsException(contractDto1.getContractorId(), contractDto1.getNumber())).when(repairContractValidationService)
                .validateContractorIdAndNumberUnique(
                        organizationId,
                        contractDto1.getContractorId(),
                        contractDto1.getNumber(),
                        contract.getId());
        doThrow(new ServicePointsUniqueException()).when(servicePointValidationService).validateServicePoint(contractDto2.getServicePoints());
        assertThatThrownBy(() -> repairContractService.createSelfOrganization(contractDto1, contract, organizationId))
                .isInstanceOf(ContractAlreadyExistsException.class)
                .hasMessage(String.format("Ошибка в полях Контрагент, Номер договора. Данное сочетание значений contractorId='%s' и " +
                        "number='%s' уже имеется в системе", contractDto1.getContractorId(), contractDto1.getNumber()));
        assertThatThrownBy(() -> repairContractService.createSelfOrganization(contractDto2, contract, organizationId))
                .isInstanceOf(ServicePointsUniqueException.class)
                .hasMessage("Договор не создан. Не должно быть Автосервисов с одинаковыми адресами и координатами");
        verify(repairContractSender, never()).send(any(RepairContractMessage.class));
    }

    @Test
    void search() {
        var searchContractDto = Instancio.of(RepairSearchContractDto.class)
                .set(field(EwbSearchContractDto::getDocumentType), DocumentType.REPAIR_AND_MAINTENANCE)
                .set(field(EwbSearchContractDto::getActive), true)
                .set(field(EwbSearchContractDto::getPeriod),
                        new DateRange(LocalDate.now(), LocalDate.now().plusDays(7)))
                .set(field(EwbSearchContractDto::getPageSetting), new PageSetting(0, 10))
                .create();
        var expected1 = Instancio.create(RepairContractGetDto.class);
        var expected2 = Instancio.create(RepairContractGetDto.class);
        var start = LocalDate.now();
        var end = LocalDate.now().plusDays(7);
        var pageRequest = PageRequest.of(0, 10);
        var expected = new PageImpl<>(List.of(expected1, expected2), pageRequest, 2);
        var projection1 = createGetRepairContractProjection(expected1);
        var projection2 = createGetRepairContractProjection(expected2);
        var projectionPageable = new PageImpl<>(List.of(projection1, projection2), pageRequest, 2);
        doReturn(projectionPageable).when(repairContractRepository).searchRepairContracts(searchContractDto.getNumber(),
                searchContractDto.getContractorId(),
                null,
                start, end,
                searchContractDto.getActive(),
                searchContractDto.getPageRequest());
        doReturn(expected1, expected2).when(repairContractMapper).getRepairContractProjectionToRepairGetContractDto(any());
        var actual = repairContractService.search(searchContractDto,
                LocalDate.now(),
                LocalDate.now().plusDays(7));
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void searchAllOrganizations() {
        var searchContractDto = Instancio.create(RepairSearchContractAllOrganizationsDto.class);
        var expected1 = Instancio.create(RepairContractGetAllOrganizationsDto.class);
        var expected2 = Instancio.create(RepairContractGetAllOrganizationsDto.class);
        var pageRequest = PageRequest.of(0, 10);
        var expected = new PageImpl<>(List.of(expected1, expected2), pageRequest, 2);
        var projection1 = createGetRepairContractAllOrganizationsProjection(expected1);
        var projection2 = createGetRepairContractAllOrganizationsProjection(expected2);
        var projectionPageable = new PageImpl<>(List.of(projection1, projection2), pageRequest, 2);
        doReturn(projectionPageable).when(repairContractRepository).searchRepairContracts(searchContractDto.getNumber(),
                searchContractDto.getContractorId(),
                searchContractDto.getOrganizationId(),
                searchContractDto.getStart(),
                searchContractDto.getEnd(),
                searchContractDto.getActive(),
                searchContractDto.getPageRequest());
        doReturn(expected1, expected2).when(repairContractMapper).getRepairContractProjectionToRepairContractGetAllOrganizationsDto(any());
        var actual = repairContractService.searchAllOrganizations(searchContractDto);
        verify(repairContractRepository).searchRepairContracts(searchContractDto.getNumber(),
                searchContractDto.getContractorId(),
                searchContractDto.getOrganizationId(),
                searchContractDto.getStart(),
                searchContractDto.getEnd(),
                searchContractDto.getActive(),
                searchContractDto.getPageRequest());
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void searchSelfOrganization() {
        var searchContractDto = Instancio.create(RepairSearchContractSelfOrganizationDto.class);
        var expected1 = Instancio.create(RepairContractGetSelfOrganizationDto.class);
        var expected2 = Instancio.create(RepairContractGetSelfOrganizationDto.class);
        var organizationId = UUID.randomUUID();
        var pageRequest = PageRequest.of(0, 10);
        var expected = new PageImpl<>(List.of(expected1, expected2), pageRequest, 2);
        var projection1 = createGetRepairContractSelfOrganizationsProjection(expected1);
        var projection2 = createGetRepairContractSelfOrganizationsProjection(expected2);
        var projectionPageable = new PageImpl<>(List.of(projection1, projection2), pageRequest, 2);
        doReturn(projectionPageable).when(repairContractRepository).searchRepairContracts(searchContractDto.getNumber(),
                searchContractDto.getContractorId(),
                organizationId,
                searchContractDto.getStart(),
                searchContractDto.getEnd(),
                searchContractDto.getActive(),
                searchContractDto.getPageRequest());
        doReturn(expected1, expected2).when(repairContractMapper).getRepairContractProjectionToRepairContractGetSelfOrganizationDto(any());
        var actual = repairContractService.searchSelfOrganization(searchContractDto, organizationId);
        verify(repairContractRepository).searchRepairContracts(searchContractDto.getNumber(),
                searchContractDto.getContractorId(),
                organizationId,
                searchContractDto.getStart(),
                searchContractDto.getEnd(),
                searchContractDto.getActive(),
                searchContractDto.getPageRequest());
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }


    @Test
    void testSuccessfulPartialUpdate() {
        var validId = UUID.randomUUID();
        var validOrgId = UUID.randomUUID();
        var repairContract = new RepairContract();
        var repairContractMessage = toRepairContractMessage(repairContract);

        when(repairContractRepository.findWithContractByContractId(validId)).thenReturn(Optional.of(repairContract));
        doReturn(repairContractMessage).when(repairContractMapper).toRepairContractMessage(repairContract);

        repairContractService.updatePartiallySelfOrganization(validId, model, validOrgId);

        verify(repairContractValidationService, times(1))
                .validateOrganizationPermission(any(RepairContract.class), eq(validOrgId));
        verify(repairContractValidationService, times(1))
                .validateUpdateContract(any(RepairContract.class), any(PartiallyUpdateRepairContractModel.class));
        verify(repairContractRepository, times(1)).save(any(RepairContract.class));
        verify(repairContractSender, times(1)).send(eq(repairContractMessage));
    }

    @Test
    void testNonExistingContract() {
        var invalidId = UUID.randomUUID();
        var validOrgId = UUID.randomUUID();
        when(repairContractRepository.findWithContractByContractId(invalidId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                repairContractService.updatePartiallySelfOrganization(invalidId, model, validOrgId)
        ).isInstanceOf(ContractNotFoundException.class);
        verify(repairContractSender, never()).send(any(RepairContractMessage.class));
    }

    @Test
    void testInvalidOrganizationAccess() {
        var validId = UUID.randomUUID();
        var invalidOrgId = UUID.randomUUID();

        assertThatThrownBy(() ->
                repairContractService.updatePartiallySelfOrganization(validId, model, invalidOrgId)
        ).isInstanceOf(ContractNotFoundException.class);
        verify(repairContractSender, never()).send(any(RepairContractMessage.class));
    }

    @Test
    void testGlobalPartialUpdate() {
        var validId = UUID.randomUUID();
        var repairContract = new RepairContract();
        var repairContractMessage = toRepairContractMessage(repairContract);

        when(repairContractRepository.findWithContractByContractId(validId)).thenReturn(Optional.of(repairContract));
        doReturn(repairContractMessage).when(repairContractMapper).toRepairContractMessage(repairContract);

        repairContractService.updatePartiallyAllOrganizations(validId, model);
        verify(repairContractValidationService, never())
                .validateOrganizationPermission(any(RepairContract.class), any(UUID.class));
        verify(repairContractValidationService, times(1))
                .validateUpdateContract(any(RepairContract.class), any(PartiallyUpdateRepairContractModel.class));
        verify(repairContractRepository, times(1)).save(any(RepairContract.class));
        verify(repairContractSender, times(1)).send(eq(repairContractMessage));
    }

    private final PartiallyUpdateRepairContractModel model = new PartiallyUpdateRepairContractModel(
            Optional.of(100L), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());


    GetRepairContractProjection createGetRepairContractProjection(RepairContractGetDto repairGetContractDto) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(GetRepairContractProjection.class);
        projection.setContractorId(repairGetContractDto.getContractorId());
        projection.setContractorName(repairGetContractDto.getContractorName());
        projection.setId(repairGetContractDto.getId());
        projection.setNumber(repairGetContractDto.getNumber());
        projection.setUvhd(repairGetContractDto.getUvhd());
        projection.setAmount(repairGetContractDto.getAmount());
        projection.setStart(repairGetContractDto.getStart());
        projection.setEnd(repairGetContractDto.getEnd());
        projection.setActive(repairGetContractDto.isActive());
        return projection;
    }

    GetRepairContractProjection createGetRepairContractAllOrganizationsProjection(RepairContractGetAllOrganizationsDto repairGetContractDto) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(GetRepairContractProjection.class);
        projection.setContractorId(null);
        projection.setContractorName(repairGetContractDto.getContractorName());
        projection.setId(repairGetContractDto.getId());
        projection.setNumber(repairGetContractDto.getNumber());
        projection.setUvhd(null);
        projection.setAmount(repairGetContractDto.getAmountWithoutVat());
        projection.setStart(repairGetContractDto.getStart());
        projection.setEnd(repairGetContractDto.getEnd());
        projection.setActive(repairGetContractDto.isActive());
        return projection;
    }

    GetRepairContractProjection createGetRepairContractSelfOrganizationsProjection(RepairContractGetSelfOrganizationDto repairGetContractDto) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(GetRepairContractProjection.class);
        projection.setContractorId(null);
        projection.setContractorName(repairGetContractDto.getContractorName());
        projection.setId(repairGetContractDto.getId());
        projection.setNumber(repairGetContractDto.getNumber());
        projection.setUvhd(null);
        projection.setAmount(repairGetContractDto.getAmountWithoutVat());
        projection.setStart(repairGetContractDto.getStart());
        projection.setEnd(repairGetContractDto.getEnd());
        projection.setActive(repairGetContractDto.isActive());
        return projection;
    }

    @SneakyThrows
    @Test
    void getContractWithServicePointsFile() {
        var repairContract = Instancio.of(RepairContract.class)
                .set(field(RepairContract::getContractor), Instancio.of(Contractor.class)
                        .set(field(Contractor::getContractorType), ContractorType.AUTOSERVICE_EXTERNAL)
                        .create())
                .create();
        var expected = Instancio.create(ContractWithServicePointsFileDto.class);

        doReturn(new FileData("xlsx", null)).when(fileService).get(anyString());
        doReturn(Optional.of(repairContract)).when(repairContractRepository).findWithContractByContractId(repairContract.getContractId());
        doReturn(null).when(servicePointService).buildServicePointsFile(any());
        doReturn(expected).when(repairContractMapper).toContractWithServicePointsFileDto(repairContract, null, null, false);

        assertThat(repairContractService.getContractWithServicePointsFile(repairContract.getContractId()))
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @SneakyThrows
    @Test
    void getContractWithServicePointsFileIsEditable() {
        var repairContract = Instancio.of(RepairContract.class)
                .set(field(RepairContract::getContractor), Instancio.of(Contractor.class)
                        .set(field(Contractor::getContractorType), ContractorType.AUTOSERVICE_INTERNAL)
                        .create())
                .create();
        var expected = Instancio.create(ContractWithServicePointsFileDto.class);

        doReturn(new FileData("xlsx", null)).when(fileService).get(anyString());
        doReturn(Optional.of(repairContract)).when(repairContractRepository).findWithContractByContractId(repairContract.getContractId());
        doReturn(null).when(servicePointService).buildServicePointsFile(any());
        doReturn(expected).when(repairContractMapper).toContractWithServicePointsFileDto(repairContract, null, null, true);

        var actual = repairContractService.getContractWithServicePointsFile(repairContract.getContractId());

        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @SneakyThrows
    @Test
    void getContractSelfWithServicePointsFile() {
        var repairContract = Instancio.of(RepairContract.class)
                .set(field(RepairContract::getContractor), Instancio.of(Contractor.class)
                        .set(field(Contractor::getContractorType), ContractorType.AUTOSERVICE_EXTERNAL)
                        .create())
                .create();
        var expected = Instancio.create(ContractWithServicePointsFileDto.class);

        doReturn(new FileData("xlsx", null)).when(fileService).get(anyString());
        doReturn(Optional.of(repairContract)).when(repairContractRepository).findWithContractByContractId(repairContract.getContractId());
        doNothing().when(repairContractValidationService).validateOrganizationPermission(repairContract, repairContract.getOrganization().getId());
        doReturn(null).when(servicePointService).buildServicePointsFile(any());
        doReturn(expected).when(repairContractMapper).toContractWithServicePointsFileDto(repairContract, null, null, false);

        assertThat(repairContractService.getContractSelfWithServicePointsFile(repairContract.getContractId(), repairContract.getOrganization().getId()))
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldThrowFileDownloadExceptionWhenFileLoadFails() throws IOException {
        var logoStorageId = UUID.randomUUID();
        var contractId = UUID.randomUUID();
        when(repairContractRepository.findWithContractByContractId(contractId)).thenReturn(Optional.of(new RepairContract() {{
            setLogoS3Id(logoStorageId);
            setContract(new Contract());
            setOrganization(new Organization());
            setContractor(new Contractor());
        }}));
        given(fileService.get(anyString())).willThrow(new IOException("Ошибка чтения файла"));

        assertThatThrownBy(() -> repairContractService.getContractWithServicePointsFile(contractId))
                .isInstanceOf(FileDownloadException.class)
                .hasMessageContaining(logoStorageId.toString());
    }

    @Test
    void validateServicePointsFileAllOrganizations() {
        var contractId = UUID.randomUUID();
        var repairContract = Instancio.create(RepairContract.class);
        var file = new MockMultipartFile("file.xlsx", new byte[0]);
        doReturn(Optional.of(repairContract)).when(repairContractRepository).findWithContractByContractId(contractId);
        doReturn(repairContract.getContractor()).when(contractorService).getById(repairContract.getContractorId());
        repairContractService.validateServicePointsFileAllOrganizations(contractId, file);
        verify(repairContractRepository).findWithContractByContractId(contractId);
        verify(repairContractValidationService).validateContractActive(repairContract.getContract());
        verify(repairContractValidationService).validateContractTypeForServicePointUpdating(repairContract.getContractor());
        verify(repairAndFuelServicePointService).validateUploadServicePoints(file);
    }

    @Test
    void validateServicePointsFileSelfOrganization() {
        var contractId = UUID.randomUUID();
        var repairContract = Instancio.create(RepairContract.class);
        var file = new MockMultipartFile("file.xlsx", new byte[0]);
        var organizationId = UUID.randomUUID();
        doReturn(Optional.of(repairContract)).when(repairContractRepository).findWithContractByContractId(contractId);
        doReturn(repairContract.getContractor()).when(contractorService).getById(repairContract.getContractorId());
        repairContractService.validateServicePointsFileSelfOrganization(contractId, file, organizationId);
        verify(repairContractRepository).findWithContractByContractId(contractId);
        verify(repairContractValidationService).validateOrganizationPermission(repairContract, organizationId);
        verify(repairContractValidationService).validateContractActive(repairContract.getContract());
        verify(repairContractValidationService).validateContractTypeForServicePointUpdating(repairContract.getContractor());
        verify(repairAndFuelServicePointService).validateUploadServicePoints(file);
    }

    @Test
    void haveActiveContractsByContractorId() {
        var contractorId = UUID.randomUUID();
        doReturn(true).when(repairContractRepository).existsByContractorIdAndContract_ActiveTrue(contractorId);
        var actual = repairContractService.haveActiveContractsByContractorId(contractorId);
        verify(repairContractRepository).existsByContractorIdAndContract_ActiveTrue(contractorId);
        assertThat(actual).isTrue();
    }

    private static RepairContractMessage toRepairContractMessage(RepairContract repairContract) {
        return new RepairContractMessage(
                repairContract.getContractId(),
                Objects.isNull(repairContract.getOrganization()) ? null : repairContract.getOrganization().getId(),
                Objects.isNull(repairContract.getContract()) ? null : repairContract.getContract().getNumber(),
                Objects.isNull(repairContract.getContract()) ? null : repairContract.getContract().getUvhd(),
                Objects.isNull(repairContract.getContract()) ? null : repairContract.getContract().getStart(),
                Objects.isNull(repairContract.getContract()) ? null : repairContract.getContract().getEnd(),
                Objects.isNull(repairContract.getContract()) ? Boolean.FALSE : repairContract.getContract().isActive(),
                repairContract.getServicePointsName(),
                repairContract.getAmountWithoutVat(),
                repairContract.getAmountWithVat(),
                Objects.isNull(repairContract.getContractor()) ? null : repairContract.getContractor().getId(),
                Objects.isNull(repairContract.getServicePoints()) ? null : repairContract.getServicePoints().stream()
                        .map(sp -> new RepairContractMessage.ServicePoint(
                                sp.getId(),
                                sp.getAddress(),
                                sp.getLatitude(),
                                sp.getLongitude(),
                                sp.isActive(),
                                repairContract.getLogoS3Id()
                        ))
                        .toList()
        );
    }

}