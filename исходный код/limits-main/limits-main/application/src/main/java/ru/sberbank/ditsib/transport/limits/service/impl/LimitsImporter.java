package ru.sberbank.ditsib.transport.limits.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ru.sber.transport.file_works.importer.DataImporter;
import ru.sber.transport.limits.web.http.resolvers.ImportDataPreparer;
import ru.sberbank.ditsib.transport.limits.dto.file.LimitDataFileDto;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
@Scope(WebApplicationContext.SCOPE_REQUEST)
class LimitsImporter implements DataImporter<LimitDataFileDto> {

    private static final String ORGANIZATION_ID_PARAMETER = "organizationId";

    private final EmployeeService employeeService;

    private final ImportDataPreparer importDataPreparer;

    private static final Map<UUID, Employee> employees = new ConcurrentHashMap<>();

    @Override
    public void preflight(@NotNull Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        var author = getEnteredEmployee(authentication);
        var organizationId = getOrganizationId(parameters, author);
        try {
            importDataPreparer.prepare(organizationId, author.getId());
            employees.put(author.getUserId(), author);
        } catch (Exception e) {
            importDataPreparer.clear(organizationId);
            log.error("Preflight failed", e);
            throw e;
        }
    }

    @Override
    public void importData(LimitDataFileDto source, @NotNull Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        var author = employees.get(UUID.fromString(authentication.getToken().getId()));
        var organizationId = getOrganizationId(parameters, author);
        try {
            importDataPreparer.add(organizationId, source);
        } catch (Exception e) {
            log.error("Import failed", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public Object finish(@NotNull Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        var author = getEnteredEmployee(authentication);
        var organizationId = getOrganizationId(parameters, author);
        try {
            importDataPreparer.persist(organizationId);
            return null;
        } catch (Exception e) {
            log.error("Finishing failed", e);
            throw e;
        } finally {
            importDataPreparer.clear(organizationId);
        }
    }

    @Override
    public void error(@NotNull Map<String, ?> parameters, JwtAuthenticationToken authentication) {
        var author = employeeService.getByUserId(UUID.fromString(Objects.requireNonNull(authentication).getToken().getId())).orElseThrow();
        var organizationId = getOrganizationId(parameters, author);

        importDataPreparer.clear(organizationId);
    }

    private Employee getEnteredEmployee(JwtAuthenticationToken authentication) {
        return employeeService.getByUserId(UUID.fromString(authentication.getToken().getId()))
                .orElseThrow(() -> new IllegalStateException("Информация о вошедшем сотруднике не найдена"));
    }

    private @NotNull UUID getOrganizationId(Map<String, ?> parameters, Employee author) {
        if (parameters.containsKey(ORGANIZATION_ID_PARAMETER)) {
            return UUID.fromString(String.valueOf(parameters.get(ORGANIZATION_ID_PARAMETER)));
        }
        return author.getOrganizationId();
    }

}
