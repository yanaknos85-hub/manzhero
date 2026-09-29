package ru.sber.transport.corporate.web.resolvers.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.corporate.web.resolvers.model.FileDepartment;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.dto.FileExportFilterDTO;
import ru.sberbank.ditsib.corpclient.service.DepartmentService;
import ru.sberbank.ditsib.corpclient.service.impl.file_resolvers.FilterDataResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сервис для распознавания и записи информации о подразделении.
 */
@Component
@Transactional
class DepartmentExporterImpl extends FilterDataResolver<FileDepartment> {

    private final DepartmentService departmentService;

    /**
     * Конструктор сервиса.
     *
     * @param departmentService   сервис департаментов.
     * @param objectMapper        маппер объектов.
     */
    public DepartmentExporterImpl(DepartmentService departmentService,
                                  ObjectMapper objectMapper) {
        super(objectMapper);
        this.departmentService = departmentService;
    }

    @Override
    protected List<FileDepartment> exportFilteredData(FileExportFilterDTO filter, Map<String, ?> parameters,
                                                         JwtAuthenticationToken authentication) {
        var result = new ArrayList<FileDepartment>();
        for (var department : departmentService.findAllByOrganizationId(filter.getOrganizationId())) {
            var resItem = new FileDepartment();

            resItem.setOrganization(department.getOrganization().getOfficialName());
            resItem.setName(department.getName());
            resItem.setLevel(department.getLevelName());
            resItem.setLevelCode(department.getLevelCode());
            resItem.setLocation(department.getLocation());
            resItem.setCode(department.getCode());
            resItem.setParent(Optional.ofNullable(department.getParent()).map(Department::getCode).orElse(null));
            resItem.setChiefPersonalNumber(Optional.ofNullable(department.getHead()).map(Employee::getPersonnelNumber).orElse(null));

            result.add(resItem);
        }
        return result;
    }
}
