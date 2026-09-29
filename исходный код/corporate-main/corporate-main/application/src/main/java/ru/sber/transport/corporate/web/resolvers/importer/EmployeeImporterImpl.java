package ru.sber.transport.corporate.web.resolvers.importer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.corporate.web.resolvers.model.FileEmployee;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.file_works.importer.DataImporter;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.database.model.messages.Role;
import ru.sberbank.ditsib.corpclient.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.corpclient.service.*;
import ru.sber.transport.humanreadableid.service.SQGenerator;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Сервис для распознавания и записи информации о сотруднике.
 */
@Component
@Transactional
@RequiredArgsConstructor
class EmployeeImporterImpl implements DataImporter<FileEmployee> {

    private static final String LAST_FIRST_NAME_FORMAT = "%s %s";

    private final EmployeeService employeeService;

    private final DepartmentService departmentService;

    private final PositionService positionService;

    private final OrganizationService organizationService;

    private final RolesService rolesService;

    private final SQGenerator generator;

    private final ExecutorService executors = Executors.newFixedThreadPool(100);

    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public void importData(FileEmployee item, @NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken token) {
        if (item.getRole() == null) {
            throw new RuntimeException("Роль не указана");
        }
        if (item.getEmail() == null) {
            throw new RuntimeException("E-Mail не указан");
        }
        var organization = organizationService.get(item.getOrganization())
                .orElseThrow(() -> new EntityNotFoundException(Organization.class,
                        item.getOrganization()));
        var employee = employeeService.getEmployeeByPersonalNumberAndOrganizationId(item.getPersonalNumber(), organization.getId())
                .orElseGet(Employee::new);

        var role = CompletableFuture.supplyAsync(() -> rolesService.getByName(item.getRole())
                .orElseThrow(() -> new EntityNotFoundException(Role.class, item.getRole())), executors);

        updateEmployee(employee, item);

        if (item.getSupervisorPersonalNumber() != null) {
            employee.setSupervisor(employeeService.getEmployeeByPersonalNumberAndOrganizationId(item.getSupervisorPersonalNumber(), organization.getId()).orElse(null));
        }
        if (role.isCompletedExceptionally()) {
            throw new RuntimeException(role.exceptionNow());
        }
        employeeService.saveEmployee(employee, Set.of(role.get().getCode()));
    }

    public void updateEmployee(Employee target, FileEmployee item) throws ExecutionException, InterruptedException {
        var organization = organizationService.get(item.getOrganization())
                .orElseThrow(() -> new EntityNotFoundException(Organization.class,
                        item.getOrganization()));
        var department = CompletableFuture.supplyAsync(() -> departmentService.getDepartmentByName(organization.getId(), item.getDepartment()).orElseThrow(() -> new EntityNotFoundException(Department.class, item.getDepartment())), executors);
        var position = CompletableFuture.supplyAsync(() -> positionService.getPosition(organization.getId(), item.getPosition()).orElseThrow(() -> new EntityNotFoundException(Position.class, item.getPosition())), executors);

        if (target.getId() == null) {
            target.setId(UUID.randomUUID());
            target.setUserId(target.getId());
        }

        var fullName = item.getFullName();
        var lastName = fullName.split("\\s")[0];
        var firstName = fullName.split("\\s")[1];
        var patronymic = fullName.substring(String.format(LAST_FIRST_NAME_FORMAT, lastName, firstName).length()).trim();

        target.setOrgStructureType(OrgStructureType.EXTERNAL);
        target.setEmail(item.getEmail());
        target.setFirstName(firstName);
        target.setLastName(lastName);
        target.setPatronymic(patronymic);
        target.setMobilePhone(item.getPhone());
        target.setPersonnelNumber(item.getPersonalNumber());
        var supervisorPersonalNumber = item.getSupervisorPersonalNumber();
        if (supervisorPersonalNumber != null) {
            var supervisor = employeeService.getEmployeeByPersonalNumberAndOrganizationId(supervisorPersonalNumber, organization.getId())
                    .orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of("personnelNumber", supervisorPersonalNumber, "organizationId", organization.getId())));
            target.setSupervisor(supervisor);
        }
        target.setHumanReadableId(generator.getNextId(Prefix.US, organization.getDigitId()));
        target.setActiveStatus(ActiveStatus.ACTIVE);
        if (department.isCompletedExceptionally()) {
            throw new RuntimeException(department.exceptionNow());
        }
        target.setDepartment(department.get());

        if (position.isCompletedExceptionally()) {
            throw new RuntimeException(position.exceptionNow());
        }
        target.setPosition(position.get());
    }

}
