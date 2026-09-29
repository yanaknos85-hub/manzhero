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
import ru.sber.transport.tariff_fleet.constant.ServiceType;
import ru.sber.transport.tariff_fleet.database.dao.FuelContractRepository;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.database.model.FuelContract;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.projection.GetFuelContractProjection;
import ru.sber.transport.tariff_fleet.dto.FileData;
import ru.sber.transport.tariff_fleet.dto.fuel.*;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointDto;
import ru.sber.transport.tariff_fleet.exception.ContractAlreadyExistsException;
import ru.sber.transport.tariff_fleet.exception.ContractNotActiveException;
import ru.sber.transport.tariff_fleet.exception.ContractNotFoundException;
import ru.sber.transport.tariff_fleet.exception.FileDownloadException;
import ru.sber.transport.tariff_fleet.exception.OrganizationPermissionException;
import ru.sber.transport.tariff_fleet.exception.ServicePointsUniqueException;
import ru.sber.transport.tariff_fleet.exception.WrongContractTypeForServicePointsUpdatingException;
import ru.sber.transport.tariff_fleet.mapper.FuelContractMapper;
import ru.sber.transport.tariff_fleet.messaging.sender.FuelContractSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.FuelContractMessage;
import ru.sber.transport.tariff_fleet.model.PartiallyUpdateFuelContractModel;
import ru.sber.transport.tariff_fleet.service.ContractorService;
import ru.sber.transport.tariff_fleet.service.FileService;
import ru.sber.transport.tariff_fleet.service.RepairAndFuelServicePointService;
import ru.sber.transport.tariff_fleet.service.ServicePointService;
import ru.sber.transport.tariff_fleet.service.tariff.FuelTariffService;
import ru.sber.transport.tariff_fleet.service.validation.FuelContractValidationService;
import ru.sber.transport.tariff_fleet.service.validation.ServicePointValidationService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuelContractServiceImplTest {

    private final Clock fixedClock = Clock.fixed(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusMinutes(20)
            .toInstant(ZoneOffset.UTC), ZoneId.of(ZoneOffset.UTC.getId()));
    @InjectMocks
    private FuelContractServiceImpl fuelContractService;
    @Mock
    private FuelContractValidationService fuelContractValidationService;
    @Mock
    private ServicePointValidationService servicePointValidationService;
    @Mock
    private FileService fileService;
    @Mock
    private FuelContractRepository fuelContractRepository;
    @Mock
    private FuelContractMapper fuelContractMapper;
    @Mock
    private ContractorService contractorService;
    @Mock
    private ServicePointService servicePointService;
    @Mock
    private RepairAndFuelServicePointService repairAndFuelServicePointService;
    @Mock
    private FuelContractSender fuelContractSender;
    @Mock
    private FuelTariffService fuelTariffService;
    @Mock
    private Clock clock;
    @Captor
    private ArgumentCaptor<FuelContract> fuelContractArgumentCaptor;
    @Captor
    private ArgumentCaptor<FuelContractPostAllOrganizationsDto> fuelPostContractDtoArgumentCaptor;
    @Captor
    private ArgumentCaptor<FuelContractPostSelfOrganizationDto> fuelContractPostSelfOrganizationDtoArgumentCaptor;

    @Test
    void findWithContract() {
        var contractId = UUID.randomUUID();
        var fuelContract = Instancio.create(FuelContract.class);
        doReturn(Optional.empty()).when(fuelContractRepository).findWithContractByContractId(contractId);
        assertThrows(ContractNotFoundException.class, () -> fuelContractService.findWithContract(contractId));

        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(contractId);
        var actual = fuelContractService.findWithContract(contractId);
        verify(fuelContractRepository, times(2)).findWithContractByContractId(contractId);
        assertThat(actual).isEqualTo(fuelContract);
    }

    @SneakyThrows
    @Test
    void createAllOrganizations() {
        var contractDto = Instancio.of(FuelContractPostAllOrganizationsDto.class)
                .set(field(FuelContractPostAllOrganizationsDto::getLogo), null)
                .create();
        contractDto.getServicePoints().add(new ServicePointDto(
                "address",
                BigDecimal.valueOf(55.755826),
                BigDecimal.valueOf(37.617298)
        ));
        var contract = Instancio.create(Contract.class);
        var fuelContract = Instancio.create(FuelContract.class);
        doReturn(Instancio.of(Contractor.class)
                .set(field(Contractor::isActive), true)
                .set(field(Contractor::getServiceType), ServiceType.AUTOSERVICE)
                .create()).when(contractorService).getById(any(UUID.class));
        doReturn(fuelContract)
                .when(fuelContractMapper).fuelContractPostDtoToFuelContract(fuelPostContractDtoArgumentCaptor.capture(), eq(contract));
        doReturn(fuelContract).when(fuelContractRepository).save(fuelContractArgumentCaptor.capture());
        doReturn(Instancio.create(FuelContractMessage.class)).when(fuelContractMapper).toFuelContractMessage(fuelContract);

        fuelContractService.createAllOrganizations(contractDto, contract);
        verify(servicePointValidationService).validateServicePoint(contractDto.getServicePoints());
        var actual = fuelContractArgumentCaptor.getValue();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(fuelContract);
        var actualDto = fuelPostContractDtoArgumentCaptor.getValue();
        assertThat(actualDto)
                .usingRecursiveComparison()
                .ignoringFields("serviceStations.logoS3Id")
                .isEqualTo(contractDto);
        verify(fuelContractSender, times(1)).send(any(FuelContractMessage.class));
    }

    @SneakyThrows
    @Test
    void createAllOrganizationsExceptions() {
        var contractDto1 = Instancio.create(FuelContractPostAllOrganizationsDto.class);
        var contractDto2 = Instancio.create(FuelContractPostAllOrganizationsDto.class);
        contractDto2.getServicePoints().add(contractDto2.getServicePoints().get(0));
        var contract = Instancio.of(Contract.class).create();

        doThrow(new ContractAlreadyExistsException(contractDto1.getContractorId(), contractDto1.getNumber()))
                .when(fuelContractValidationService).validateContractorIdAndNumberUnique(
                        contractDto1.getOrganizationId(),
                        contractDto1.getContractorId(),
                        contractDto1.getNumber(), contract.getId());
        doThrow(new ServicePointsUniqueException()).when(servicePointValidationService).validateServicePoint(contractDto2.getServicePoints());

        assertThatThrownBy(() -> fuelContractService.createAllOrganizations(contractDto1, contract))
                .isInstanceOf(ContractAlreadyExistsException.class)
                .hasMessage(String.format("Ошибка в полях Контрагент, Номер договора. Данное сочетание значений contractorId='%s' и " +
                                "number='%s' уже имеется в системе",
                        contractDto1.getContractorId(), contractDto1.getNumber()));

        assertThatThrownBy(() -> fuelContractService.createAllOrganizations(contractDto2, contract))
                .isInstanceOf(ServicePointsUniqueException.class)
                .hasMessage("Договор не создан. Не должно быть Автосервисов с одинаковыми адресами и координатами");
    }

    @SneakyThrows
    @Test
    void createSelfOrganization() {
        var contractDto = Instancio.of(FuelContractPostSelfOrganizationDto.class)
                .set(field(FuelContractPostSelfOrganizationDto::getLogo), null)
                .create();
        var servicePointDto = new ServicePointDto(
                "address",
                BigDecimal.valueOf(55.755826),
                BigDecimal.valueOf(37.617298)
        );
        contractDto.getServicePoints().add(servicePointDto);
        var contract = Instancio.create(Contract.class);
        var fuelContract = Instancio.create(FuelContract.class);
        var organizationId = UUID.randomUUID();
        doReturn(Instancio.of(Contractor.class)
                .set(field(Contractor::isActive), true)
                .set(field(Contractor::getServiceType), ServiceType.AUTOSERVICE)
                .create()).when(contractorService).getById(contractDto.getContractorId());
        doReturn(fuelContract)
                .when(fuelContractMapper)
                .fuelContractPostSelfOrganizationDtoToFuelContract(fuelContractPostSelfOrganizationDtoArgumentCaptor.capture(),
                        eq(contract),
                        eq(organizationId));
        doReturn(fuelContract).when(fuelContractRepository).save(fuelContractArgumentCaptor.capture());
        doReturn(Instancio.create(FuelContractMessage.class)).when(fuelContractMapper).toFuelContractMessage(fuelContract);

        fuelContractService.createSelfOrganization(contractDto, contract, organizationId);
        verify(servicePointValidationService).validateServicePoint(anyList());
        var actual = fuelContractArgumentCaptor.getValue();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(fuelContract);
        var actualDto = fuelContractPostSelfOrganizationDtoArgumentCaptor.getValue();
        assertThat(actualDto)
                .usingRecursiveComparison()
                .ignoringFields("serviceStations.logoS3Id")
                .isEqualTo(contractDto);
        verify(fuelContractSender, times(1)).send(any(FuelContractMessage.class));
    }

    @SneakyThrows
    @Test
    void createSelfOrganizationExceptions() {
        var contractDto1 = Instancio.create(FuelContractPostSelfOrganizationDto.class);
        var contractDto2 = Instancio.create(FuelContractPostSelfOrganizationDto.class);
        contractDto2.getServicePoints().add(contractDto2.getServicePoints().get(0));
        var contract = Instancio.of(Contract.class).create();
        var organizationId = UUID.randomUUID();
        doThrow(new ContractAlreadyExistsException(contract.getId())).when(fuelContractValidationService)
                .validateContractorIdAndNumberUnique(
                        organizationId,
                        contractDto1.getContractorId(),
                        contractDto1.getNumber(), contract.getId());
        doThrow(new ServicePointsUniqueException()).when(servicePointValidationService).validateServicePoint(contractDto2.getServicePoints());
        assertThatThrownBy(() -> fuelContractService.createSelfOrganization(contractDto1, contract, organizationId))
                .isInstanceOf(ContractAlreadyExistsException.class)
                .hasMessageContaining("В системе уже есть договор с такими же Организацией Контрагентом и Номером. ИД Договора");
        assertThatThrownBy(() -> fuelContractService.createSelfOrganization(contractDto2, contract, organizationId))
                .isInstanceOf(ServicePointsUniqueException.class)
                .hasMessage("Договор не создан. Не должно быть Автосервисов с одинаковыми адресами и координатами");
    }

    @SneakyThrows
    @Test
    void testGetContractWithServicePointsFile() {
        var id = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var fuelContract = new FuelContract() {{
            setContractId(id);
            setLogoS3Id(UUID.randomUUID());
            setContractor(new Contractor());
            setOrganization(new Organization(organizationId, 1L, "Рога и копыта", true));
            setContract(new Contract(
                    null,
                    LocalDateTime.now(),
                    UUID.randomUUID(),
                    LocalDate.now(),
                    LocalDate.now(),
                    "123",
                    "123uvhd",
                    true
            ));
        }};

        doReturn(new FileData("xlsx", null)).when(fileService).get(anyString());
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(id);
        doReturn(null).when(servicePointService).buildServicePointsFile(any());

        assertDoesNotThrow(() -> {
            fuelContractService.getContractWithServicePointsFile(id);
        });
    }

    @SneakyThrows
    @Test
    void testGetContractSelfWithServicePointsFile() {
        var id = UUID.randomUUID();
        var organizationId = UUID.randomUUID();
        var fuelContract = new FuelContract() {{
            setContractId(id);
            setLogoS3Id(UUID.randomUUID());
            setContractor(new Contractor());
            setOrganization(new Organization(organizationId, 1L, "Рога и копыта", true));
            setContract(new Contract(
                    null,
                    LocalDateTime.now(),
                    UUID.randomUUID(),
                    LocalDate.now(),
                    LocalDate.now(),
                    "123",
                    "123uvhd",
                    true
            ));
        }};

        doReturn(new FileData("xlsx", null)).when(fileService).get(anyString());
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(id);
        doNothing().when(fuelContractValidationService).validateOrganizationPermission(fuelContract, organizationId);
        doReturn(null).when(servicePointService).buildServicePointsFile(any());
        assertDoesNotThrow(() -> {
            fuelContractService.getContractSelfWithServicePointsFile(id, organizationId);
        });
    }

    @Test
    void searchAllOrganizations() {
        var searchContractDto = Instancio.create(FuelSearchContractAllOrganizationsDto.class);
        var expected1 = Instancio.create(FuelContractGetAllOrganizationsDto.class);
        var expected2 = Instancio.create(FuelContractGetAllOrganizationsDto.class);
        var pageRequest = PageRequest.of(0, 10);
        var expected = new PageImpl<>(List.of(expected1, expected2), pageRequest, 2);
        var projection1 = createGetFuelContractAllOrganizationsProjection(expected1);
        var projection2 = createGetFuelContractAllOrganizationsProjection(expected2);
        var projectionPageable = new PageImpl<>(List.of(projection1, projection2), pageRequest, 2);
        doReturn(projectionPageable).when(fuelContractRepository).searchFuelContracts(searchContractDto.getNumber(),
                searchContractDto.getContractorId(),
                searchContractDto.getOrganizationId(),
                searchContractDto.getStart(),
                searchContractDto.getEnd(),
                searchContractDto.getActive(),
                searchContractDto.getPageRequest());
        doReturn(expected1, expected2).when(fuelContractMapper).getFuelContractProjectionToFuelContractGetAllOrganizationsDto(any());
        var actual = fuelContractService.searchAllOrganizations(searchContractDto);
        verify(fuelContractRepository).searchFuelContracts(searchContractDto.getNumber(),
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
        var searchContractDto = Instancio.create(FuelSearchContractSelfOrganizationDto.class);
        var expected1 = Instancio.create(FuelContractGetSelfOrganizationDto.class);
        var expected2 = Instancio.create(FuelContractGetSelfOrganizationDto.class);
        var organizationId = UUID.randomUUID();
        var pageRequest = PageRequest.of(0, 10);
        var expected = new PageImpl<>(List.of(expected1, expected2), pageRequest, 2);
        var projection1 = createGetFuelContractSelfOrganizationsProjection(expected1);
        var projection2 = createGetFuelContractSelfOrganizationsProjection(expected2);
        var projectionPageable = new PageImpl<>(List.of(projection1, projection2), pageRequest, 2);
        doReturn(projectionPageable).when(fuelContractRepository).searchFuelContracts(searchContractDto.getNumber(),
                searchContractDto.getContractorId(),
                organizationId,
                searchContractDto.getStart(),
                searchContractDto.getEnd(),
                searchContractDto.getActive(),
                searchContractDto.getPageRequest());
        doReturn(expected1, expected2).when(fuelContractMapper).getFuelContractProjectionToFuelContractGetSelfOrganizationDto(any());
        var actual = fuelContractService.searchSelfOrganization(searchContractDto, organizationId);
        verify(fuelContractRepository).searchFuelContracts(searchContractDto.getNumber(),
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
        var fuelContract = Instancio.create(FuelContract.class);
        var updatedContract = fuelContract;
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(validId);
        doReturn(updatedContract).when(fuelContractRepository).save(fuelContract);
        doReturn(Instancio.create(FuelContractMessage.class)).when(fuelContractMapper).toFuelContractMessage(updatedContract);

        fuelContractService.updatePartiallySelfOrganization(validId, model, validOrgId);

        verify(fuelContractValidationService, times(1))
                .validateOrganizationPermission(any(FuelContract.class), eq(validOrgId));
        verify(fuelContractValidationService, times(1))
                .validateUpdateContract(any(FuelContract.class), any(PartiallyUpdateFuelContractModel.class));
        verify(fuelContractRepository, times(1)).save(any(FuelContract.class));
        verify(fuelContractSender, times(1)).send(any(FuelContractMessage.class));
        verify(fuelContractRepository, times(1)).findWithContractByContractId(validId);
    }

    @Test
    void testSuccessfulPartialUpdateAllOrganizations() {
        var validId = UUID.randomUUID();
        var fuelContract = Instancio.create(FuelContract.class);
        var updatedContract = fuelContract;
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(validId);
        doReturn(updatedContract).when(fuelContractRepository).save(fuelContract);
        doReturn(Instancio.create(FuelContractMessage.class)).when(fuelContractMapper).toFuelContractMessage(updatedContract);

        fuelContractService.updatePartiallyAllOrganizations(validId, model);

        verify(fuelContractValidationService, never())
                .validateOrganizationPermission(any(FuelContract.class), any(UUID.class));
        verify(fuelContractValidationService, times(1))
                .validateUpdateContract(any(FuelContract.class), any(PartiallyUpdateFuelContractModel.class));
        verify(fuelContractRepository, times(1)).save(any(FuelContract.class));
        verify(fuelContractSender, times(1)).send(any(FuelContractMessage.class));
        verify(fuelContractRepository, times(1)).findWithContractByContractId(validId);
    }

    @Test
    @SneakyThrows
    void testSuccessfulPartialUpdateWithAllFields() {
        var validId = UUID.randomUUID();
        var validOrgId = UUID.randomUUID();
        var servicePoints = List.of(
                new ServicePointDto("address1", BigDecimal.valueOf(55.75), BigDecimal.valueOf(37.61)),
                new ServicePointDto("address2", BigDecimal.valueOf(56.75), BigDecimal.valueOf(38.61))
        );
        var modelWithAllFields = new PartiallyUpdateFuelContractModel(
                Optional.of(150000L),
                Optional.of(125000L),
                Optional.of("base64LogoString"),
                Optional.of(servicePoints),
                Optional.of("Сервисные точки")
        );
        var fuelContract = Instancio.create(FuelContract.class);
        var updatedContract = fuelContract;
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(validId);
        doReturn(updatedContract).when(fuelContractRepository).save(fuelContract);
        doReturn(Instancio.create(FuelContractMessage.class)).when(fuelContractMapper).toFuelContractMessage(updatedContract);

        fuelContractService.updatePartiallySelfOrganization(validId, modelWithAllFields, validOrgId);

        verify(fuelContractValidationService, times(1))
                .validateOrganizationPermission(any(FuelContract.class), eq(validOrgId));
        verify(fuelContractValidationService, times(1))
                .validateUpdateContract(any(FuelContract.class), eq(modelWithAllFields));
        verify(servicePointService, times(1))
                .dropExistedPointsForContractAndCreate(fuelContract.getContractId(), servicePoints);
        verify(fileService, times(1)).upload(any(ByteArrayInputStream.class), anyString(), anyString());
        verify(fuelContractRepository, times(1)).save(any(FuelContract.class));
        verify(fuelContractSender, times(1)).send(any(FuelContractMessage.class));
    }

    @Test
    void testSuccessfulPartialUpdateClearLogo() {
        var validId = UUID.randomUUID();
        var validOrgId = UUID.randomUUID();
        var modelClearLogo = new PartiallyUpdateFuelContractModel(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        var fuelContract = new FuelContract();
        fuelContract.setLogoS3Id(UUID.randomUUID());
        var updatedContract = fuelContract;
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(validId);
        doReturn(updatedContract).when(fuelContractRepository).save(fuelContract);
        doReturn(Instancio.create(FuelContractMessage.class)).when(fuelContractMapper).toFuelContractMessage(updatedContract);

        fuelContractService.updatePartiallySelfOrganization(validId, modelClearLogo, validOrgId);

        verify(fuelContractValidationService, times(1))
                .validateOrganizationPermission(any(FuelContract.class), eq(validOrgId));
        verify(fuelContractRepository, times(1)).save(argThat(c -> c.getLogoS3Id() == null));
        verify(fuelContractSender, times(1)).send(any(FuelContractMessage.class));
    }

    @Test
    void testNonExistingContract() {
        var invalidId = UUID.randomUUID();
        var validOrgId = UUID.randomUUID();
        when(fuelContractRepository.findWithContractByContractId(invalidId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                fuelContractService.updatePartiallySelfOrganization(invalidId, model, validOrgId)
        ).isInstanceOf(ContractNotFoundException.class);
    }

    @Test
    void testNonExistingContractAllOrganizations() {
        var invalidId = UUID.randomUUID();
        when(fuelContractRepository.findWithContractByContractId(invalidId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                fuelContractService.updatePartiallyAllOrganizations(invalidId, model)
        ).isInstanceOf(ContractNotFoundException.class);
    }

    @Test
    void testInvalidOrganizationAccess() {
        var validId = UUID.randomUUID();
        var invalidOrgId = UUID.randomUUID();
        var contractId = UUID.randomUUID();
        var fuelContract = new FuelContract();
        fuelContract.setContractId(contractId);
        fuelContract.setOrganization(new Organization());
        fuelContract.getOrganization().setId(UUID.randomUUID());
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(validId);
        doThrow(new OrganizationPermissionException(OrganizationPermissionException.CONTRACT_PERMISSION_ERROR_MESSAGE, contractId))
                .when(fuelContractValidationService).validateOrganizationPermission(fuelContract, invalidOrgId);

        assertThatThrownBy(() ->
                fuelContractService.updatePartiallySelfOrganization(validId, model, invalidOrgId)
        ).isInstanceOf(OrganizationPermissionException.class);
    }

    @Test
    void testContractNotActiveSelfOrganization() {
        var validId = UUID.randomUUID();
        var validOrgId = UUID.randomUUID();
        var fuelContract = new FuelContract();
        fuelContract.setContract(new Contract());
        fuelContract.getContract().setActive(false);
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(validId);
        doThrow(new ContractNotActiveException(""))
                .when(fuelContractValidationService).validateUpdateContract(fuelContract, model);

        assertThatThrownBy(() ->
                fuelContractService.updatePartiallySelfOrganization(validId, model, validOrgId)
        ).isInstanceOf(ContractNotActiveException.class);
    }

    @Test
    void testContractNotActiveAllOrganizations() {
        var validId = UUID.randomUUID();
        var fuelContract = new FuelContract();
        fuelContract.setContract(new Contract());
        fuelContract.getContract().setActive(false);
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(validId);
        doThrow(new ContractNotActiveException(""))
                .when(fuelContractValidationService).validateUpdateContract(fuelContract, model);

        assertThatThrownBy(() ->
                fuelContractService.updatePartiallyAllOrganizations(validId, model)
        ).isInstanceOf(ContractNotActiveException.class);
    }

    @Test
    void testWrongContractorTypeForServicePointsSelfOrganization() {
        var validId = UUID.randomUUID();
        var validOrgId = UUID.randomUUID();
        var contractor = new Contractor();
        contractor.setContractorType(ContractorType.API);
        var fuelContract = new FuelContract();
        fuelContract.setContractor(contractor);
        fuelContract.setContract(new Contract());
        fuelContract.getContract().setActive(true);
        var modelWithServicePoints = new PartiallyUpdateFuelContractModel(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(List.of(new ServicePointDto("addr", BigDecimal.valueOf(55), BigDecimal.valueOf(37)))),
                Optional.empty()
        );
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(validId);
        doThrow(new WrongContractTypeForServicePointsUpdatingException())
                .when(fuelContractValidationService).validateUpdateContract(fuelContract, modelWithServicePoints);

        assertThatThrownBy(() ->
                fuelContractService.updatePartiallySelfOrganization(validId, modelWithServicePoints, validOrgId)
        ).isInstanceOf(WrongContractTypeForServicePointsUpdatingException.class);
    }

    @Test
    void testWrongContractorTypeForServicePointsAllOrganizations() {
        var validId = UUID.randomUUID();
        var contractor = new Contractor();
        contractor.setContractorType(ContractorType.API);
        var fuelContract = new FuelContract();
        fuelContract.setContractor(contractor);
        fuelContract.setContract(new Contract());
        fuelContract.getContract().setActive(true);
        var modelWithServicePoints = new PartiallyUpdateFuelContractModel(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(List.of(new ServicePointDto("addr", BigDecimal.valueOf(55), BigDecimal.valueOf(37)))),
                Optional.empty()
        );
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(validId);
        doThrow(new WrongContractTypeForServicePointsUpdatingException())
                .when(fuelContractValidationService).validateUpdateContract(fuelContract, modelWithServicePoints);

        assertThatThrownBy(() ->
                fuelContractService.updatePartiallyAllOrganizations(validId, modelWithServicePoints)
        ).isInstanceOf(WrongContractTypeForServicePointsUpdatingException.class);
    }

    @Test
    void testGlobalPartialUpdate() {
        var validId = UUID.randomUUID();
        var fuelContract = Instancio.create(FuelContract.class);
        var updatedContract = fuelContract;
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(validId);
        doReturn(updatedContract).when(fuelContractRepository).save(fuelContract);
        doReturn(Instancio.create(FuelContractMessage.class)).when(fuelContractMapper).toFuelContractMessage(updatedContract);

        fuelContractService.updatePartiallyAllOrganizations(validId, model);
        verify(fuelContractValidationService, never())
                .validateOrganizationPermission(any(FuelContract.class), any(UUID.class));
        verify(fuelContractValidationService, times(1))
                .validateUpdateContract(any(FuelContract.class), any(PartiallyUpdateFuelContractModel.class));
        verify(fuelContractRepository, times(1)).save(any(FuelContract.class));
        verify(fuelContractSender, times(1)).send(any(FuelContractMessage.class));
        verify(fuelContractRepository, times(1)).findWithContractByContractId(validId);
    }

    @Test
    void testGlobalPartialUpdateWithServicePoints() {
        var validId = UUID.randomUUID();
        var servicePoints = List.of(
                new ServicePointDto("address1", BigDecimal.valueOf(55.75), BigDecimal.valueOf(37.61))
        );
        var modelWithServicePoints = new PartiallyUpdateFuelContractModel(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(servicePoints),
                Optional.of("Новые сервисные точки")
        );
        var fuelContract = Instancio.create(FuelContract.class);
        var updatedContract = fuelContract;
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(validId);
        doReturn(updatedContract).when(fuelContractRepository).save(fuelContract);
        doReturn(Instancio.create(FuelContractMessage.class)).when(fuelContractMapper).toFuelContractMessage(updatedContract);

        fuelContractService.updatePartiallyAllOrganizations(validId, modelWithServicePoints);
        verify(fuelContractValidationService, never())
                .validateOrganizationPermission(any(FuelContract.class), any(UUID.class));
        verify(fuelContractValidationService, times(1))
                .validateUpdateContract(any(FuelContract.class), eq(modelWithServicePoints));
        verify(servicePointService, times(1))
                .dropExistedPointsForContractAndCreate(fuelContract.getContractId(), servicePoints);
        verify(fuelContractRepository, times(1)).save(any(FuelContract.class));
        verify(fuelContractSender, times(1)).send(any(FuelContractMessage.class));
    }

    @Test
    void shouldThrowFileDownloadExceptionWhenFileLoadFails() throws IOException {
        var logoStorageId = UUID.randomUUID();
        var contractId = UUID.randomUUID();
        when(fuelContractRepository.findWithContractByContractId(contractId)).thenReturn(Optional.of(new FuelContract() {{
            setLogoS3Id(logoStorageId);
            setContract(new Contract());
            setOrganization(new Organization());
            setContractor(new Contractor());
        }}));
        given(fileService.get(anyString())).willThrow(new IOException("Ошибка чтения файла"));

        assertThatThrownBy(() -> fuelContractService.getContractWithServicePointsFile(contractId))
                .isInstanceOf(FileDownloadException.class)
                .hasMessageContaining(logoStorageId.toString());
    }

    @Test
    void validateServicePointsFileAllOrganizations() {
        var contractId = UUID.randomUUID();
        var fuelContract = Instancio.create(FuelContract.class);
        var file = new MockMultipartFile("file.xlsx", new byte[0]);
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(contractId);
        doReturn(fuelContract.getContractor()).when(contractorService).getById(fuelContract.getContractorId());
        fuelContractService.validateServicePointsFileAllOrganizations(contractId, file);
        verify(fuelContractRepository).findWithContractByContractId(contractId);
        verify(fuelContractValidationService).validateContractActive(fuelContract.getContract());
        verify(fuelContractValidationService).validateContractTypeForServicePointUpdating(fuelContract.getContractor());
        verify(repairAndFuelServicePointService).validateUploadServicePoints(file);
    }

    @Test
    void validateServicePointsFileSelfOrganization() {
        var contractId = UUID.randomUUID();
        var fuelContract = Instancio.create(FuelContract.class);
        var file = new MockMultipartFile("file.xlsx", new byte[0]);
        var organizationId = UUID.randomUUID();
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(contractId);
        doReturn(fuelContract.getContractor()).when(contractorService).getById(fuelContract.getContractorId());
        fuelContractService.validateServicePointsFileSelfOrganization(contractId, file, organizationId);
        verify(fuelContractRepository).findWithContractByContractId(contractId);
        verify(fuelContractValidationService).validateOrganizationPermission(fuelContract, organizationId);
        verify(fuelContractValidationService).validateContractActive(fuelContract.getContract());
        verify(fuelContractValidationService).validateContractTypeForServicePointUpdating(fuelContract.getContractor());
        verify(repairAndFuelServicePointService).validateUploadServicePoints(file);
    }

    @Test
    void autoActivate() {
        var contractId = UUID.randomUUID();
        var fuelContract = Instancio.of(FuelContract.class)
                .set(field(FuelContract::getContractId), contractId)
                .create();
        var message = Instancio.create(FuelContractMessage.class);

        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(contractId);
        doReturn(message).when(fuelContractMapper).toFuelContractMessage(fuelContract);

        fuelContractService.autoActivate(contractId);

        verify(fuelContractRepository).save(fuelContractArgumentCaptor.capture());
        verify(fuelTariffService).activateByContractId(contractId);
        verify(fuelContractSender).send(message);

        assertThat(fuelContractArgumentCaptor.getValue().getContract().isActive()).isTrue();
    }

    @Test
    void autoDeactivate() {
        var contractId = UUID.randomUUID();
        var fuelContract = Instancio.of(FuelContract.class)
                .set(field(FuelContract::getContractId), contractId)
                .create();
        fuelContract.getContract().setActive(true);
        var message = Instancio.create(FuelContractMessage.class);

        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(Optional.of(fuelContract)).when(fuelContractRepository).findWithContractByContractId(contractId);
        doReturn(message).when(fuelContractMapper).toFuelContractMessage(fuelContract);

        fuelContractService.autoDeactivate(contractId);
        verify(fuelContractRepository).save(fuelContractArgumentCaptor.capture());
        verify(fuelTariffService).deactivateByContractId(contractId);
        verify(fuelContractSender).send(message);

        var contract = fuelContractArgumentCaptor.getValue().getContract();
        assertThat(contract.isActive()).isFalse();
        assertThat(contract.getEnd()).isEqualTo(LocalDate.now(fixedClock));
    }

    @Test
    void haveActiveContractsByContractorId() {
        var contractorId = UUID.randomUUID();
        doReturn(true).when(fuelContractRepository).existsByContractorIdAndContract_ActiveTrue(contractorId);
        boolean actual = fuelContractService.haveActiveContractsByContractorId(contractorId);
        verify(fuelContractRepository).existsByContractorIdAndContract_ActiveTrue(contractorId);
        assertThat(actual).isTrue();
    }

    private final PartiallyUpdateFuelContractModel model = new PartiallyUpdateFuelContractModel(
            Optional.of(100L), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());

    private GetFuelContractProjection createGetFuelContractAllOrganizationsProjection(FuelContractGetAllOrganizationsDto fuelGetContractDto) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(GetFuelContractProjection.class);
        projection.setContractorId(null);
        projection.setContractorName(fuelGetContractDto.getContractorName());
        projection.setId(fuelGetContractDto.getId());
        projection.setNumber(fuelGetContractDto.getNumber());
        projection.setUvhd(null);
        projection.setAmount(fuelGetContractDto.getAmountWithoutVat());
        projection.setStart(fuelGetContractDto.getStart());
        projection.setEnd(fuelGetContractDto.getEnd());
        projection.setActive(fuelGetContractDto.isActive());
        return projection;
    }

    GetFuelContractProjection createGetFuelContractSelfOrganizationsProjection(FuelContractGetSelfOrganizationDto fuelGetContractDto) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(GetFuelContractProjection.class);
        projection.setContractorId(null);
        projection.setContractorName(fuelGetContractDto.getContractorName());
        projection.setId(fuelGetContractDto.getId());
        projection.setNumber(fuelGetContractDto.getNumber());
        projection.setUvhd(null);
        projection.setAmount(fuelGetContractDto.getAmountWithoutVat());
        projection.setStart(fuelGetContractDto.getStart());
        projection.setEnd(fuelGetContractDto.getEnd());
        projection.setActive(fuelGetContractDto.isActive());
        return projection;
    }

}