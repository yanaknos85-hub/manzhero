package ru.sber.transport.tariff_fleet.service.impl;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import ru.sber.transport.tariff_fleet.constant.ActivationType;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.constant.InspectionType;
import ru.sber.transport.tariff_fleet.database.dao.EwbTariffRepository;
import ru.sber.transport.tariff_fleet.database.model.Department;
import ru.sber.transport.tariff_fleet.database.model.EwbTariff;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.database.projection.GetEwbTariffProjection;
import ru.sber.transport.tariff_fleet.dto.PageSetting;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbSearchTariffDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffGetDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffPatchDto;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffPostDto;
import ru.sber.transport.tariff_fleet.exception.*;
import ru.sber.transport.tariff_fleet.mapper.EwbTariffMapper;
import ru.sber.transport.tariff_fleet.messaging.sender.EwbTariffSender;
import ru.sber.transport.tariff_fleet.messaging.sender.message.EwbTariffMessage;
import ru.sber.transport.tariff_fleet.service.DepartmentService;
import ru.sber.transport.tariff_fleet.service.FleetOwnerOrganizationService;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sber.transport.tariff_fleet.service.tariff.impl.EwbTariffServiceImpl;
import ru.sber.transport.tariff_fleet.service.validation.EwbTariffValidationService;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EwbTariffServiceImplTest {

    private final Clock fixedClock = Clock.fixed(
            LocalDateTime.now().toInstant(ZoneOffset.UTC),
            ZoneId.of(ZoneOffset.UTC.getId()));

    @InjectMocks
    private EwbTariffServiceImpl ewbTariffService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private FleetOwnerOrganizationService fleetOwnerOrganizationService;
    @Mock
    private EwbTariffRepository ewbTariffRepository;
    @Mock
    private EwbTariffMapper ewbTariffMapper;
    @Mock
    private EwbTariffSender ewbTariffSender;
    @Mock
    private EwbTariffValidationService ewbTariffValidationService;
    @Mock
    private Clock clock;
    @Captor
    private ArgumentCaptor<EwbTariff> ewbTariffArgumentCaptor;

    @Test
    void createTariff() {
        var postTariffDto1 = Instancio.create(EwbTariffPostDto.class);
        var postTariffDto2 = Instancio.create(EwbTariffPostDto.class);
        var postTariffDto3 = Instancio.create(EwbTariffPostDto.class);
        var postTariffDto4 = Instancio.create(EwbTariffPostDto.class);
        var postTariffDto5 = Instancio.create(EwbTariffPostDto.class);
        var department1 = Instancio.create(Department.class);
        department1.setActive(false);
        var department2 = Instancio.create(Department.class);
        department2.setActive(true);
        var department3 = Instancio.create(Department.class);
        department3.setActive(true);
        var tariff = Instancio.create(Tariff.class);
        var ewbTariff = Instancio.create(EwbTariff.class);
        var ewbTariffMessage = Instancio.create(EwbTariffMessage.class);
        doReturn(Optional.of(department1)).when(departmentService).get(postTariffDto1.getDepartmentId());
        doReturn(Optional.empty()).when(departmentService).get(postTariffDto2.getDepartmentId());
        doReturn(Optional.of(department2)).when(departmentService).get(postTariffDto3.getDepartmentId());
        doReturn(Optional.of(department3)).when(departmentService).get(postTariffDto4.getDepartmentId());
        doReturn(Optional.of(department2)).when(departmentService).get(postTariffDto5.getDepartmentId());
        doReturn(true).when(fleetOwnerOrganizationService).existsById(department2.getOrganizationId());
        doReturn(false).when(fleetOwnerOrganizationService).existsById(department3.getOrganizationId());
        doReturn(true).when(ewbTariffRepository).existsByDepartmentIdAndTariff_ContractIdAndTariff_ActiveIsTrue(postTariffDto3.getDepartmentId(),
                tariff.getContractId());
        doReturn(false).when(ewbTariffRepository).existsByDepartmentIdAndTariff_ContractIdAndTariff_ActiveIsTrue(postTariffDto5.getDepartmentId(),
                tariff.getContractId());
        doReturn(ewbTariff).when(ewbTariffMapper).ewbPostTariffDtoToEwbTariff(any(EwbTariffPostDto.class), any(Tariff.class), any(UUID.class));
        doReturn(ewbTariff).when(ewbTariffRepository).save(ewbTariff);
        doReturn(ewbTariffMessage).when(ewbTariffMapper).ewbTariffToEwbTariffMessage(any(EwbTariff.class));
        doNothing().when(ewbTariffSender).send(ewbTariffMessage);
        assertThatThrownBy(() -> ewbTariffService.createTariff(postTariffDto1, tariff, InspectionType.TECHNIC))
                .isInstanceOf(FleetOwnerDepartmentNotActiveException.class)
                .hasMessage("Не активно подразделение владельца автопарка %s", postTariffDto1.getDepartmentId());
        assertThatThrownBy(() -> ewbTariffService.createTariff(postTariffDto2, tariff, InspectionType.TECHNIC))
                .isInstanceOf(FleetOwnerDepartmentNotFoundException.class)
                .hasMessage("Не найдено подразделение владельца автопарка %s", postTariffDto2.getDepartmentId());
        assertThatThrownBy(() -> ewbTariffService.createTariff(postTariffDto3, tariff, InspectionType.TECHNIC))
                .isInstanceOf(EwbTariffAlreadyExistsException.class)
                .hasMessage("У подразделения владельца автопарка уже имеется тариф по договору %s", tariff.getContractId());
        assertThatThrownBy(() -> ewbTariffService.createTariff(postTariffDto4, tariff, InspectionType.TECHNIC))
                .isInstanceOf(FleetOwnerOrganizationNotFoundException.class)
                .hasMessage("Не найдена организация владельца автопарка %s", department3.getOrganizationId());
        ewbTariffService.createTariff(postTariffDto5, tariff, InspectionType.TECHNIC);
        verify(departmentService, times(5)).get(any(UUID.class));
        verify(fleetOwnerOrganizationService, times(3)).existsById(any(UUID.class));
        verify(ewbTariffRepository, times(2)).existsByDepartmentIdAndTariff_ContractIdAndTariff_ActiveIsTrue(any(UUID.class), any(UUID.class));
        verify(ewbTariffMapper).ewbPostTariffDtoToEwbTariff(any(EwbTariffPostDto.class), any(Tariff.class), any(UUID.class));
        verify(ewbTariffRepository).save(any(EwbTariff.class));
        verify(ewbTariffMapper).ewbTariffToEwbTariffMessage(any(EwbTariff.class));
        verify(ewbTariffSender).send(any(EwbTariffMessage.class));

        var postTariffDtoMedicineException = Instancio.create(EwbTariffPostDto.class);
        var departmentMedicineException = Instancio.of(Department.class)
                .set(Select.field(Department::isActive), true)
                .create();
        doReturn(Optional.of(departmentMedicineException)).when(departmentService).get(postTariffDtoMedicineException.getDepartmentId());
        doReturn(true).when(fleetOwnerOrganizationService).existsById(departmentMedicineException.getOrganizationId());
        doReturn(false).when(ewbTariffRepository).existsByDepartmentIdAndTariff_ContractIdAndTariff_ActiveIsTrue(postTariffDtoMedicineException.getDepartmentId(), tariff.getContractId());
        doReturn(true).when(ewbTariffRepository).existsByDepartmentIdAndActiveIsTrueAndMedicineContractType(postTariffDtoMedicineException.getDepartmentId());
        assertThatThrownBy(() -> ewbTariffService.createTariff(postTariffDtoMedicineException, tariff, InspectionType.TELEMEDIC))
                .isInstanceOf(MedicineTariffAlreadyExistsException.class)
                .hasMessage("Подразделение id=%s уже имеет тариф на медицинские услуги".formatted(postTariffDtoMedicineException.getDepartmentId()));
    }

    @Test
    void search() {
        var pageSetting = new PageSetting(0, 10);
        var notExistsId = UUID.randomUUID();
        var organization = Instancio.create(Organization.class);
        var contractOrganization = Instancio.create(Organization.class);
        var department = Instancio.create(Department.class);
        department.setActive(true);
        department.setOrganizationId(organization.getId());
        var ewbSearchTariffDto1 = Instancio.of(EwbSearchTariffDto.class)
                .set(field(EwbSearchTariffDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbSearchTariffDto::getActive), true)
                .set(field(EwbSearchTariffDto::getOrganizationId), organization.getId())
                .set(field(EwbSearchTariffDto::getDepartmentId), department.getId())
                .set(field(EwbSearchTariffDto::getContractOrganizationId), contractOrganization.getId())
                .set(field(EwbSearchTariffDto::getPageSetting), pageSetting)
                .create();
        var ewbSearchTariffDto2 = Instancio.of(EwbSearchTariffDto.class)
                .set(field(EwbSearchTariffDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbSearchTariffDto::getActive), true)
                .set(field(EwbSearchTariffDto::getOrganizationId), organization.getId())
                .set(field(EwbSearchTariffDto::getDepartmentId), department.getId())
                .set(field(EwbSearchTariffDto::getContractOrganizationId), notExistsId)
                .set(field(EwbSearchTariffDto::getPageSetting), pageSetting)
                .create();
        var ewbSearchTariffDto3 = Instancio.of(EwbSearchTariffDto.class)
                .set(field(EwbSearchTariffDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbSearchTariffDto::getActive), true)
                .set(field(EwbSearchTariffDto::getOrganizationId), notExistsId)
                .set(field(EwbSearchTariffDto::getDepartmentId), department.getId())
                .set(field(EwbSearchTariffDto::getContractOrganizationId), contractOrganization.getId())
                .set(field(EwbSearchTariffDto::getPageSetting), pageSetting)
                .create();
        var ewbSearchTariffDto4 = Instancio.of(EwbSearchTariffDto.class)
                .set(field(EwbSearchTariffDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbSearchTariffDto::getActive), true)
                .set(field(EwbSearchTariffDto::getOrganizationId), organization.getId())
                .set(field(EwbSearchTariffDto::getDepartmentId), notExistsId)
                .set(field(EwbSearchTariffDto::getContractOrganizationId), contractOrganization.getId())
                .set(field(EwbSearchTariffDto::getPageSetting), pageSetting)
                .create();
        var ewbSearchTariffDto5 = Instancio.of(EwbSearchTariffDto.class)
                .set(field(EwbSearchTariffDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbSearchTariffDto::getActive), true)
                .set(field(EwbSearchTariffDto::getOrganizationId), contractOrganization.getId())
                .set(field(EwbSearchTariffDto::getDepartmentId), department.getId())
                .set(field(EwbSearchTariffDto::getContractOrganizationId), contractOrganization.getId())
                .set(field(EwbSearchTariffDto::getPageSetting), pageSetting)
                .create();
        var ewbSearchTariffDto6 = Instancio.of(EwbSearchTariffDto.class)
                .set(field(EwbSearchTariffDto::getDocumentType), DocumentType.EWB)
                .set(field(EwbSearchTariffDto::getActive), true)
                .set(field(EwbSearchTariffDto::getOrganizationId), null)
                .set(field(EwbSearchTariffDto::getDepartmentId), null)
                .set(field(EwbSearchTariffDto::getContractOrganizationId), null)
                .set(field(EwbSearchTariffDto::getPageSetting), pageSetting)
                .create();
        var expected1 = Instancio.create(EwbTariffGetDto.class);
        var expected2 = Instancio.create(EwbTariffGetDto.class);
        var pageRequest = PageRequest.of(0, 10);
        var expected = new PageImpl<>(List.of(expected1, expected2), pageRequest, 2);
        var projection1 = createGetEwbTariffProjection(expected1);
        var projection2 = createGetEwbTariffProjection(expected2);
        var projectionPageable = new PageImpl<>(List.of(projection1, projection2), pageRequest, 2);
        doReturn(true).when(organizationService).existsById(contractOrganization.getId());
        doReturn(false).when(organizationService).existsById(notExistsId);
        doReturn(true).when(fleetOwnerOrganizationService).existsById(organization.getId());
        doReturn(true).when(fleetOwnerOrganizationService).existsById(contractOrganization.getId());
        doReturn(false).when(fleetOwnerOrganizationService).existsById(notExistsId);
        doReturn(Optional.of(department)).when(departmentService).get(department.getId());
        doReturn(Optional.empty()).when(departmentService).get(notExistsId);
        doReturn(projectionPageable).when(ewbTariffRepository).searchEwbTariffs(any(),
                any(),
                any(),
                anyString(),
                anyString(),
                anyBoolean(),
                any(Pageable.class));
        doReturn(List.of(expected1, expected2))
                .when(ewbTariffMapper).listGetEwbTariffProjectionToListEwbGetTariffDto(List.of(projection1, projection2));
        var actual1 = ewbTariffService.search(ewbSearchTariffDto1);
        assertThat(actual1)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        var actual2 = ewbTariffService.search(ewbSearchTariffDto6);
        assertThat(actual2)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThatThrownBy(() -> ewbTariffService.search(ewbSearchTariffDto2))
                .isInstanceOf(ContractorOrganizationNotFoundException.class)
                .hasMessage("Не найдена организация контрагента %s", notExistsId);
        assertThatThrownBy(() -> ewbTariffService.search(ewbSearchTariffDto3))
                .isInstanceOf(FleetOwnerOrganizationNotFoundException.class)
                .hasMessage("Не найдена организация владельца автопарка %s", notExistsId);
        assertThatThrownBy(() -> ewbTariffService.search(ewbSearchTariffDto4))
                .isInstanceOf(FleetOwnerDepartmentNotFoundException.class)
                .hasMessage("Не найдено подразделение владельца автопарка %s", notExistsId);
        assertThatThrownBy(() -> ewbTariffService.search(ewbSearchTariffDto5))
                .isInstanceOf(DepartmentInOrganizationNotFoundException.class)
                .hasMessage("Подразделение %s не найдено в организации с id:%s", department.getId(), contractOrganization.getId());
        verify(organizationService, times(5)).existsById(any(UUID.class));
        verify(fleetOwnerOrganizationService, times(4)).existsById(any(UUID.class));
        verify(departmentService, times(3)).get(any(UUID.class));
        verify(ewbTariffRepository, times(2)).searchEwbTariffs(any(),
                any(),
                any(),
                anyString(),
                anyString(),
                anyBoolean(),
                any(Pageable.class));
        verify(ewbTariffMapper, times(2)).listGetEwbTariffProjectionToListEwbGetTariffDto(any());
    }

    @Test
    void edit() {
        var tariffId = UUID.randomUUID();
        var wrongTariffId = UUID.randomUUID();
        var ewbTariff = Instancio.of(EwbTariff.class)
                .set(field(EwbTariff::getAmount), 123L)
                .create();
        var savedTariff = Instancio.of(EwbTariff.class)
                .set(field(EwbTariff::getAmount), 456L)
                .create();
        var ewbTariffMessage = Instancio.create(EwbTariffMessage.class);
        var ewbTariffPatchDto = Instancio.of(EwbTariffPatchDto.class)
                .set(field(EwbTariffPatchDto::getAmount), 456L)
                .create();
        doReturn(Optional.of(ewbTariff)).when(ewbTariffRepository).findById(tariffId);
        doReturn(Optional.empty()).when(ewbTariffRepository).findById(wrongTariffId);
        doReturn(savedTariff).when(ewbTariffRepository).save(ewbTariffArgumentCaptor.capture());
        doReturn(ewbTariffMessage).when(ewbTariffMapper).ewbTariffToEwbTariffMessage(savedTariff);
        doNothing().when(ewbTariffSender).send(ewbTariffMessage);
        ewbTariffService.edit(tariffId, ewbTariffPatchDto);
        verify(ewbTariffMapper).ewbTariffToEwbTariffMessage(any(EwbTariff.class));
        verify(ewbTariffSender).send(any(EwbTariffMessage.class));
        verify(ewbTariffRepository).save(any(EwbTariff.class));
        var actual = ewbTariffArgumentCaptor.getValue();
        assertThat(actual.getAmount()).isEqualTo(456L);
        assertThatExceptionOfType(EwbTariffNotFoundException.class)
                .isThrownBy(() -> ewbTariffService.edit(wrongTariffId, ewbTariffPatchDto))
                .withMessage("Не найден тариф на услугу Выпуск на линию id:%s", wrongTariffId);
    }

    @Test
    void getException() {
        var tariffId = UUID.randomUUID();
        assertThatThrownBy(() -> ewbTariffService.get(tariffId))
                .isInstanceOf(EwbTariffNotFoundException.class)
                .hasMessage("Не найден тариф на услугу Выпуск на линию id:%s", tariffId);
    }

    @Test
    void findActiveTariffDepartmentIds() {
        var contractId = UUID.randomUUID();
        var departmentIds = Instancio.createList(UUID.class);
        doReturn(departmentIds).when(ewbTariffRepository).findAllActiveTariffDepartmentIdsByContractId(contractId);
        assertThat(ewbTariffService.findActiveTariffDepartmentIds(contractId)).isEqualTo(departmentIds);
    }

    @Test
    void autoActivateAllByContractId() {
        var contractId = UUID.randomUUID();
        var ewbTariff = Instancio.of(EwbTariff.class)
                .set(field(EwbTariff::getTariff), Instancio.of(Tariff.class)
                        .set(field(Tariff::getContractId), contractId)
                        .set(field(Tariff::isActive), false)
                        .create())
                .create();
        var ewbTariffList = List.of(ewbTariff);
        when(ewbTariffRepository.findNotActiveNotManualByContractIdWithTariff(contractId))
                .thenReturn(ewbTariffList);
        ewbTariff.getTariff().setActive(true);
        when(ewbTariffRepository.saveAll(ewbTariffList)).thenAnswer(args -> args.getArgument(0));
        ewbTariffService.autoActivateAllByContractId(contractId);

        verify(ewbTariffRepository).findNotActiveNotManualByContractIdWithTariff(contractId);
        verify(ewbTariffRepository).saveAll(ewbTariffList);
    }

    @Test
    void deactivate() {
        var id1 = UUID.randomUUID();
        var id3 = UUID.randomUUID();
        var id4 = UUID.randomUUID();
        var ewbTariff1 = Instancio.of(EwbTariff.class)
                .set(field(EwbTariff::getTariffId), id1)
                .set(field(EwbTariff::getTariff),
                        Instancio.of(Tariff.class)
                                .set(field(Tariff::getId), id1)
                                .set(field(Tariff::getActivationType), ActivationType.AUTO)
                                .set(field(Tariff::isActive), true)
                                .create())
                .create();
        var ewbTariff3 = Instancio.of(EwbTariff.class)
                .set(field(EwbTariff::getTariffId), id3)
                .set(field(EwbTariff::getTariff),
                        Instancio.of(Tariff.class)
                                .set(field(Tariff::getId), id3)
                                .set(field(Tariff::getActivationType), ActivationType.AUTO)
                                .set(field(Tariff::isActive), false)
                                .create())
                .create();
        var departmentIds1 = Collections.singletonList(ewbTariff1.getDepartmentId());
        var message = Instancio.create(EwbTariffMessage.class);
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doReturn(Optional.of(ewbTariff1)).when(ewbTariffRepository).findByIdWithTariff(id1);
        doReturn(Optional.of(ewbTariff3)).when(ewbTariffRepository).findByIdWithTariff(id3);
        doReturn(Optional.empty()).when(ewbTariffRepository).findByIdWithTariff(id4);
        doNothing().when(ewbTariffValidationService).validateActiveEwbExistence(departmentIds1, LocalDate.now().plusDays(1));
        doReturn(ewbTariff1).when(ewbTariffRepository).save(ewbTariffArgumentCaptor.capture());
        doReturn(message).when(ewbTariffMapper).ewbTariffToEwbTariffMessage(ewbTariff1);
        doNothing().when(ewbTariffSender).send(message);
        ewbTariffService.deactivate(id1);
        var actual = ewbTariffArgumentCaptor.getValue();
        assertThat(actual.getTariff().getActivationType()).isEqualTo(ActivationType.MANUAL);
        assertThat(actual.getTariff().isActive()).isFalse();
        assertThatExceptionOfType(EwbTariffNotActiveException.class)
                .isThrownBy(() -> ewbTariffService.deactivate(id3))
                .withMessage("Не активен тариф на услугу Выпуск на линию %s", id3);
        assertThatExceptionOfType(EwbTariffNotFoundException.class)
                .isThrownBy(() -> ewbTariffService.deactivate(id4))
                .withMessage("Не найден тариф на услугу Выпуск на линию id:%s", id4);
    }

    @Test
    void autoDeactivateAllByContractId() {
        var contractId = UUID.randomUUID();
        var ewbTariff = Instancio.of(EwbTariff.class)
                .set(field(EwbTariff::getTariff), Instancio.of(Tariff.class)
                        .set(field(Tariff::getContractId), contractId)
                        .set(field(Tariff::getActivationType), ActivationType.MANUAL)
                        .set(field(Tariff::isActive), true)
                        .create())
                .create();
        var departmentIds = List.of(ewbTariff.getDepartmentId());
        when(ewbTariffRepository.findActiveByContractIdWithTariff(contractId))
                .thenReturn(List.of(ewbTariff));
        ewbTariff.getTariff()
                .setActive(false)
                .setActivationType(ActivationType.AUTO);
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        doNothing().when(ewbTariffValidationService).validateActiveEwbExistence(departmentIds, LocalDate.now());
        when(ewbTariffRepository.saveAll(List.of(ewbTariff))).thenAnswer(args -> args.getArgument(0));
        ewbTariffService.autoDeactivateAllByContractId(contractId);

        verify(ewbTariffRepository).findActiveByContractIdWithTariff(contractId);
        verify(ewbTariffRepository).saveAll(List.of(ewbTariff));
    }

    private GetEwbTariffProjection createGetEwbTariffProjection(EwbTariffGetDto source) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(GetEwbTariffProjection.class);
        projection.setId(source.getId());
        projection.setHumanReadableId(source.getHumanReadableId());
        projection.setActive(source.isActive());
        projection.setInspectionType(source.getInspectionType());
        projection.setOrganizationName(source.getInspectionType());
        projection.setDepartmentName(source.getDepartmentName());
        projection.setContractOrganizationName(source.getContractOrganizationName());
        projection.setContractNumber(source.getContractNumber());
        return projection;
    }
}