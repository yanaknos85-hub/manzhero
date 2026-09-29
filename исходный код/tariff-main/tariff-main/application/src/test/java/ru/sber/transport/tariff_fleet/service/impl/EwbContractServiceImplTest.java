package ru.sber.transport.tariff_fleet.service.impl;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.constant.InspectionType;
import ru.sber.transport.tariff_fleet.database.dao.EwbContractRepository;
import ru.sber.transport.tariff_fleet.database.model.*;
import ru.sber.transport.tariff_fleet.database.projection.GetEwbContractProjection;
import ru.sber.transport.tariff_fleet.dto.DateRange;
import ru.sber.transport.tariff_fleet.dto.OrganizationMedicalLicensePatchDto;
import ru.sber.transport.tariff_fleet.dto.OrganizationMedicalLicensePostDto;
import ru.sber.transport.tariff_fleet.dto.PageSetting;
import ru.sber.transport.tariff_fleet.dto.ewb.*;
import ru.sber.transport.tariff_fleet.exception.*;
import ru.sber.transport.tariff_fleet.mapper.EwbContractMapper;
import ru.sber.transport.tariff_fleet.mapper.OrganizationMedicalLicenseMapper;
import ru.sber.transport.tariff_fleet.messaging.sender.EwbContractSender;
import ru.sber.transport.tariff_fleet.messaging.sender.OrganizationMedicalLicenseSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.EwbContractMessage;
import ru.sber.transport.tariff_fleet.messaging.sender.message.OrganizationMedicalLicenseMessage;
import ru.sber.transport.tariff_fleet.service.EdfOperatorService;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sber.transport.tariff_fleet.service.tariff.EwbTariffService;
import ru.sber.transport.tariff_fleet.service.validation.EwbContractValidationService;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EwbContractServiceImplTest {
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2023, 11, 27, 9, 10);
    private final Clock fixedClock = Clock.fixed(LOCAL_DATE_TIME.toInstant(ZoneOffset.UTC), ZoneId.of(ZoneOffset.UTC.getId()));
    @InjectMocks
    private EwbContractServiceImpl ewbContractService;
    @Mock
    private Clock clock;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private EdfOperatorService edfOperatorService;
    @Mock
    private EwbContractValidationService ewbContractValidationService;
    @Mock
    private EwbTariffService ewbTariffService;
    @Mock
    private EwbContractRepository ewbContractRepository;
    @Mock
    private EwbContractMapper ewbContractMapper;
    @Mock
    private OrganizationMedicalLicenseMapper organizationMedicalLicenseMapper;
    @Mock
    private EwbContractSender ewbContractSender;
    @Mock
    private OrganizationMedicalLicenseSender organizationMedicalLicenseSender;
    @Captor
    private ArgumentCaptor<EwbContract> ewbContractArgumentCaptor;

    static Stream<Arguments> createMedicineTypes() {
        return Stream.of(
                Arguments.of(
                        InspectionType.MEDIC
                ),
                Arguments.of(
                        InspectionType.TECHNIC
                ),
                Arguments.of(
                        InspectionType.TELEMEDIC
                )
        );
    }

    @ParameterizedTest
    @MethodSource
    void createMedicineTypes(InspectionType inspectionType) {
        var contractDto = Instancio.of(EwbContractPostDto.class)
                .set(field(EwbContractPostDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbContractPostDto::getInspectionType), inspectionType)
                .set(field(EwbContractPostDto::getContractorMedicalLicense), Instancio.of(OrganizationMedicalLicensePostDto.class)
                        .set(field(OrganizationMedicalLicensePostDto::issueDate), LocalDate.now())
                        .set(field(OrganizationMedicalLicensePostDto::expiryDate), LocalDate.now().plusYears(1))
                        .create())
                .create();
        var contract = Instancio.create(Contract.class);
        var ewbContract = Instancio.of(EwbContract.class)
                .set(field(EwbContract::getInspectionType), inspectionType)
                .create();
        var organization = Instancio.of(Organization.class)
                .set(field(Organization::isActive), true)
                .create();
        var edfOperator = Instancio.of(EdfOperator.class)
                .set(field(EdfOperator::isActive), true)
                .create();
        var ewbContractMessage = Instancio.create(EwbContractMessage.class);
        var organizationMedicalLicenseMessage = Instancio.create(OrganizationMedicalLicenseMessage.class);
        doReturn(Optional.of(organization)).when(organizationService).get(any(UUID.class));
        doReturn(Optional.of(edfOperator)).when(edfOperatorService).getById(anyString());
        doReturn(false).when(ewbContractRepository).existsByOrganizationIdAndInspectionTypeAndContract_NumberAndContract_ActiveTrue(
                contractDto.getContractorOrganizationId(),
                contractDto.getInspectionType(),
                contractDto.getNumber());
        doReturn(ewbContract)
                .when(ewbContractMapper).ewbContractPostDtoToEwbContract(contractDto, contract);
        doReturn(ewbContract).when(ewbContractRepository).save(any(EwbContract.class));
        doReturn(ewbContractMessage).when(ewbContractMapper).ewbContractToEwbContractMessage(ewbContract);
        doNothing().when(ewbContractSender).send(ewbContractMessage);
        if (inspectionType == InspectionType.MEDIC || inspectionType == InspectionType.TELEMEDIC) {
            doReturn(organizationMedicalLicenseMessage).when(organizationMedicalLicenseMapper)
                    .organizationMedicalLicenseToOrganizationMedicalLicenseMessage(any(OrganizationMedicalLicense.class));
        }
        ewbContractService.create(contractDto, contract);
        verify(ewbContractRepository).save(ewbContractArgumentCaptor.capture());
        var actual = ewbContractArgumentCaptor.getValue();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(ewbContract);
        if (inspectionType == InspectionType.MEDIC || inspectionType == InspectionType.TELEMEDIC) {
            verify(organizationMedicalLicenseMapper).organizationMedicalLicenseToOrganizationMedicalLicenseMessage(
                    any(OrganizationMedicalLicense.class));
            verify(organizationMedicalLicenseSender).send(any(OrganizationMedicalLicenseMessage.class));
        } else {
            verify(organizationMedicalLicenseMapper, never()).organizationMedicalLicenseToOrganizationMedicalLicenseMessage(
                    any(OrganizationMedicalLicense.class));
            verify(organizationMedicalLicenseSender, never()).send(any(OrganizationMedicalLicenseMessage.class));
        }
        verify(ewbContractMapper).ewbContractToEwbContractMessage(any(EwbContract.class));
        verify(ewbContractSender).send(any(EwbContractMessage.class));
    }

    @SneakyThrows
    @Test
    void createExceptions() {
        var contractDto1 = Instancio.create(EwbContractPostDto.class);
        var contractDto2 = Instancio.create(EwbContractPostDto.class);
        var contractDto3 = Instancio.create(EwbContractPostDto.class);
        var contractDto4 = Instancio.create(EwbContractPostDto.class);
        var contractDto5 = Instancio.of(EwbContractPostDto.class)
                .set(field(EwbContractPostDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbContractPostDto::getInspectionType), InspectionType.MEDIC)
                .set(field(EwbContractPostDto::getContractorMedicalLicense), null)
                .create();
        var organizationMedicalLicense = Instancio.of(OrganizationMedicalLicensePostDto.class)
                .set(field(OrganizationMedicalLicensePostDto::issueDate), LocalDate.now())
                .set(field(OrganizationMedicalLicensePostDto::expiryDate), LocalDate.now().plusYears(1))
                .create();
        var contractDto6 = Instancio.of(EwbContractPostDto.class)
                .set(field(EwbContractPostDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbContractPostDto::getInspectionType), InspectionType.MEDIC)
                .set(field(EwbContractPostDto::getContractorMedicalLicense), organizationMedicalLicense)
                .create();
        var wrongOrganizationMedicalLicense = Instancio.of(OrganizationMedicalLicensePostDto.class)
                .set(field(OrganizationMedicalLicensePostDto::issueDate), LocalDate.now().plusYears(1))
                .set(field(OrganizationMedicalLicensePostDto::expiryDate), LocalDate.now())
                .create();
        var contractDto7 = Instancio.of(EwbContractPostDto.class)
                .set(field(EwbContractPostDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbContractPostDto::getInspectionType), InspectionType.MEDIC)
                .set(field(EwbContractPostDto::getContractorMedicalLicense), wrongOrganizationMedicalLicense)
                .create();
        var contract = Instancio.create(Contract.class);
        var organization1 = Instancio.of(Organization.class)
                .set(field(Organization::isActive), false)
                .create();
        var organization2 = Instancio.of(Organization.class)
                .set(field(Organization::isActive), true)
                .create();
        var edfOperator1 = Instancio.of(EdfOperator.class)
                .set(field(EdfOperator::isActive), false)
                .create();
        var edfOperator2 = Instancio.of(EdfOperator.class)
                .set(field(EdfOperator::isActive), true)
                .create();
        doReturn(Optional.empty()).when(organizationService).get(contractDto1.getContractorOrganizationId());
        doReturn(Optional.of(organization1)).when(organizationService).get(contractDto2.getContractorOrganizationId());
        doReturn(Optional.of(organization2)).when(organizationService).get(contractDto3.getContractorOrganizationId());
        doReturn(Optional.of(organization2)).when(organizationService).get(contractDto4.getContractorOrganizationId());
        doReturn(Optional.of(organization2)).when(organizationService).get(contractDto5.getContractorOrganizationId());
        doReturn(Optional.of(organization2)).when(organizationService).get(contractDto6.getContractorOrganizationId());
        doReturn(Optional.of(organization2)).when(organizationService).get(contractDto7.getContractorOrganizationId());
        doReturn(Optional.empty()).when(edfOperatorService).getById(contractDto3.getEdfOperatorId());
        doReturn(Optional.of(edfOperator1)).when(edfOperatorService).getById(contractDto4.getEdfOperatorId());
        doReturn(Optional.of(edfOperator2)).when(edfOperatorService).getById(contractDto5.getEdfOperatorId());
        doReturn(Optional.of(edfOperator2)).when(edfOperatorService).getById(contractDto6.getEdfOperatorId());
        doReturn(Optional.of(edfOperator2)).when(edfOperatorService).getById(contractDto7.getEdfOperatorId());
        doReturn(true).when(ewbContractRepository).existsByOrganizationIdAndInspectionTypeAndContract_NumberAndContract_ActiveTrue(
                contractDto6.getContractorOrganizationId(),
                contractDto6.getInspectionType(),
                contractDto6.getNumber());
        assertThatThrownBy(() -> ewbContractService.create(contractDto1, contract))
                .isInstanceOf(ContractorOrganizationNotFoundException.class)
                .hasMessage("Не найдена организация контрагента %s", contractDto1.getContractorOrganizationId());
        assertThatThrownBy(() -> ewbContractService.create(contractDto2, contract))
                .isInstanceOf(ContractorOrganizationNotActiveException.class)
                .hasMessage("Не активна организация контрагента %s", contractDto2.getContractorOrganizationId());
        assertThatThrownBy(() -> ewbContractService.create(contractDto3, contract))
                .isInstanceOf(EdfOperatorNotFoundException.class)
                .hasMessage("Не найден оператор ЭДО %s", contractDto3.getEdfOperatorId());
        assertThatThrownBy(() -> ewbContractService.create(contractDto4, contract))
                .isInstanceOf(EdfOperatorNotActiveException.class)
                .hasMessage("Не активен оператор ЭДО %s", contractDto4.getEdfOperatorId());
        assertThatThrownBy(() -> ewbContractService.create(contractDto5, contract))
                .isInstanceOf(OrganizationMedicalLicenseEmptyException.class)
                .hasMessage("Нет данных о медицинской лицензии");
        assertThatThrownBy(() -> ewbContractService.create(contractDto6, contract))
                .isInstanceOf(EwbContractAlreadyExistsException.class)
                .hasMessage("В системе имеется договор с указанными параметрами. Номер договора: %s", contractDto6.getNumber());
        assertThatThrownBy(() -> ewbContractService.create(contractDto7, contract))
                .isInstanceOf(DateRangeValidationException.class)
                .hasMessage("Дата выдачи медицинской лицензии не может быть позже даты окончания действия");
    }

    @Test
    void search() {
        var pageSetting = new PageSetting(0, 10);
        var ewbSearchContractDto = Instancio.of(EwbSearchContractDto.class)
                .set(field(EwbSearchContractDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbSearchContractDto::getActive), true)
                .set(field(EwbSearchContractDto::getPeriod), new DateRange(LocalDate.now(), LocalDate.now().plusDays(7)))
                .set(field(EwbSearchContractDto::getPageSetting), pageSetting)
                .create();
        var expected1 = Instancio.create(EwbContractGetDto.class);
        var expected2 = Instancio.create(EwbContractGetDto.class);
        var pageRequest = PageRequest.of(0, 10);
        var expected = new PageImpl<>(List.of(expected1, expected2), pageRequest, 2);
        var projection1 = createGetEwbContractWithParentContractProjection(expected1);
        var projection2 = createGetEwbContractWithParentContractProjection(expected2);
        var projectionPageable = new PageImpl<>(List.of(projection1, projection2), pageRequest, 2);
        doReturn(projectionPageable).when(ewbContractRepository).searchEwbContracts(any(UUID.class),
                any(),
                anyString(),
                any(),
                any(),
                anyBoolean(),
                any(Pageable.class));
        doReturn(List.of(expected1, expected2))
                .when(ewbContractMapper).listGetEwbContractProjectionToListEwbContractGetDto(List.of(projection1, projection2));
        var actual = ewbContractService.search(ewbSearchContractDto,
                LocalDate.now(),
                LocalDate.now().plusDays(7));
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void createTariff() {
        var postTariffDto = Instancio.create(EwbTariffPostDto.class);
        var contract1 = Instancio.of(EwbContract.class)
                .set(field(EwbContract::getContract),
                        Instancio.of(Contract.class)
                                .set(field(Contract::getStart), LOCAL_DATE_TIME.toLocalDate())
                                .set(field(Contract::getEnd), LOCAL_DATE_TIME.toLocalDate().plusYears(1))
                                .set(field(Contract::isActive), true)
                                .create())
                .create();
        var contract2 = Instancio.of(EwbContract.class)
                .set(field(EwbContract::getContract),
                        Instancio.of(Contract.class)
                                .set(field(Contract::getStart), LOCAL_DATE_TIME.toLocalDate().plusDays(1))
                                .set(field(Contract::getEnd), LOCAL_DATE_TIME.toLocalDate().plusYears(1))
                                .set(field(Contract::isActive), false)
                                .create())
                .create();
        var tariff1 = Instancio.of(Tariff.class)
                .set(field(Tariff::getContractId), contract1.getContractId())
                .create();
        var tariff2 = Instancio.create(Tariff.class);
        var tariff3 = Instancio.of(Tariff.class)
                .set(field(Tariff::getContractId), contract2.getContractId())
                .create();
        doReturn(Optional.of(contract1)).when(ewbContractRepository).findById(tariff1.getContractId());
        doReturn(Optional.empty()).when(ewbContractRepository).findById(tariff2.getContractId());
        doReturn(Optional.of(contract2)).when(ewbContractRepository).findById(tariff3.getContractId());
        doNothing().when(ewbTariffService).createTariff(any(EwbTariffPostDto.class), any(Tariff.class), any(InspectionType.class));
        ewbContractService.createTariff(postTariffDto, tariff1);
        assertThatThrownBy(() -> ewbContractService.createTariff(postTariffDto, tariff2))
                .isInstanceOf(EwbContractNotFoundException.class)
                .hasMessage("Не найден договор на услугу Выпуск на линию %s", tariff2.getContractId());
        assertThatThrownBy(() -> ewbContractService.createTariff(postTariffDto, tariff3))
                .isInstanceOf(EwbContractNotActiveException.class)
                .hasMessage("Не активен договор на услугу Выпуск на линию %s", tariff3.getContractId());
        verify(ewbContractRepository, times(3)).findById(any(UUID.class));
        verify(ewbTariffService).createTariff(any(EwbTariffPostDto.class), any(Tariff.class), any(InspectionType.class));
    }

    @Test
    void edit() {
        var contractId1 = UUID.randomUUID();
        var contractId2 = UUID.randomUUID();
        var contractId3 = UUID.randomUUID();
        var contractId4 = UUID.randomUUID();
        var contractId5 = UUID.randomUUID();
        var contractId6 = UUID.randomUUID();
        var contractId7 = UUID.randomUUID();
        var contractId8 = UUID.randomUUID();
        var edfOperator = Instancio.of(EdfOperator.class)
                .set(field(EdfOperator::isActive), true)
                .create();
        var license1 = Instancio.of(OrganizationMedicalLicense.class)
                .set(field(OrganizationMedicalLicense::getIssueDate), LOCAL_DATE_TIME.toLocalDate().plusDays(10))
                .set(field(OrganizationMedicalLicense::getExpiryDate), LOCAL_DATE_TIME.toLocalDate().plusDays(30))
                .create();
        var license2 = Instancio.of(OrganizationMedicalLicense.class)
                .set(field(OrganizationMedicalLicense::getIssueDate), LOCAL_DATE_TIME.toLocalDate().plusDays(10))
                .set(field(OrganizationMedicalLicense::getExpiryDate), LOCAL_DATE_TIME.toLocalDate().plusDays(30))
                .create();
        var license3 = Instancio.of(OrganizationMedicalLicense.class)
                .set(field(OrganizationMedicalLicense::getIssueDate), LOCAL_DATE_TIME.toLocalDate().plusDays(10))
                .set(field(OrganizationMedicalLicense::getExpiryDate), LOCAL_DATE_TIME.toLocalDate().plusDays(30))
                .create();
        var license4 = Instancio.of(OrganizationMedicalLicense.class)
                .set(field(OrganizationMedicalLicense::getIssueDate), LOCAL_DATE_TIME.toLocalDate().plusDays(10))
                .set(field(OrganizationMedicalLicense::getExpiryDate), LOCAL_DATE_TIME.toLocalDate().plusDays(30))
                .create();
        var licensePatchDto1 = Instancio.of(OrganizationMedicalLicensePatchDto.class)
                .set(field(OrganizationMedicalLicensePatchDto::issueDate), LOCAL_DATE_TIME.toLocalDate())
                .set(field(OrganizationMedicalLicensePatchDto::expiryDate), LOCAL_DATE_TIME.toLocalDate().plusYears(1))
                .create();
        var licensePatchDto2 = Instancio.of(OrganizationMedicalLicensePatchDto.class)
                .set(field(OrganizationMedicalLicensePatchDto::issueDate), LOCAL_DATE_TIME.toLocalDate())
                .set(field(OrganizationMedicalLicensePatchDto::expiryDate), null)
                .create();
        var licensePatchDto3 = Instancio.of(OrganizationMedicalLicensePatchDto.class)
                .set(field(OrganizationMedicalLicensePatchDto::issueDate), null)
                .set(field(OrganizationMedicalLicensePatchDto::expiryDate), LOCAL_DATE_TIME.toLocalDate().plusYears(1))
                .create();
        var licensePatchDto4 = Instancio.of(OrganizationMedicalLicensePatchDto.class)
                .set(field(OrganizationMedicalLicensePatchDto::issueDate), LOCAL_DATE_TIME.toLocalDate().plusYears(1))
                .set(field(OrganizationMedicalLicensePatchDto::expiryDate), null)
                .create();
        var contractPatchDto1 = Instancio.of(EwbContractPatchDto.class)
                .set(field(EwbContractPatchDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbContractPatchDto::getContractorMedicalLicense), licensePatchDto1)
                .create();
        var contractPatchDto2 = Instancio.of(EwbContractPatchDto.class)
                .set(field(EwbContractPatchDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbContractPatchDto::getContractorMedicalLicense), licensePatchDto2)
                .create();
        var contractPatchDto3 = Instancio.of(EwbContractPatchDto.class)
                .set(field(EwbContractPatchDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbContractPatchDto::getContractorMedicalLicense), licensePatchDto3)
                .create();
        var contractPatchDto4 = Instancio.of(EwbContractPatchDto.class)
                .set(field(EwbContractPatchDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbContractPatchDto::getContractorMedicalLicense), licensePatchDto4)
                .create();
        var contractPatchDto5 = Instancio.of(EwbContractPatchDto.class)
                .set(field(EwbContractPatchDto::getDocumentType), null)
                .set(field(EwbContractPatchDto::getUvhd), null)
                .set(field(EwbContractPatchDto::getEdfOperatorId), null)
                .set(field(EwbContractPatchDto::getEdfCode), null)
                .set(field(EwbContractPatchDto::getContractorMedicalLicense),
                        new OrganizationMedicalLicensePatchDto(null,
                                null,
                                null,
                                null))
                .create();
        var contractPatchDto6 = Instancio.of(EwbContractPatchDto.class)
                .set(field(EwbContractPatchDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbContractPatchDto::getUvhd), null)
                .set(field(EwbContractPatchDto::getEdfOperatorId), null)
                .set(field(EwbContractPatchDto::getEdfCode), null)
                .set(field(EwbContractPatchDto::getContractorMedicalLicense), licensePatchDto4)
                .create();
        var contract1 = Instancio.of(Contract.class)
                .set(field(Contract::getId), contractId1)
                .set(field(Contract::getStart), LOCAL_DATE_TIME.toLocalDate())
                .set(field(Contract::getEnd), LOCAL_DATE_TIME.toLocalDate().plusYears(1))
                .set(field(Contract::isActive), true)
                .create();
        var contract2 = Instancio.of(Contract.class)
                .set(field(Contract::getId), contractId2)
                .set(field(Contract::getStart), LOCAL_DATE_TIME.toLocalDate())
                .set(field(Contract::getEnd), LOCAL_DATE_TIME.toLocalDate().plusYears(1))
                .set(field(Contract::isActive), true)
                .create();
        var contract3 = Instancio.of(Contract.class)
                .set(field(Contract::getId), contractId3)
                .set(field(Contract::getStart), LOCAL_DATE_TIME.toLocalDate())
                .set(field(Contract::getEnd), LOCAL_DATE_TIME.toLocalDate().plusYears(1))
                .set(field(Contract::isActive), true)
                .create();
        var contract4 = Instancio.of(Contract.class)
                .set(field(Contract::getId), contractId4)
                .set(field(Contract::getStart), LOCAL_DATE_TIME.toLocalDate())
                .set(field(Contract::getEnd), LOCAL_DATE_TIME.toLocalDate().plusYears(1))
                .set(field(Contract::isActive), true)
                .create();
        var contract5 = Instancio.of(Contract.class)
                .set(field(Contract::getId), contractId7)
                .set(field(Contract::getStart), LOCAL_DATE_TIME.toLocalDate().plusYears(1))
                .set(field(Contract::getEnd), LOCAL_DATE_TIME.toLocalDate().plusYears(2))
                .set(field(Contract::isActive), false)
                .create();
        var contract6 = Instancio.of(Contract.class)
                .set(field(Contract::getId), contractId8)
                .set(field(Contract::getStart), LOCAL_DATE_TIME.toLocalDate().minusYears(1))
                .set(field(Contract::getEnd), LOCAL_DATE_TIME.toLocalDate().minusDays(1))
                .set(field(Contract::isActive), false)
                .create();
        var ewbContract = Instancio.of(EwbContract.class)
                .set(field(EwbContract::getContractId), contract1.getId())
                .set(field(EwbContract::getInspectionType), InspectionType.MEDIC)
                .set(field(EwbContract::getContract), contract1)
                .set(field(EwbContract::getOrganizationMedicalLicense), license1)
                .create();
        var ewbContract2 = Instancio.of(EwbContract.class)
                .set(field(EwbContract::getContractId), contract2.getId())
                .set(field(EwbContract::getInspectionType), InspectionType.MEDIC)
                .set(field(EwbContract::getContract), contract2)
                .set(field(EwbContract::getOrganizationMedicalLicense), license2)
                .create();
        var ewbContract3 = Instancio.of(EwbContract.class)
                .set(field(EwbContract::getContractId), contract3.getId())
                .set(field(EwbContract::getInspectionType), InspectionType.MEDIC)
                .set(field(EwbContract::getContract), contract3)
                .set(field(EwbContract::getOrganizationMedicalLicense), license3)
                .create();
        var ewbContract4 = Instancio.of(EwbContract.class)
                .set(field(EwbContract::getContractId), contract4.getId())
                .set(field(EwbContract::getInspectionType), InspectionType.MEDIC)
                .set(field(EwbContract::getContract), contract4)
                .set(field(EwbContract::getOrganizationMedicalLicense), license4)
                .create();
        var ewbContract5 = Instancio.of(EwbContract.class)
                .set(field(EwbContract::getContractId), contract5.getId())
                .set(field(EwbContract::getInspectionType), InspectionType.MEDIC)
                .set(field(EwbContract::getContract), contract5)
                .set(field(EwbContract::getOrganizationMedicalLicense), license1)
                .create();
        var ewbContract6 = Instancio.of(EwbContract.class)
                .set(field(EwbContract::getContractId), contract6.getId())
                .set(field(EwbContract::getInspectionType), InspectionType.MEDIC)
                .set(field(EwbContract::getContract), contract6)
                .set(field(EwbContract::getOrganizationMedicalLicense), license1)
                .create();
        var organizationMedicalLicenseMessage = Instancio.create(OrganizationMedicalLicenseMessage.class);
        var ewbContractMessage = Instancio.create(EwbContractMessage.class);
        doReturn(Optional.of(ewbContract)).when(ewbContractRepository).findByIdWithMedicalLicense(contractId1);
        doReturn(Optional.of(ewbContract2)).when(ewbContractRepository).findByIdWithMedicalLicense(contractId2);
        doReturn(Optional.of(ewbContract3)).when(ewbContractRepository).findByIdWithMedicalLicense(contractId3);
        doReturn(Optional.of(ewbContract4)).when(ewbContractRepository).findByIdWithMedicalLicense(contractId4);
        doReturn(Optional.empty()).when(ewbContractRepository).findByIdWithMedicalLicense(contractId5);
        doReturn(Optional.of(ewbContract)).when(ewbContractRepository).findByIdWithMedicalLicense(contractId6);
        doReturn(Optional.of(ewbContract5)).when(ewbContractRepository).findByIdWithMedicalLicense(contractId7);
        doReturn(Optional.of(ewbContract6)).when(ewbContractRepository).findByIdWithMedicalLicense(contractId8);
        doReturn(Optional.of(edfOperator)).when(edfOperatorService).getById(contractPatchDto1.getEdfOperatorId());
        doReturn(Optional.of(edfOperator)).when(edfOperatorService).getById(contractPatchDto2.getEdfOperatorId());
        doReturn(Optional.of(edfOperator)).when(edfOperatorService).getById(contractPatchDto3.getEdfOperatorId());
        doReturn(Optional.of(edfOperator)).when(edfOperatorService).getById(contractPatchDto4.getEdfOperatorId());
        doReturn(ewbContract).when(ewbContractRepository).save(ewbContractArgumentCaptor.capture());
        doReturn(organizationMedicalLicenseMessage)
                .when(organizationMedicalLicenseMapper)
                .organizationMedicalLicenseToOrganizationMedicalLicenseMessage(ewbContract.getOrganizationMedicalLicense());
        doNothing().when(organizationMedicalLicenseSender).send(organizationMedicalLicenseMessage);
        doReturn(ewbContractMessage).when(ewbContractMapper).ewbContractToEwbContractMessage(ewbContract);
        doNothing().when(ewbContractSender).send(ewbContractMessage);
        ewbContractService.edit(contractId1, contractPatchDto1);
        ewbContractService.edit(contractId2, contractPatchDto2);
        ewbContractService.edit(contractId3, contractPatchDto3);
        ewbContractService.edit(contractId1, contractPatchDto6);
        verify(organizationMedicalLicenseMapper, times(4)).organizationMedicalLicenseToOrganizationMedicalLicenseMessage(
                any(OrganizationMedicalLicense.class));
        verify(organizationMedicalLicenseSender, times(4)).send(any(OrganizationMedicalLicenseMessage.class));
        verify(ewbContractMapper, times(4)).ewbContractToEwbContractMessage(any(EwbContract.class));
        verify(ewbContractSender, times(4)).send(any(EwbContractMessage.class));
        var result = ewbContractArgumentCaptor.getAllValues();
        assertThat(result).hasSize(4);
        var actual1 = result.get(0);
        assertThat(actual1)
                .isNotNull()
                .extracting(contract -> contract.getContract().getUvhd(),
                        EwbContract::getEdfOperatorId,
                        EwbContract::getEdfCode,
                        contract -> contract.getOrganizationMedicalLicense().getSeries(),
                        contract -> contract.getOrganizationMedicalLicense().getNumber(),
                        contract -> contract.getOrganizationMedicalLicense().getIssueDate(),
                        contract -> contract.getOrganizationMedicalLicense().getExpiryDate())
                .containsExactly(contractPatchDto1.getUvhd(),
                        contractPatchDto1.getEdfOperatorId(),
                        contractPatchDto1.getEdfCode(),
                        contractPatchDto6.getContractorMedicalLicense().series(),
                        contractPatchDto6.getContractorMedicalLicense().number(),
                        contractPatchDto6.getContractorMedicalLicense().issueDate(),
                        contractPatchDto1.getContractorMedicalLicense().expiryDate());
        var actual2 = result.get(1);
        assertThat(actual2)
                .isNotNull()
                .extracting(contract -> contract.getContract().getUvhd(),
                        EwbContract::getEdfOperatorId,
                        EwbContract::getEdfCode,
                        contract -> contract.getOrganizationMedicalLicense().getSeries(),
                        contract -> contract.getOrganizationMedicalLicense().getNumber(),
                        contract -> contract.getOrganizationMedicalLicense().getIssueDate(),
                        contract -> contract.getOrganizationMedicalLicense().getExpiryDate())
                .containsExactly(contractPatchDto2.getUvhd(),
                        contractPatchDto2.getEdfOperatorId(),
                        contractPatchDto2.getEdfCode(),
                        contractPatchDto2.getContractorMedicalLicense().series(),
                        contractPatchDto2.getContractorMedicalLicense().number(),
                        contractPatchDto2.getContractorMedicalLicense().issueDate(),
                        license2.getExpiryDate());
        var actual3 = result.get(2);
        assertThat(actual3)
                .isNotNull()
                .extracting(contract -> contract.getContract().getUvhd(),
                        EwbContract::getEdfOperatorId,
                        EwbContract::getEdfCode,
                        contract -> contract.getOrganizationMedicalLicense().getSeries(),
                        contract -> contract.getOrganizationMedicalLicense().getNumber(),
                        contract -> contract.getOrganizationMedicalLicense().getIssueDate(),
                        contract -> contract.getOrganizationMedicalLicense().getExpiryDate())
                .containsExactly(contractPatchDto3.getUvhd(),
                        contractPatchDto3.getEdfOperatorId(),
                        contractPatchDto3.getEdfCode(),
                        contractPatchDto3.getContractorMedicalLicense().series(),
                        contractPatchDto3.getContractorMedicalLicense().number(),
                        license3.getIssueDate(),
                        contractPatchDto3.getContractorMedicalLicense().expiryDate());
        assertThatThrownBy(() -> ewbContractService.edit(contractId4, contractPatchDto4))
                .isInstanceOf(DateRangeValidationException.class)
                .hasMessage("Дата выдачи медицинской лицензии не может быть позже даты окончания действия");
        assertThatThrownBy(() -> ewbContractService.edit(contractId5, contractPatchDto1))
                .isInstanceOf(EwbContractNotFoundException.class)
                .hasMessage("Не найден договор на услугу Выпуск на линию %s", contractId5);
        assertThatThrownBy(() -> ewbContractService.edit(contractId6, contractPatchDto5))
                .isInstanceOf(NothingToEditException.class)
                .hasMessage("Не переданы данные для редактирования");
        assertThatThrownBy(() -> ewbContractService.edit(contractId7, contractPatchDto1))
                .isInstanceOf(EwbContractNotActiveException.class)
                .hasMessage("Не активен договор на услугу Выпуск на линию %s", contractId7);
        assertThatThrownBy(() -> ewbContractService.edit(contractId8, contractPatchDto1))
                .isInstanceOf(EwbContractNotActiveException.class)
                .hasMessage("Не активен договор на услугу Выпуск на линию %s", contractId8);
    }

    static Stream<Arguments> editMedicineTypes() {
        return Stream.of(
                Arguments.of(
                        InspectionType.MEDIC
                ),
                Arguments.of(
                        InspectionType.TELEMEDIC
                )
        );
    }

    @MethodSource
    @ParameterizedTest
    @DisplayName("Редактирование договора по медицинским услугам")
    void editMedicineTypes(InspectionType inspectionType) {
        var id = UUID.randomUUID();
        var license = Instancio.of(OrganizationMedicalLicensePatchDto.class)
                .set(field(OrganizationMedicalLicensePatchDto::series), "123")
                .set(field(OrganizationMedicalLicensePatchDto::number), "456")
                .set(field(OrganizationMedicalLicensePatchDto::issueDate), LOCAL_DATE_TIME.toLocalDate())
                .set(field(OrganizationMedicalLicensePatchDto::expiryDate), LOCAL_DATE_TIME.toLocalDate().plusYears(1))
                .create();
        var license2 = Instancio.of(OrganizationMedicalLicense.class).create();

        var contractPatchDto = Instancio.of(EwbContractPatchDto.class)
                .set(field(EwbContractPatchDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbContractPatchDto::getEdfOperatorId), "123")
                .set(field(EwbContractPatchDto::getEdfCode), "456")
                .set(field(EwbContractPatchDto::getContractorMedicalLicense), license)
                .create();
        var contract = Instancio.of(Contract.class)
                .set(field(Contract::getId), id)
                .set(field(Contract::getStart), LOCAL_DATE_TIME.toLocalDate())
                .set(field(Contract::getEnd), LOCAL_DATE_TIME.toLocalDate().plusYears(1))
                .set(field(Contract::isActive), true)
                .create();
        var ewbContract = Instancio.of(EwbContract.class)
                .set(field(EwbContract::getContractId), contract.getId())
                .set(field(EwbContract::getInspectionType), inspectionType)
                .set(field(EwbContract::getContract), contract)
                .set(field(EwbContract::getOrganizationMedicalLicense), license2)
                .create();
        var organizationMedicalLicenseMessage = Instancio.create(OrganizationMedicalLicenseMessage.class);
        var ewbContractMessage = Instancio.create(EwbContractMessage.class);

        doReturn(Optional.of(ewbContract)).when(ewbContractRepository).findByIdWithMedicalLicense(id);
        doReturn(Optional.of(Instancio.of(EdfOperator.class)
                .set(field(EdfOperator::isActive), true)
                .create())).when(edfOperatorService).getById(contractPatchDto.getEdfOperatorId());
        doReturn(ewbContract).when(ewbContractRepository).save(any(EwbContract.class));
        doReturn(organizationMedicalLicenseMessage).when(organizationMedicalLicenseMapper).organizationMedicalLicenseToOrganizationMedicalLicenseMessage(ewbContract.getOrganizationMedicalLicense());
        doReturn(ewbContractMessage).when(ewbContractMapper).ewbContractToEwbContractMessage(ewbContract);

        ewbContractService.edit(id, contractPatchDto);

        verify(ewbContractRepository).save(ewbContractArgumentCaptor.capture());
        assertThat(ewbContractArgumentCaptor.getValue()).isNotNull();
        verify(organizationMedicalLicenseSender).send(organizationMedicalLicenseMessage);
        verify(ewbContractSender).send(ewbContractMessage);
    }

    @Test
    void get() {
        var uuid = UUID.randomUUID();
        var ewbContract = Instancio.create(EwbContract.class);
        var organization = Instancio.create(Organization.class);
        var dto = Instancio.create(EwbGetContractByIdDto.class);
        when(ewbContractRepository.findByIdWithMedicalLicense(uuid))
                .thenReturn(Optional.of(ewbContract));
        when(organizationService.get(ewbContract.getOrganizationId()))
                .thenReturn(Optional.of(organization));
        when(ewbContractMapper.ewbContractToEwbGetContractByIdDto(ewbContract, organization.getOfficialName()))
                .thenReturn(dto);

        var actual = ewbContractService.get(uuid);

        verify(ewbContractRepository).findByIdWithMedicalLicense(uuid);
        verify(organizationService).get(ewbContract.getOrganizationId());
        verify(ewbContractMapper).ewbContractToEwbGetContractByIdDto(ewbContract, organization.getOfficialName());
        assertEquals(dto, actual);
    }

    @Test
    void getNotFoundExceptions() {
        var uuid = UUID.randomUUID();
        assertThatThrownBy(() -> ewbContractService.get(uuid)).isInstanceOf(EwbContractNotFoundException.class)
                .hasMessage("Не найден договор на услугу Выпуск на линию %s", uuid);

        var ewbContract = Instancio.create(EwbContract.class);
        when(ewbContractRepository.findByIdWithMedicalLicense(uuid)).thenReturn(Optional.of(ewbContract));
        assertThatThrownBy(() -> ewbContractService.get(uuid)).isInstanceOf(ContractorOrganizationNotFoundException.class)
                .hasMessage("Не найдена организация контрагента %s", ewbContract.getOrganizationId());
    }

    @Test
    void autoActivate() {
        var contract = Instancio.create(Contract.class);
        var contractId = contract.getId();
        var ewbContract = Instancio.of(EwbContract.class)
                .set(field(EwbContract::getContractId), contractId)
                .set(field(EwbContract::getContract), contract)
                .create();
        when(ewbContractRepository.findById(contractId)).thenReturn(Optional.of(ewbContract));
        ewbContract.getContract().setActive(true);
        when(ewbContractRepository.save(ewbContract)).thenAnswer(args -> args.getArgument(0));
        ewbContractService.autoActivate(contract);

        verify(ewbContractRepository).findById(contractId);
        verify(ewbContractRepository).save(ewbContract);
        verify(ewbTariffService).autoActivateAllByContractId(contractId);
    }

    @Test
    void deactivate() {
        var id1 = UUID.randomUUID();
        var id3 = UUID.randomUUID();
        var id4 = UUID.randomUUID();
        var departmentIds1 = Instancio.createList(UUID.class);
        var ewbContract1 = Instancio.of(EwbContract.class)
                .set(field(EwbContract::getContractId), id1)
                .set(field(EwbContract::getContract),
                        Instancio.of(Contract.class)
                                .set(field(Contract::getId), id1)
                                .set(field(Contract::getStart), LOCAL_DATE_TIME.toLocalDate())
                                .set(field(Contract::getEnd), LOCAL_DATE_TIME.toLocalDate().plusYears(1))
                                .set(field(Contract::isActive), true)
                                .create())
                .create();
        var ewbContract3 = Instancio.of(EwbContract.class)
                .set(field(EwbContract::getContractId), id3)
                .set(field(EwbContract::getContract),
                        Instancio.of(Contract.class)
                                .set(field(Contract::getId), id3)
                                .set(field(Contract::getStart), LOCAL_DATE_TIME.toLocalDate())
                                .set(field(Contract::getEnd), LOCAL_DATE_TIME.toLocalDate().plusYears(1))
                                .set(field(Contract::isActive), false)
                                .create())
                .create();
        var message = Instancio.create(EwbContractMessage.class);
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(Optional.of(ewbContract1)).when(ewbContractRepository).findByIdWithContract(id1);
        doReturn(Optional.of(ewbContract3)).when(ewbContractRepository).findByIdWithContract(id3);
        doReturn(Optional.empty()).when(ewbContractRepository).findByIdWithContract(id4);
        doReturn(departmentIds1).when(ewbTariffService).findActiveTariffDepartmentIds(id1);
        doNothing().when(ewbContractValidationService).validateActiveEwbExistence(departmentIds1, LocalDate.now(fixedClock).plusDays(1));
        doReturn(ewbContract1).when(ewbContractRepository).save(ewbContractArgumentCaptor.capture());
        doReturn(message).when(ewbContractMapper).ewbContractToEwbContractMessage(ewbContract1);
        doNothing().when(ewbContractSender).send(message);
        ewbContractService.deactivate(id1);
        var actual = ewbContractArgumentCaptor.getValue();
        verify(ewbContractValidationService).validateActiveEwbExistence(departmentIds1, LocalDate.now(fixedClock).plusDays(1));
        assertThat(actual.getContract().getEnd()).isEqualTo(LOCAL_DATE_TIME.toLocalDate());
        assertThat(actual.getContract().isActive()).isFalse();
        assertThatExceptionOfType(EwbContractNotActiveException.class)
                .isThrownBy(() -> ewbContractService.deactivate(id3))
                .withMessage("Не активен договор на услугу Выпуск на линию %s", id3);
        assertThatExceptionOfType(EwbContractNotFoundException.class)
                .isThrownBy(() -> ewbContractService.deactivate(id4))
                .withMessage("Не найден договор на услугу Выпуск на линию %s", id4);
    }

    @Test
    void autoDeactivate() {
        var contract = Instancio.create(Contract.class);
        var contractId = contract.getId();
        var ewbContract = Instancio.of(EwbContract.class)
                .set(field(EwbContract::getContractId), contractId)
                .set(field(EwbContract::getContract), Instancio.of(Contract.class)
                        .set(field(Contract::getId), contractId)
                        .set(field(Contract::isActive), true)
                        .create())
                .create();
        var departmentIds = List.of(UUID.randomUUID());
        doNothing().when(ewbContractValidationService).validateActiveEwbExistence(departmentIds, LocalDate.now(fixedClock));
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        when(ewbContractRepository.findByIdWithContract(contractId)).thenReturn(Optional.of(ewbContract));
        when(ewbTariffService.findActiveTariffDepartmentIds(contractId)).thenReturn(departmentIds);
        ewbContract.getContract().setActive(false);
        when(ewbContractRepository.save(ewbContract)).thenAnswer(args -> args.getArgument(0));
        ewbContractService.autoDeactivate(contract.getId());

        verify(ewbContractRepository).findByIdWithContract(contractId);
        verify(ewbTariffService).findActiveTariffDepartmentIds(contractId);
        verify(ewbContractValidationService).validateActiveEwbExistence(departmentIds, LocalDate.now(fixedClock));
        verify(ewbContractRepository).save(ewbContract);
        verify(ewbTariffService).autoDeactivateAllByContractId(contractId);
    }

    private GetEwbContractProjection createGetEwbContractWithParentContractProjection(EwbContractGetDto ewbGetContractDto) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(GetEwbContractProjection.class);
        projection.setId(ewbGetContractDto.getId());
        projection.setNumber(ewbGetContractDto.getNumber());
        projection.setStart(ewbGetContractDto.getStart());
        projection.setEnd(ewbGetContractDto.getEnd());
        projection.setActive(ewbGetContractDto.isActive());
        projection.setOrganizationName(ewbGetContractDto.getContractorOrganizationName());
        projection.setInspectionType(ewbGetContractDto.getInspectionType());
        projection.setAmount(ewbGetContractDto.getAmount());
        return projection;
    }
}