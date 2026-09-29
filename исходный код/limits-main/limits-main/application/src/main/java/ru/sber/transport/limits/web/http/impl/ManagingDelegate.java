package ru.sber.transport.limits.web.http.impl;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.limits.business.Limits;
import ru.sber.transport.limits.business.model.Limit;
import ru.sber.transport.limits.web.api.ManagingApiDelegate;
import ru.sber.transport.limits.web.http.mappers.*;
import ru.sber.transport.limits.web.http.model.LimitWebFilter;
import ru.sber.transport.limits.web.model.*;
import ru.sber.transport.limits.web.providers.DepartmentsProvider;
import ru.sber.transport.limits.web.providers.EmployeeProvider;
import ru.sber.transport.limits.web.providers.SharingsProvider;
import ru.sberbank.ditsib.request.Direction;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
class ManagingDelegate implements ManagingApiDelegate {

    private static final String DATA_MASTER = "data_master";

    private final LimitWebMapper mapper;

    private final Limits limits;

    private final SpelExpressionParser parser;

    private final SortMapper sortMapper;

    private final PageMapper pageMapper;

    private final EmployeeProvider employees;

    private final DepartmentsProvider departments;

    private final SharingsProvider sharings;

    private final EmployeeWebMapper employeeMapper;

    private final DepartmentWebMapper departmentMapper;

    private final SharingsWebMapper sharingsMapper;

