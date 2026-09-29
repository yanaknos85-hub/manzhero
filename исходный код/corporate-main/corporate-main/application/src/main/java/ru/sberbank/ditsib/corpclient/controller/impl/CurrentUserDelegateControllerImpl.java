package ru.sberbank.ditsib.corpclient.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.corpclient.controller.CurrentUserDelegateController;
import ru.sberbank.ditsib.corpclient.dto.EmployeeDTO;
import ru.sberbank.ditsib.corpclient.dto.EmployeeParameters;
import ru.sberbank.ditsib.corpclient.dto.GetDelegateRecordDTO;
import ru.sberbank.ditsib.corpclient.service.DelegateService;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@RestController
public class CurrentUserDelegateControllerImpl extends BaseControllerImpl implements CurrentUserDelegateController {

    private final DelegateService delegateService;

    private final EmployeeService employeeService;

    public CurrentUserDelegateControllerImpl(DelegateService delegateService, EmployeeService employeeService) {
        super(employeeService);
        this.delegateService = delegateService;
        this.employeeService = employeeService;
    }

    @Override
    public Collection<GetDelegateRecordDTO> getAllDelegatesForLoggedUser(
            JwtAuthenticationToken authentication,
            Optional<String> date
    ) {
        var activeEmployee = UUID.fromString(authentication.getToken().getId());
        if (date.isEmpty() || date.get().isBlank()) {
            return delegateService.getAllBySupervisor(activeEmployee);
        }
        var dateString = date.get();
        return delegateService.getAllBySupervisorByDate(activeEmployee, getDate(dateString));
    }

    @Override
    public Iterable<EmployeeDTO> getAllCandidatesForCurrentUser(
            JwtAuthenticationToken authentication,
            TransportTypeEnum transportType,
            String date, EmployeeParameters parameters
    ) {
        var userId = UUID.fromString(authentication.getToken().getId());
        var activeEmployee = employeeService.getEmployeeByUserId(userId);
        return delegateService.getAllCandidatesForUserAndTransportType(activeEmployee.getId(), transportType,
                LocalDate.parse(date, DateTimeFormatter.ISO_DATE), parameters);
    }


    @Override
    public Collection<GetDelegateRecordDTO> getDelegateRecordsByEmpoyee(
            JwtAuthenticationToken authentication, String date
    ) {
        var activeEmployee = UUID.fromString(authentication.getToken().getId());

        return delegateService.getAllByDelegate(activeEmployee, getDate(date));

    }

    protected LocalDate getDate(String dateString) {
        LocalDate dateToSearch;
        if ("current".equalsIgnoreCase(dateString)) {
            dateToSearch = LocalDate.now();
        } else {
            dateToSearch = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
        }
        return dateToSearch;
    }

}
