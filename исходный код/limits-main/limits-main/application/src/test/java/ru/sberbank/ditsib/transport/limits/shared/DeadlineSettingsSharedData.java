package ru.sberbank.ditsib.transport.limits.shared;

import ru.sber.transport.deadline.messaging.DeadlineSettingsMessage;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitRequest;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;
import ru.sberbank.ditsib.transport.limits.model.limit.PeriodData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Экземпляры и методы для тестирования настроек КС
 */
public class DeadlineSettingsSharedData {
    
    /**
     * Вернуть тестовый экземпляр DeadlineSettingsMessage.LimitDeadlineSettingsItem
     *
     * @param limitType тип лимита
     *
     * @return DeadlineSettingsMessage.LimitDeadlineSettingsItem
     */
    public DeadlineSettingsMessage.LimitDeadlineSettingsItem createTestLimitDeadlineSettingsItem(LimitType limitType) {
        return new DeadlineSettingsMessage.LimitDeadlineSettingsItem(
                UUID.randomUUID(),
                ChronoUnit.MILLIS,
                1800,
                limitType.name()
        );
    }
    
    /**
     * Вернуть тестовый экземпляр сообщения о настройках КС
     *
     * @return тестовый экземпляр DeadlineSettingsMessage
     */
    public DeadlineSettingsMessage getTestDeadlineSettingsMessage(UUID organizationId) {
        return new DeadlineSettingsMessage(
                UUID.randomUUID(),
                organizationId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                createTestLimitDeadlineSettingsItem(LimitType.EMPLOYEE),
                createTestLimitDeadlineSettingsItem(LimitType.DEPARTMENT),
                null,
                false
        );
    }
    
    public Organization getTestOrganization(UUID organizationId, Long digitId) {
        return Organization.builder()
                           .id(organizationId)
                           .digitId(digitId)
                           .build();
    }
    
    
    public Department getTestDepartment(UUID organizationId, UUID departmentId, String name, String humanReadableId) {
        return Department.builder()
                         .id(departmentId)
                         .organizationId(organizationId)
                         .departmentName(name)
                         .active(true)
                         .humanReadableId(humanReadableId)
                         .build();
    }
    
    public Employee getTestEmployee(UUID id, UUID departmentId, UUID organizationId) {
        return Employee.builder()
                       .id(id)
                       .departmentId(departmentId)
                       .organizationId(organizationId)
                       .build();
    }
    
    public LimitRequest getTestLimitRequest(
            Integer year, Period period, Long sum, TransportTypeEnum transportType,
            String description, Employee author, String humanReadableId,
            LimitType limitType, LimitRequestStatus status
                                           ) {
        return LimitRequest.builder()
                           .year(year)
                           .periodData(PeriodData.valueOf(period.name()))
                           .sum(BigDecimal.valueOf(sum))
                           .transportType(transportType)
                           .description(description)
                           .author(author)
                           .humanReadableId(humanReadableId)
                           .limitType(limitType)
                           .status(status)
                           .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())))
                           .build();
    }
    
    /**
     * Получить сообщение об удалении настроек КС с заданным ID
     *
     * @param settingsId ID настроек КС
     */
    public DeadlineSettingsMessage getDeletedMessage(UUID settingsId) {
        return new DeadlineSettingsMessage(
                settingsId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true
        );
    }
}
