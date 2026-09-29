package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.approvals.database.model.Department;
import ru.sberbank.ditsib.transport.approvals.database.model.Employee;
import ru.sberbank.ditsib.transport.approvals.database.model.OtherTrTypesApprovalsSettings;
import ru.sberbank.ditsib.transport.approvals.database.model.PublicTrApprovalsSettings;
import ru.sberbank.ditsib.transport.approvals.services.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalsSettingsInjectionServiceImpl implements ApprovalsSettingsInjectionService {
    
    private final GroupTransferApprovalsSettingsServiceImpl groupTransferApprovalsSettingsService;
    private final TaxiApprovalsSettingsServiceImpl taxiApprovalsSettingsService;
    private final OtherTrTypesApprovalsSettingsServiceImpl otherTypesApprovalsSettingsService;
    private final PublicTrApprovalsSettingsServiceImpl publicApprovalsSettingsService;
    private final TariffService tariffService;
    private final TaxiTariffService taxiTariffService;
    private final EmployeeService employeeService;
    private final DepartmentService departmentService;
    
    @Override
    public boolean isAutoApproveByApprovalsSettings(
            UUID tariffId, @NonNull String transportType,
            double cost, @NonNull UUID purposeId, @NonNull UUID employeeId, UUID requestId
                                                   ) {
        if (tariffId == null && !TransportTypeEnum.PUBLIC.name().equals(transportType)) {
            throw new IllegalStateException("tariffId must not be null");
        }
        try {
            // стоимость double в БД, однако запись идет в коп. Переведем в int
            int costKop = (int) cost;
            UUID organizationId;
            UUID regionId;
            if (TransportTypeEnum.TAXI.name().equals(transportType)) {
                final var taxiTariff = taxiTariffService.getTariffById(tariffId);
                organizationId = taxiTariff.getOrganizationId();
                regionId = taxiTariff.getRegionId();
            } else if (TransportTypeEnum.PUBLIC.name().equals(transportType)) {
                // для ОТ попытаться сначала найти тариф, если не найден - заполнить параметры с пом. employeeId
                var tariff = tariffId == null ? null : tariffService.getOptionalById(tariffId).orElse(null);
                if (tariff != null) {
                    organizationId = tariff.getOrganizationId();
                    regionId = tariff.getRegionId();
                } else {
                    var employee = employeeService.get(employeeId).orElseThrow();
                    var department = departmentService.get(employee.getDepartmentId()).orElseThrow();
                    organizationId = department.getOrganizationId();
                    regionId = null;
                }
            } else {
                var tariff = tariffService.getTariffById(tariffId);
                organizationId = tariff.getOrganizationId();
                regionId = tariff.getRegionId();
            }
            //По корп.клиенту и типу транспорта взять соответствующие настройки
            var settings = switch (transportType) {
                case "TAXI" -> taxiApprovalsSettingsService.get(organizationId);
                case "PUBLIC" -> publicApprovalsSettingsService.get(organizationId);
                case "GROUP_TRANSFER" -> groupTransferApprovalsSettingsService.get(organizationId);
                default -> otherTypesApprovalsSettingsService.get(organizationId, transportType);
            };
            // проверить по настройкам, будет ли автоаппрув. Приоритет настроек:
            // 1 - основная настройка - флаг, нужен ли этап согласования. Если нет - автоаппрув
            log.info("Найдены настройки по заявке {} {}", requestId, settings);
            log.info("Автоаппрув ({}) - согласование по флагу {}", transportType, requestId);
            if (!settings.isApprovalActive()) {
                log.info("Автоаппрув ({}) - согласование пропускается по флагу {}", transportType, requestId);
                return true;
            }
            // 2 - если нет доп.настроек, то сравниться по стоимости
            log.info("Автоаппрув ({}) - согласование по основной сумме {}", transportType, requestId);
            if (settings.getPurposeAndRegionItems() == null || settings.getPurposeAndRegionItems().isEmpty()) {
                log.info("Автоаппрув ({}) - согласование {} по основной сумме {} < {} {}",
                         transportType,
                         costKop < (int) settings.getMinCostToBeApproved() ? "пропускается" : "не пропускается",
                         costKop,
                         (int) settings.getMinCostToBeApproved(),
                         requestId);
                return costKop < (int) settings.getMinCostToBeApproved();
            }
            // 3 - если есть доп.настройки, отфильтровать сначала по геозоне и цели
            // регион может быть null, такие настройки отфильтровать в первую очередь
            log.info("Автоаппрув ({}) - согласование по доп.настройке (цель + регион) {}", transportType, requestId);
            var item = settings.getPurposeAndRegionItems()
                            .stream()
                            .filter(i -> i.getRegion() != null && i.getRegion().getId().equals(regionId) &&
                                         i.getTripPurpose().getId().equals(purposeId))
                            .findFirst().orElse(null);
            if (item != null) {
                log.info("Автоаппрув ({}) - согласование {} по доп.настройке (цель + регион) {} < {} {}",
                         transportType,
                         costKop < (int) item.getMinCostToBeApproved() ? "пропускается" : "не пропускается",
                         costKop,
                         (int) item.getMinCostToBeApproved(),
                         requestId);
                return costKop < (int) item.getMinCostToBeApproved();
            }
            // 4 - затем отфильтровать по геозоне == null и цели
            log.info("Автоаппрув ({}) - согласование по доп.настройке (цель + любой регион) {}", transportType, requestId);
            item = settings.getPurposeAndRegionItems()
                           .stream()
                           .filter(i -> i.getRegion() == null && i.getTripPurpose().getId().equals(purposeId))
                           .findFirst().orElse(null);
            if (item != null) {
                log.info("Автоаппрув ({}) - согласование {} по доп.настройке (цель + любой регион) {} < {} {}",
                         transportType,
                         costKop < (int) item.getMinCostToBeApproved() ? "пропускается" : "не пропускается",
                         costKop,
                         (int) item.getMinCostToBeApproved(),
                         requestId);
                return costKop < (int) item.getMinCostToBeApproved();
            }
            // 5 - если не сработал ни один кейс - сравниться по основной стоимости
            log.info("Автоаппрув ({}) - согласование {} по основной сумме {} < {} {}",
                     transportType,
                     costKop < (int) settings.getMinCostToBeApproved() ? "пропускается" : "не пропускается",
                     costKop,
                     (int) settings.getMinCostToBeApproved(),
                     requestId);
            return costKop < (int) settings.getMinCostToBeApproved();
        } catch (Exception e) {
            // если настройки не найдены, либо какие-то ошибки в данных, то вернуть без аппрува
            log.error("Автоаппрув ({}) - согласование не пропускается из-за ошибки {}", transportType, requestId, e);
            return false;
        }
    }

    @Override
    public boolean isPublicTrConfirmationDocumentRequired(@NonNull UUID employeeId) {
        return getPublicApprovalsSettingsFlag(employeeId,
                                              PublicApprovalsSettingsFlagType.TRIP_CONFIRMATION_DOCUMENT_CHECK);
    }
    
    @Override
    public boolean isPublicTrAffirmativeRequired(@NonNull UUID employeeId) {
        return getPublicApprovalsSettingsFlag(employeeId, PublicApprovalsSettingsFlagType.AFFIRMATIVE_ACTIVE);
    }
    
    @Override
    public boolean isOtherTrFinalTripConfirmation(UUID employeeId, String transportType) {
        try {
            // извлечь настройки
            Employee employee = employeeService.get(employeeId).orElseThrow();
            Department department = departmentService.get(employee.getDepartmentId()).orElseThrow();
            UUID organizationId = department.getOrganizationId();
            OtherTrTypesApprovalsSettings settings = otherTypesApprovalsSettingsService.get(organizationId, transportType);
            return settings.isTripApprovalActive();
        } catch (Throwable ex) {
            // если настройки не найдены, либо какие-то ошибки в данных, то вернуть без аппрува
            if (ex.getMessage() != null) {
                log.warn(ex.getMessage());
            }
            // вернуть true ( настройки отменяют этап по false)
            return true;
        }
    }
    
    /**
     * Выбрать из настроек для ОТ одну по ее типу
     *
     * @param employeeId ID сотрудника
     * @param flagType тип поля из настроек
     *
     * @return значение поля из настроек
     */
    private boolean getPublicApprovalsSettingsFlag(UUID employeeId, PublicApprovalsSettingsFlagType flagType) {
        try {
            // извлечь настройки
            Employee employee = employeeService.get(employeeId).orElseThrow();
            Department department = departmentService.get(employee.getDepartmentId()).orElseThrow();
            UUID organizationId = department.getOrganizationId();
            PublicTrApprovalsSettings settings = publicApprovalsSettingsService.get(organizationId);
            // выбрать из настроек нужную
            return switch (flagType) {
                case APPROVAL_DOCUMENT_CHECK -> settings.isApprovalDocumentCheck();
                case TRIP_CONFIRMATION_DOCUMENT_CHECK -> settings.isTripConfirmationDocumentCheck();
                case TRIP_CONFIRMATION_ACTIVE -> settings.isTripConfirmationActive();
                case AFFIRMATIVE_ACTIVE -> settings.isAffirmativeActive();
            };
        } catch (Throwable ex) {
            // если настройки не найдены, либо какие-то ошибки в данных, то вернуть без аппрува
            if (ex.getMessage() != null) {
                log.warn(ex.getMessage());
            }
            // вернуть true (т.к. все настройки отменяют этап по false)
            return true;
        }
    }
    
    /**
     * Тип флага из настроек согласований для ОТ
     */
    private enum PublicApprovalsSettingsFlagType {
        APPROVAL_DOCUMENT_CHECK,
        TRIP_CONFIRMATION_DOCUMENT_CHECK,
        TRIP_CONFIRMATION_ACTIVE,
        AFFIRMATIVE_ACTIVE
    }
}
