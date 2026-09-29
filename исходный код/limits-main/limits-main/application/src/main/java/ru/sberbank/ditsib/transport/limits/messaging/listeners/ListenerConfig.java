package ru.sberbank.ditsib.transport.limits.messaging.listeners;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import ru.sber.transport.deadline.messaging.DeadlineSettingsMessage;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.limits.dto.LimitActionMessage;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.mapper.DeadlineSettingsMapper;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.deadline.DeadlineSettings;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.service.*;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Configuration
class ListenerConfig {

    @Bean
    Consumer<Message<DeadlineSettingsMessage>> deadlineSettingsInput(DeadlineSettingsService settingsService, DeadlineSettingsMapper mapper) {
        return deadlineSettingsInputSsl(settingsService, mapper);
    }

    @Bean
    Consumer<Message<LimitActionMessage>> limitSpendInput(LimitActionService limitActionService) {
        return limitSpendInputSsl(limitActionService);
    }

    @Bean
    Consumer<Message<DeadlineSettingsMessage>> deadlineSettingsInputSsl(DeadlineSettingsService settingsService, DeadlineSettingsMapper mapper) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            if (message.deleted()) {
                Optional<DeadlineSettings> optional = settingsService.getOptional(message.getId());
                optional.ifPresentOrElse(settings -> {
                    settingsService.delete(settings);
                    log.info("Настройки КС ID '{}' были удалены", message.getId());
                }, () -> {
                    log.info("При попытке удаления Настроек КС ID '{}', они не были найдены в БД", message.getId());
                    throw new EntityNotFoundException(DeadlineSettings.class, message.getId());
                });
            } else {
                DeadlineSettings settings = mapper.fromMessage(message);
                settingsService.save(settings);
                log.info("Настройки КС ID '{}' были записаны / отредактированы", message.getId());
            }
        };
    }

    @Bean
    Consumer<Message<OrganizationMessage>> organizationsInput(OrganizationService organizationService) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var headers = rawMessage.getHeaders();
            var organizationId = (UUID) headers.getOrDefault(KafkaHeaders.RECEIVED_KEY, message.getId());
            if (message.isDeleted()) {
                organizationService.get(organizationId).ifPresent(organizationService::delete);
            } else {
                organizationService.save(Organization.builder().id(organizationId).digitId(message.getDigitId())
                        .active(true).build());
            }
        };
    }

    @Bean
    Consumer<Message<DepartmentMessage>> departmentsInput(DepLimitService depLimitService, LimitService limitService,
                                                              DepartmentService departmentService, EmployeeService employeeService) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var depLimitList = depLimitService.getByDepartment(message.getId());
            if (message.isDeleted()) {
                deleteDepartment(depLimitService, limitService, departmentService, depLimitList, message);
            } else {
                saveDepartment(depLimitService, departmentService, employeeService, message, depLimitList);
            }
        };
    }

    @Bean
    Consumer<Message<EmployeeMessage>> employeesInput(EmployeeService employeeService, EmpLimitService empLimitService) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var id = (UUID) rawMessage.getHeaders().getOrDefault(KafkaHeaders.RECEIVED_KEY, message.getId());
            if (message.isDeleted()) {
                employeeService.get(id).ifPresent(employeeService::delete);
                deleteEmpLimits(empLimitService, id);
            } else {
                if (message.getDepartmentId() == null) {
                    deleteEmpLimits(empLimitService, id);
                }
                employeeService.save(Employee.builder()
                        .id(id)
                        .userId(message.getUserId())
                        .organizationId((message.getOrganizationId()))
                        .departmentId(message.getDepartmentId())
                        .supervisorId(message.getSupervisorId())
                        .firstName(message.getFirstName())
                        .lastName(message.getLastName())
                        .patronymic(message.getPatronymic())
                        .personnelNumber(message.getPersonnelNumber())
                        .positionId((message.getPositionId()))
                        .email(message.getEmail())
                        .active(true)
                        .humanReadableId(message.getHumanReadableId()).build());
            }
        };
    }

    @Bean
    Consumer<Message<LimitActionMessage>> limitSpendInputSsl(LimitActionService limitActionService) {
        return rawMessage -> {
            var payload = rawMessage.getPayload();
            limitActionService.processLimitAction(payload);
        };
    }

    private void saveDepartment(DepLimitService depLimitService, DepartmentService departmentService, EmployeeService employeeService, DepartmentMessage message, List<DepLimit> depLimitList) {
        departmentService.save(Department.builder()
                .id(message.getId())
                .parentId(message.getParentId())
                .code(message.getCode())
                .departmentName(message.getDepartmentName())
                .departmentHead(employeeService.get(message.getDepartmentHeadId()).orElse(null))
                .organizationId(message.getOrganizationId())
                .humanReadableId(message.getHumanReadableId())
                .active(true)
                .build());

        var departmentHead = message.getDepartmentHeadId();
        if (departmentHead != null) {
            employeeService.get(departmentHead).ifPresent(newDepHead -> changeDepLimitVLP(depLimitService, depLimitList, newDepHead));
        }
    }

    private void deleteDepartment(DepLimitService depLimitService, LimitService limitService, DepartmentService departmentService, List<DepLimit> depLimitList, DepartmentMessage message) {
        if(depLimitService.checkHaveNotClosedChildLimits(depLimitList.stream()
                .map(depLimit -> depLimit.getDepartment().getId())
                .toList())) {
            throw new LimitLogicException("ERROR: Попытка удаления подразделения с действующими дочерними лимитами!");
        }
        closeDepLimits(depLimitList, limitService);
        departmentService.get(message.getId()).ifPresent(departmentService::delete);
    }

    /**
     * Удаление лимитов подразделения.
     *
     * @param depLimitList список лимитов.
     */
    private void closeDepLimits(List<DepLimit> depLimitList, LimitService limitService) {
        var now = LocalDateTime.now(ZoneOffset.UTC);
        var authorId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        depLimitList.stream()
                .filter(depLimit -> depLimit.getYear() >= now.getYear())
                .forEach(depLimit -> limitService.closeLimit(depLimit, authorId));
    }

    /**
     * Смена ВЛП для лимитов подразделения.
     *
     * @param depLimitList список лимитов.
     * @param newDepHead   новый ВЛП
     * @deprecated Удалить вместе с полем владельца лимита
     */
    @Deprecated(since = "20230724-1")
    private void changeDepLimitVLP(DepLimitService depLimitService, List<DepLimit> depLimitList, Employee newDepHead) {
        var now = LocalDateTime.now(ZoneOffset.UTC);

        depLimitList.stream()
                .filter(depLimit -> depLimit.getYear() >= now.getYear()
                        && !newDepHead.getId().equals(depLimit.getLimitOwner() != null ? depLimit.getLimitOwner().getId() : null))
                .forEach(depLimit -> {
                    depLimit.setLimitOwner(newDepHead);
                    depLimitService.save(depLimit);
                });
    }

    /**
     * Удаление лимитов сотрудников.
     *
     * @param employeeId айди сотрудника.
     */
    private void deleteEmpLimits(EmpLimitService empLimitService, UUID employeeId) {
        var currentYear = Calendar.getInstance().get(Calendar.YEAR);
        var authorId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        var empLimitList = empLimitService.getByEmployee(employeeId);

        for (var empLimit : empLimitList) {
            if (empLimit.getYear() >= currentYear) {
                empLimitService.closeEmpLimit(empLimit, authorId);
            }
        }
    }

}