    @Override
    public ResponseEntity<Void> delete(UUID limitId) {
        log.trace("Attempting to delete limit with id: {}", limitId);
        final var auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        final var token = auth.getToken();
        final var userId = UUID.fromString(token.getId());
        final var forceAllow = Optional.ofNullable(token.getClaimAsBoolean(DATA_MASTER)).orElse(false);
        limits.delete(limitId, userId, forceAllow);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Override
    public ResponseEntity<LimitData> get(UUID limitId, Optional<OffsetDateTime> ifModifiedSince) {
        final var auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        final var token = auth.getToken();
        final var userId = UUID.fromString(token.getId());
        final boolean forceAllow = Optional.ofNullable(token.getClaimAsBoolean(DATA_MASTER)).orElse(false);
        final Limit limit;
        final boolean modified;
        if (ifModifiedSince.isPresent()) {
            final var limitWithStatus = limits.get(userId, forceAllow, limitId, ifModifiedSince.get());
            limit = limitWithStatus.data();
            modified = limitWithStatus.modified();
        } else {
            limit = limits.get(userId, forceAllow, limitId);
            modified = true;
        }
        return ResponseEntity.status(modified ? HttpStatus.OK : HttpStatus.NOT_MODIFIED)
                .lastModified(limit.getUpdateTime().toInstant())
                .eTag(limit.getHash())
                .body(modified ? mapper.toWeb(limit) : null);
    }

    @Override
    public ResponseEntity<Void> head(UUID limitId, Optional<OffsetDateTime> ifModifiedSince) {
        final var auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        final var token = auth.getToken();
        final var userId = UUID.fromString(token.getId());
        final boolean forceAllow = Optional.ofNullable(token.getClaimAsBoolean(DATA_MASTER)).orElse(false);
        final Limit data;
        final boolean modified;
        if (ifModifiedSince.isPresent()) {
            var limit = limits.hash(userId, forceAllow, limitId, ifModifiedSince.get());
            data = limit.data();
            modified = limit.modified();
        } else {
            data = limits.hash(userId, forceAllow, limitId);
            modified = true;
        }
        return ResponseEntity.status(modified ? HttpStatus.NO_CONTENT : HttpStatus.NOT_MODIFIED)
                .lastModified(data.getUpdateTime().toInstant())
                .eTag(data.getHash())
                .build();
    }

    @Override
    public ResponseEntity<Void> put(NewLimit newLimit, UUID limitId) {
        final var auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        final var token = auth.getToken();
        final var userId = UUID.fromString(token.getId());
        final boolean forceAllow = Optional.ofNullable(token.getClaimAsBoolean(DATA_MASTER)).orElse(false);
        final var limit = limits.update(limitId, userId, forceAllow, mapper.toBusiness(newLimit), Arrays.stream(newLimit.getClass().getDeclaredFields()).map(Field::getName).map(it -> "REPLACE:" + it).toList());
        return ResponseEntity.status(HttpStatus.ACCEPTED).eTag(limit.getHash()).lastModified(limit.getUpdateTime().toInstant()).build();
    }

    @Override
    public ResponseEntity<Void> patch(List<PatchRequestInner> patchRequestInner, UUID limitId) {
        final var auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        final var token = auth.getToken();
        final var userId = UUID.fromString(token.getId());
        final boolean forceAllow = Optional.ofNullable(token.getClaimAsBoolean(DATA_MASTER)).orElse(false);
        final var updated = limits.update(limitId, userId, forceAllow, createEdited(patchRequestInner), patchRequestInner.parallelStream().map(it -> it.getOp() + ":" + it.getPath()).map(it -> it.replace("/", ".").replaceFirst("\\.", "")).toList());
        return ResponseEntity.status(HttpStatus.ACCEPTED).eTag(updated.getHash()).lastModified(updated.getUpdateTime().toInstant()).build();
    }

    @Override
    public ResponseEntity<LimitsPage> getAll(Optional<UUID> organizationId, Optional<UUID> departmentId, Optional<UUID> employeeId, Optional<String> parentDepartment, Optional<String> limitOwner, Optional<UUID> limitId, Optional<UUID> parentLimitId, Optional<UUID> parentId, Optional<String> humanReadableLimitId, Optional<String> humanReadableId, Optional<UUID> requestId, Optional<@Min(1970) Integer> year, Optional<Status> status, Optional<Status> limitStatus, Optional<String> serviceType, Optional<String> limitServiceType, Optional<Type> limitType, Optional<Type> type, Optional<@Min(0) Integer> page, Optional<@Min(1) Integer> size, Optional<String> sort, Optional<SortDirection> direction) {
        var filter = LimitWebFilter.builder()
                .organizationId(organizationId.orElse(null))
                .departmentId(departmentId.orElse(null))
                .employeeId(employeeId.orElse(null))
                .parentDepartment(parentDepartment.orElse(null))
                .limitOwner(limitOwner.orElse(null))
                .limitId(limitId.orElse(null))
                .parentId(parentLimitId.or(() -> parentLimitId).orElse(null))
                .humanReadableId(humanReadableId.or(() -> humanReadableLimitId).orElse(null))
                .requestId(requestId.orElse(null))
                .year(year.orElse(null))
                .status(status.or(() -> limitStatus).map(Enum::name).map(ru.sber.transport.limits.business.model.Status::valueOf).orElse(null))
                .serviceType(serviceType.or(() -> limitServiceType).orElse(null))
                .type(type.or(() -> limitType).map(Enum::name).map(ru.sber.transport.limits.business.model.Type::valueOf).orElse(null))
                .build();
        final var result = limits.get(filter, page.orElse(0), size.orElse(20), sort.orElse("human_readable_id"), Direction.valueOf(direction.orElse(SortDirection.ASC).name())).map(mapper::toWeb);
        final var resultMap = result.getContent().parallelStream().collect(Collectors.toMap(LimitData::getId, Function.identity()));
        final var futures = new ArrayList<CompletableFuture<Void>>(4);
        try (final var executor = Executors.newFixedThreadPool(4)) {

            futures.add(CompletableFuture.supplyAsync(() -> employees.getOwnerOfLimits(resultMap.values().parallelStream().map(LimitData::getId).toList()), executor)
                    .thenAccept(it -> it.entrySet().parallelStream().forEach(e -> resultMap.get(e.getKey()).setOwner(employeeMapper.toWeb(e.getValue())))));
            futures.add(CompletableFuture.supplyAsync(() -> employees.getEmployeeOfLimits(resultMap.values().parallelStream().map(LimitData::getId).toList()), executor)
                    .thenAccept(it -> it.entrySet().parallelStream().forEach(e -> resultMap.get(e.getKey()).setEmployee(employeeMapper.toWeb(e.getValue())))));
            futures.add(CompletableFuture.supplyAsync(() -> departments.getOfLimits(resultMap.values().parallelStream().map(LimitData::getId).toList()), executor)
                    .thenAccept(it -> it.entrySet().parallelStream().forEach(e -> resultMap.get(e.getKey()).setDepartment(departmentMapper.toWeb(e.getValue())))));
            futures.add(CompletableFuture.supplyAsync(() -> sharings.get(resultMap.values().parallelStream().map(LimitData::getId).toList()), executor)
                    .thenAccept(it -> it.entrySet().parallelStream().forEach(e -> resultMap.get(e.getKey()).setLimitSharingDTOList(e.getValue().stream().map(sharingsMapper::toWeb).collect(Sharings::new, Sharings::add, Sharings::addAll)))));
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        result.getContent().parallelStream()
                .filter(it -> it.getLimitSharingDTOList() == null)
                .forEach(it -> it.setLimitSharingDTOList(new Sharings()));
        result.getContent().parallelStream()
                .map(LimitData::getLimitSharingDTOList)
                .flatMap(Collection::parallelStream)
                .filter(it -> it.getLimitSharingPerPeriodDTO() == null)
                .forEach(it -> it.setLimitSharingPerPeriodDTO(List.of()));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new LimitsPage(result.getContent(), sortMapper.toWeb(result.getSortData()), pageMapper.toWeb(result.getPageData())));
    }

    private Limit createEdited(List<PatchRequestInner> list) {
        final var result = new NewLimit();
        for (final var item : list) {
            final var path = item.getPath()
                    .replaceFirst("/", "")
                    .replace("/", ".");
            final var op = item.getOp();
            final var value = PatchRequestInner.OpEnum.REMOVE.equals(op) ? null : item.getValue();
            parser.parseRaw(path).setValue(result, value);
        }
        Optional.ofNullable(result.getResponsibles()).ifPresent(it -> it.remove(null));
        return mapper.toBusiness(result);
    }
}
