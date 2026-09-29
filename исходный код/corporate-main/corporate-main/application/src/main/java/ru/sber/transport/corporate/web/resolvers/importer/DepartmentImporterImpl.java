package ru.sber.transport.corporate.web.resolvers.importer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.corporate.web.resolvers.model.FileDepartment;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.file_works.importer.DataImporter;
import ru.sberbank.ditsib.corpclient.database.dao.GeoZoneRepository;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.database.model.messages.GeoZone;
import ru.sberbank.ditsib.corpclient.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.corpclient.messaging.sender.DepartmentSender;
import ru.sberbank.ditsib.corpclient.service.DepartmentService;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;
import ru.sber.transport.humanreadableid.service.SQGenerator;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Сервис для распознавания и записи информации о подразделении.
 */
@Component
@Transactional
@RequiredArgsConstructor
class DepartmentImporterImpl implements DataImporter<FileDepartment> {

    private final OrganizationService organizationService;

    private final DepartmentService departmentService;

    private final DepartmentSender sender;

    private final EmployeeService employeeService;

    private final GeoZoneRepository geoZoneRepository;

    private final SQGenerator generator;

    private final ExecutorService executorService = Executors.newFixedThreadPool(100);

    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    @Override
    public void importData(FileDepartment item, @NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
        var organization = organizationService.get(item.getOrganization())
                .orElseThrow(() -> new EntityNotFoundException(Organization.class,
                        item.getOrganization()));

        var department = departmentService.getDepartment(organization.getId(), item.getCode()).orElseGet(Department::new);
        if (departmentService.existsName(organization.getId(), department.getName(), department.getId())) {
            throw new DuplicateDataException(Department.class,
                    Map.of("name", item.getName(), "organization", organization.getId()));
        }
        if (departmentService.existsCode(organization.getId(), department.getCode(), department.getId())) {
            throw new DuplicateDataException(Department.class,
                    Map.of("code", item.getCode(), "organization", organization.getId()));
        }

        CompletableFuture<Department> parent = null;
        CompletableFuture<Employee> chief = null;
        if (item.getParent() != null && !item.getParent().isEmpty()) {
            parent = CompletableFuture.supplyAsync(() -> departmentService.getDepartment(organization.getId(), item.getParent())
                    .orElseThrow(() -> new EntityNotFoundException(Department.class, item.getParent())), executorService);
        }
        if (item.getChiefPersonalNumber() != null && !item.getChiefPersonalNumber().isEmpty()) {
            chief = CompletableFuture.supplyAsync(() -> employeeService.getEmployeeByPersonalNumberAndOrganizationId(item.getChiefPersonalNumber(), organization.getId())
                    .orElseThrow(() -> new EntityNotFoundException(Employee.class,
                            Map.of("personnelNumber", item.getChiefPersonalNumber()))), executorService);
        }

        department.setName(item.getName());
        department.setLevelCode(item.getLevelCode());
        department.setOrganization(organization);
        if (department.getId() == null) {
            department.setHumanReadableId(generator.getNextId(Prefix.DT, organization.getDigitId()));
        }
        department.setCode(item.getCode());
        department.setLevelName(item.getLevel());
        department.setLocation(item.getLocation());

        if (item.getLocation() != null) {
            var geozone = geoZoneRepository.findByName(item.getLocation()).orElseThrow(
                    () -> new EntityNotFoundException(GeoZone.class, item.getLocation()));
            department.setGeozone(geozone.getId());
        }

        if (parent != null) {
            if (parent.isCompletedExceptionally()) {
                throw new RuntimeException(parent.exceptionNow());
            }
            department.setParent(parent.get());
        }
        if (chief != null) {
            if (chief.isCompletedExceptionally()) {
                throw new RuntimeException(chief.exceptionNow());
            }
            department.setHead(chief.get());
        }

        departmentService.save(department);
    }
}
