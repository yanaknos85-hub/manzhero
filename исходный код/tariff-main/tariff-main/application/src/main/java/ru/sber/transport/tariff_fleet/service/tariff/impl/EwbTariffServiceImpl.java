package ru.sber.transport.tariff_fleet.service.tariff.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.constant.ActivationType;
import ru.sber.transport.tariff_fleet.constant.InspectionType;
import ru.sber.transport.tariff_fleet.database.dao.EwbTariffRepository;
import ru.sber.transport.tariff_fleet.database.model.Department;
import ru.sber.transport.tariff_fleet.database.model.EwbTariff;
import ru.sber.transport.tariff_fleet.database.model.Tariff;
import ru.sber.transport.tariff_fleet.dto.ewb.*;
import ru.sber.transport.tariff_fleet.exception.*;
import ru.sber.transport.tariff_fleet.mapper.EwbTariffMapper;
import ru.sber.transport.tariff_fleet.messaging.sender.EwbTariffSender;
import ru.sber.transport.tariff_fleet.service.DepartmentService;
import ru.sber.transport.tariff_fleet.service.FleetOwnerOrganizationService;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sber.transport.tariff_fleet.service.tariff.EwbTariffService;
import ru.sber.transport.tariff_fleet.service.validation.EwbTariffValidationService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class EwbTariffServiceImpl implements EwbTariffService {
    private final DepartmentService departmentService;
    private final OrganizationService organizationService;
    private final FleetOwnerOrganizationService fleetOwnerOrganizationService;
    private final EwbTariffValidationService ewbTariffValidationService;
    private final EwbTariffRepository ewbTariffRepository;
    private final EwbTariffMapper ewbTariffMapper;
    private final EwbTariffSender ewbTariffSender;
    private final Clock clock;

    @Override
    public void createTariff(EwbTariffPostDto postTariffDto, Tariff tariff, InspectionType inspectionType) {
        var department = getFleetOwnerDepartment(postTariffDto.getDepartmentId());
        if (!department.isActive()) {
            throw new FleetOwnerDepartmentNotActiveException(postTariffDto.getDepartmentId());
        }
        checkFleetOwnerOrganizationExists(department.getOrganizationId());
        if (ewbTariffRepository.existsByDepartmentIdAndTariff_ContractIdAndTariff_ActiveIsTrue(postTariffDto.getDepartmentId(), tariff.getContractId())) {
            throw new EwbTariffAlreadyExistsException(tariff.getContractId());
        }
        if (InspectionType.getMedicineTypes().contains(inspectionType) &&
                ewbTariffRepository.existsByDepartmentIdAndActiveIsTrueAndMedicineContractType(postTariffDto.getDepartmentId())) {
            throw new MedicineTariffAlreadyExistsException(postTariffDto.getDepartmentId());
        }
        var saved = ewbTariffRepository.save(ewbTariffMapper.ewbPostTariffDtoToEwbTariff(postTariffDto, tariff, department.getOrganizationId()));
        sendToKafka(saved);
    }

    @Override
    public Page<EwbTariffGetDto> search(EwbSearchTariffDto searchTariffDto) {
        checkContractOrganizationId(searchTariffDto.getContractOrganizationId());
        if (searchTariffDto.getOrganizationId() != null) {
            checkFleetOwnerOrganizationExists(searchTariffDto.getOrganizationId());
        }
        if (searchTariffDto.getDepartmentId() != null) {
            var department = getFleetOwnerDepartment(searchTariffDto.getDepartmentId());
            if (searchTariffDto.getOrganizationId() != null && !department.getOrganizationId().equals(searchTariffDto.getOrganizationId())) {
                throw new DepartmentInOrganizationNotFoundException(searchTariffDto.getDepartmentId(), searchTariffDto.getOrganizationId());
            }
        }
        var result = ewbTariffRepository.searchEwbTariffs(searchTariffDto.getOrganizationId(),
                searchTariffDto.getDepartmentId(),
                searchTariffDto.getContractOrganizationId(),
                getInspectionType(searchTariffDto.getInspectionType()),
                searchTariffDto.getHumanReadableId(),
                searchTariffDto.getActive(),
                searchTariffDto.getPageRequest(searchTariffDto));
        var mappedResult = ewbTariffMapper.listGetEwbTariffProjectionToListEwbGetTariffDto(result.getContent());
        return new PageImpl<>(
                mappedResult,
                result.getPageable(),
                result.getTotalElements());
    }

    @Override
    @Transactional
    public void edit(UUID tariffId, EwbTariffPatchDto tariffPatchDto) {
        var ewbTariff = ewbTariffRepository.findById(tariffId)
                .orElseThrow(() -> new EwbTariffNotFoundException(tariffId));
        ewbTariff.setAmount(tariffPatchDto.getAmount());
        var saved = ewbTariffRepository.save(ewbTariff);
        sendToKafka(saved);
    }

    @Override
    public EwbTariffGetByIdDto get(UUID tariffId) {
        return ewbTariffRepository.findByTariffId(tariffId)
                .map(ewbTariffMapper::getEwbTariffByIdProjectionToEwbTariffGetByIdDto)
                .orElseThrow(() -> new EwbTariffNotFoundException(tariffId));
    }

    @Override
    public List<UUID> findActiveTariffDepartmentIds(UUID contractId) {
        return ewbTariffRepository.findAllActiveTariffDepartmentIdsByContractId(contractId);
    }

    @Override
    @Transactional
    public List<EwbTariff> autoActivateAllByContractId(UUID contractId) {
        var ewbTariffList = ewbTariffRepository.findNotActiveNotManualByContractIdWithTariff(contractId);
        ewbTariffList.forEach(ewbTariff -> ewbTariff.getTariff().setActive(true));
        return ewbTariffRepository.saveAll(ewbTariffList);
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        var ewbTariff = ewbTariffRepository.findByIdWithTariff(id)
                .orElseThrow(() -> new EwbTariffNotFoundException(id));
        if (!ewbTariff.getTariff().isActive()) {
            throw new EwbTariffNotActiveException(id);
        }
        var departmentIds = Collections.singletonList(ewbTariff.getDepartmentId());
        ewbTariffValidationService.validateActiveEwbExistence(departmentIds, LocalDate.now(clock).plusDays(1));
        ewbTariff.getTariff().setActive(false)
                .setActivationType(ActivationType.MANUAL);
        var saved = ewbTariffRepository.save(ewbTariff);
        sendToKafka(saved);
    }

    @Override
    @Transactional
    public List<EwbTariff> autoDeactivateAllByContractId(UUID contractId) {
        var ewbTariffList = ewbTariffRepository.findActiveByContractIdWithTariff(contractId);
        var departmentIds = new ArrayList<UUID>();
        for (var ewbTariff : ewbTariffList) {
            departmentIds.add(ewbTariff.getDepartmentId());
            ewbTariff.getTariff().setActive(false)
                    .setActivationType(ActivationType.AUTO);
        }
        ewbTariffValidationService.validateActiveEwbExistence(departmentIds, LocalDate.now(clock));
        return ewbTariffRepository.saveAll(ewbTariffList);
    }

    private void sendToKafka(EwbTariff tariff) {
        ewbTariffSender.send(ewbTariffMapper.ewbTariffToEwbTariffMessage(tariff));
    }

    /**
     * Если прислан фильтр по организации договора, то проверяем наличие этой организации
     *
     * @param contractOrganizationId Идентификатор записи об организации договора
     */
    private void checkContractOrganizationId(UUID contractOrganizationId) {
        if (contractOrganizationId != null && !organizationService.existsById(contractOrganizationId)) {
            throw new ContractorOrganizationNotFoundException(contractOrganizationId);
        }
    }

    /**
     * Проверяем, существует ли организация владельца автопарка по идентификатору
     *
     * @param id Идентификатор записи об организации владельца автопарка
     * @throws FleetOwnerOrganizationNotFoundException ошибка, если организация отсутствует
     */
    private void checkFleetOwnerOrganizationExists(UUID id) {
        if (!fleetOwnerOrganizationService.existsById(id)) {
            throw new FleetOwnerOrganizationNotFoundException(id);
        }
    }

    /**
     * Получаем подразделение владельца автопарка, или возвращаем ошибку
     *
     * @param departmentId Индентификатор записи о подразделении
     * @return {@link Department}
     * @throws FleetOwnerDepartmentNotFoundException ошибка, если подразделение отсутствует
     */
    private Department getFleetOwnerDepartment(UUID departmentId) {
        return departmentService.get(departmentId).orElseThrow(() -> new FleetOwnerDepartmentNotFoundException(departmentId));
    }

    private static String getInspectionType(InspectionType inspectionType) {
        return inspectionType == null ? null : inspectionType.name();
    }
}
