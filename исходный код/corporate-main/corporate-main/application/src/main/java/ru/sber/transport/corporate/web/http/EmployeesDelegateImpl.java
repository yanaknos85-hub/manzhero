package ru.sber.transport.corporate.web.http;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.Employees;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.corporate.web.http.mappers.EmployeesWebMapper;
import ru.sber.transport.corporate.web.http.mappers.PageMapper;
import ru.sber.transport.corporate.web.http.mappers.SortMapper;
import ru.sber.transport.corporate.web.model.EmployeeWebFilter;
import ru.sber.transport.web.api.EmployeesQueryApi;
import ru.sber.transport.web.model.*;
import ru.sberbank.ditsib.request.Direction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
class EmployeesDelegateImpl implements EmployeesQueryApi {

    private final Employees employees;

    private final EmployeesWebMapper mapper;

    private final PageMapper pageMapper;

    private final SortMapper sortMapper;

    @Override
    public ResponseEntity<EmployeePage> employeesGet(List<UUID> organizations, List<UUID> departments, List<UUID> employees, String fullName, String personnelNumber, String humanReadableId, ActiveStatus status, String mobilePhone, String email, OrgStructureType orgStructureType, Projection projection, Integer page, Integer size, String sort, String direction) {
        final var filter = EmployeeWebFilter.builder()
                .organizations(organizations)
                .departments(departments)
                .employees(employees)
                .fullName(fullName)
                .personnelNumber(personnelNumber)
                .humanReadableId(humanReadableId)
                .status(Active.valueOf(status.name()))
                .mobilePhone(mobilePhone)
                .email(email)
                .orgStructureType(orgStructureType)
                .build();
        final var employeesPage = this.employees.get(filter, Optional.ofNullable(projection).orElse(Projection.FULL), Optional.ofNullable(page).orElse(0), Optional.ofNullable(size).orElse(20), Optional.ofNullable(sort).orElse("last_name"), Optional.ofNullable(direction).map(Direction::valueOf).orElse(Direction.ASC));

        final var result = new EmployeePage();
        result.setContent(employeesPage.map(mapper::toWeb).getContent());
        result.setPage(pageMapper.toWeb(employeesPage.getPageData()));
        result.setSort(sortMapper.toWeb(employeesPage.getSortData()));
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<EmployeeData> get() {
        var authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var employeeId = authentication.getToken().getId();
        return ResponseEntity.ok(mapper.toWeb(employees.get(UUID.fromString(employeeId))));
    }
}
