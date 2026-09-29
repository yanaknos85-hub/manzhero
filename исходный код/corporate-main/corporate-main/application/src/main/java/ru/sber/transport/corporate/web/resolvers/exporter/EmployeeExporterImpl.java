package ru.sber.transport.corporate.web.resolvers.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.corporate.web.resolvers.model.FileEmployee;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.dto.FileExportFilterDTO;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;
import ru.sberbank.ditsib.corpclient.service.impl.file_resolvers.FilterDataResolver;

import java.util.*;

/**
 * Сервис для распознавания и записи информации о сотруднике.
 */
@Component
@Transactional
class EmployeeExporterImpl extends FilterDataResolver<FileEmployee> {

    private static final String LAST_FIRST_NAME_FORMAT = "%s %s";

    private final EmployeeService employeeService;

    /**
     * Конструктор сервиса.
     *
     * @param employeeService     сервис сотрудников.
     * @param objectMapper        маппер объектов.
     */
    public EmployeeExporterImpl(EmployeeService employeeService,
                                ObjectMapper objectMapper) {
        super(objectMapper);
        this.employeeService = employeeService;
    }

    @Override
    protected List<FileEmployee> exportFilteredData(FileExportFilterDTO filter, Map<String, ?> parameters,
                                                          JwtAuthenticationToken authentication) {
        var result = new ArrayList<FileEmployee>();
        for (var item : employeeService.findAllByOrganizationId(filter.getOrganizationId())) {
            var resItem = new FileEmployee();

            var department = item.getDepartment();

            var fullName = new StringBuilder(String.format(LAST_FIRST_NAME_FORMAT, item.getLastName(), item.getFirstName()));
            if (item.getPatronymic() != null) {
                fullName.append(" ").append(item.getPatronymic());
            }

            resItem.setEmail(item.getEmail());
            resItem.setOrganization(department.getOrganization().getOfficialName());
            resItem.setFullName(fullName.toString());
            resItem.setPhone(item.getMobilePhone());
            resItem.setDepartment(department.getName());
            resItem.setPersonalNumber(item.getPersonnelNumber());
            resItem.setPosition(item.getPosition().getName());

            var supervisor = item.getSupervisor();
            if (supervisor != null) {
                resItem.setSupervisorPersonalNumber(supervisor.getPersonnelNumber());
            }

            result.add(resItem);
        }
        return result;
    }

}
