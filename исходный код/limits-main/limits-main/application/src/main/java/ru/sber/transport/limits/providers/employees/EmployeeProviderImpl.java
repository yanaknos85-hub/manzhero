package ru.sber.transport.limits.providers.employees;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.limits.tables.records.EmployeeRecord;
import ru.sber.transport.limits.business.model.Employee;
import ru.sber.transport.limits.business.providers.EmployeeProvider;
import ru.sber.transport.limits.providers.employees.mappers.EmployeeDatabaseMapper;
import ru.sber.transport.limits.web.grpc.client.StateClient;

import java.util.*;
import java.util.stream.Collectors;

import static ru.sber.transport.database.limits.Tables.LIMIT;

@Slf4j
@Repository
@Transactional
@RequiredArgsConstructor
public class EmployeeProviderImpl implements EmployeeProvider, ru.sber.transport.limits.web.providers.EmployeeProvider, JooqRepository<ru.sber.transport.database.limits.tables.Employee, EmployeeRecord, UUID> {

    private final EmployeeDatabaseMapper mapper;

    private final StateClient stateClient;

    @Override
    public Optional<Employee> get(UUID userId) {
        var found = context()
                .selectFrom(table())
                .where(table().USER_ID.eq(userId))
                .or(table().ID.eq(userId))
                .fetchOptionalInto(EmployeeRecord.class);
        if (found.isEmpty()) {
            found = stateClient.get(userId).map(mapper::toDatabase)
                    .map(this::save);
        }
        return found.map(mapper::toBusiness);
    }

    @Override
    public Set<String> getEmails(List<UUID> responsibles) {
        final var emails = context().select(table().ID, table().EMAIL)
                .from(table())
                .where(table().ID.in(responsibles))
                .and(table().EMAIL.isNotNull())
                .fetchMap(table().ID, table().EMAIL);
        if (emails.size() != responsibles.size()) {
            log.info("Found {} responsibles, but {} emails. Requesting remains", responsibles.size(), emails.size());
            final var fromSource = responsibles
                    .parallelStream()
                    .filter(it -> !emails.containsKey(it))
                    .toList();
            log.info("Requesting {} employees", fromSource.size());
            emails.putAll(fromSource.parallelStream()
                    .map(it -> getEmail(it).map(email -> new AbstractMap.SimpleEntry<>(it, email)).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }

        return new HashSet<>(emails.values());
    }

    @Override
    public @NonNull Employee current() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken jwt) {
            var id = UUID.fromString(jwt.getToken().getId());
            return context().selectFrom(table())
                    .where(table().ID.eq(id))
                    .or(table().USER_ID.eq(id))
                    .fetchOptional()
                    .map(mapper::toBusiness)
                    .orElseGet(() -> getUser(id));
        }
        throw new IllegalStateException("Can't acquire current employee");
    }

    @Override
    public boolean hasAccess() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(it -> it instanceof JwtAuthenticationToken)
                .map(JwtAuthenticationToken.class::cast)
                .map(it -> it.getToken().getClaimAsBoolean("data_master"))
                .orElse(false);
    }

    private Employee getUser(UUID id) {
        return stateClient.get(id).map(mapper::toDatabase)
                .map(this::save)
                .map(mapper::toBusiness)
                .orElseThrow(() -> new IllegalStateException("Can't acquire current employee"));
    }

    @Override
    public ru.sber.transport.database.limits.tables.Employee table() {
        return ru.sber.transport.database.limits.tables.Employee.EMPLOYEE;
    }

    private Optional<String> getEmail(@NonNull UUID id) {
        return stateClient.get(id).map(it -> saveEmail(id, it));
    }

    private String saveEmail(UUID id, Employee employee) {
        final var email = employee.getEmail();
        if (email != null) {
            context().update(table())
                    .set(table().EMAIL, email)
                    .where(table().ID.eq(id))
                    .executeAsync();
        }
        return email;
    }

    @Override
    public Map<UUID, Employee> getOwnerOfLimits(List<UUID> ids) {
        return context().select()
                .from(table())
                .innerJoin(LIMIT).on(LIMIT.LIMIT_OWNER_ID.eq(table().ID))
                .where(LIMIT.ID.in(ids))
                .fetchMap(LIMIT.ID, new RecordMapper<>() {

                    @Nullable
                    @Override
                    public Employee map(Record element) {
                        return mapper.toBusiness(element.into(EmployeeRecord.class));
                    }
                });
    }

    @Override
    public Map<UUID, Employee> getEmployeeOfLimits(List<UUID> ids) {
        return context().select()
                .from(table())
                .innerJoin(LIMIT).on(LIMIT.EMPLOYEE_ID.eq(table().ID))
                .where(LIMIT.ID.in(ids))
                .fetchMap(LIMIT.ID, new RecordMapper<>() {

                    @Nullable
                    @Override
                    public Employee map(Record element) {
                        return mapper.toBusiness(element.into(EmployeeRecord.class));
                    }
                });
    }
}
