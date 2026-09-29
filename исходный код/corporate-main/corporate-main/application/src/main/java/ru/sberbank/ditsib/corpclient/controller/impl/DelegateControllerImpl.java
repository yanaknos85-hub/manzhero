package ru.sberbank.ditsib.corpclient.controller.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sberbank.ditsib.corpclient.controller.DelegateController;
import ru.sberbank.ditsib.corpclient.dto.DelegateRecordDTO;
import ru.sberbank.ditsib.corpclient.dto.EmployeeDTO;
import ru.sberbank.ditsib.corpclient.dto.EmployeeParameters;
import ru.sberbank.ditsib.corpclient.dto.GetDelegateRecordDTO;
import ru.sberbank.ditsib.corpclient.service.DelegateService;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@RestController
public class DelegateControllerImpl extends BaseControllerImpl implements DelegateController {
    private final DelegateService delegateService;

    public DelegateControllerImpl(DelegateService delegateService, EmployeeService employeeService) {
        super(employeeService);
        this.delegateService = delegateService;
    }

    @CheckOrganizationAccess
    @Override
    public GetDelegateRecordDTO add(
            @Organization UUID organizationId,
            UUID departmentId,
            @Valid DelegateRecordDTO delegateRecordDTO
                                   ) {
        return delegateService.add(organizationId, departmentId, delegateRecordDTO);
    }
    
    @CheckOrganizationAccess
    @Override
    public void edit(
            @Organization UUID organizationId,
            UUID departmentId,
            UUID recordId,
            @Valid DelegateRecordDTO delegateRecordDTO
                    ) {
        delegateService.edit(organizationId, departmentId, recordId, delegateRecordDTO);
    }
    
    @CheckOrganizationAccess
    @Override
    public void delete(
            @Organization UUID organizationId,
            UUID departmentId,
            UUID recordId
                      ) {
        delegateService.delete(organizationId, departmentId, recordId);
    }
    
    @CheckOrganizationAccess
    @Override
    public GetDelegateRecordDTO get(
            @Organization UUID organizationId,
            UUID departmentId,
            UUID recordId
                                   ) {
        return delegateService.get(organizationId, departmentId, recordId);
    }

    @CheckOrganizationAccess
    @Override
    public Page<GetDelegateRecordDTO> getAll(
            @Organization UUID organizationId,
            UUID departmentId,
            UUID supervisorId,
            Optional<String> date,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        if (date.isEmpty() || date.get().isBlank()) {
            return delegateService.getAllBySupervisor(organizationId, departmentId, supervisorId, pageable);
        }
        var dateString = date.get();
        LocalDate dateToSearch;
        if ("current".equalsIgnoreCase(dateString)) {
            dateToSearch = LocalDate.now();
        } else {
            dateToSearch = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
        }
        return delegateService.getAllBySupervisorByDate(organizationId, departmentId, supervisorId, dateToSearch, pageable);
    }
    
    @CheckOrganizationAccess
    @Override
    public Iterable<EmployeeDTO> getAllCandidates(
            @Organization UUID organizationId,
            UUID departmentId,
            UUID supervisorId,
            TransportTypeEnum transportType,
            String date, EmployeeParameters parameters
                                                 ) {
        return delegateService.getAllCandidates(organizationId, departmentId, supervisorId, transportType,
                                                LocalDate.parse(date, DateTimeFormatter.ISO_DATE), parameters);
    }
    
}
