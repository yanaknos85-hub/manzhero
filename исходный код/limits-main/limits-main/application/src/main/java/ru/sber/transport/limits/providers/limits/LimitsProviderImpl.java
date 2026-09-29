package ru.sber.transport.limits.providers.limits;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.limits.enums.Status;
import ru.sber.transport.database.limits.enums.Type;
import ru.sber.transport.database.limits.tables.records.LimitRecord;
import ru.sber.transport.dto.Page;
import ru.sber.transport.limits.business.model.Limit;
import ru.sber.transport.limits.business.model.LimitFilter;
import ru.sber.transport.limits.business.model.ModifiedLimit;
import ru.sber.transport.limits.business.providers.LimitsProvider;
import ru.sber.transport.limits.providers.limits.mappers.LimitsDatabaseMapper;
import ru.sberbank.ditsib.request.Direction;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static ru.sber.transport.database.limits.Tables.*;
import static ru.sber.transport.database.limits.tables.LimitResponsibleEmployee.LIMIT_RESPONSIBLE_EMPLOYEE;

@RequiredArgsConstructor
@Repository
@Transactional
public class LimitsProviderImpl implements LimitsProvider, JooqRepository<ru.sber.transport.database.limits.tables.Limit, LimitRecord, UUID> {

    private static final String LIKE_FORMAT = "%%%s%%";

    private final LimitsDatabaseMapper mapper;

    private final SpelExpressionParser parser;

    @Override
    public Optional<Limit> upper(UUID id) {
        Optional<LimitRecord> data;
        do {
            data = context().select(table().ID, table().PARENT_ID).from(table()).where(table().ID.eq(id)).fetchOptionalInto(LimitRecord.class);
            if (data.isEmpty()) {
                break;
            }
            id = data.get().getParentId();
        } while (id != null);
        return data.flatMap(it -> findById(it.getId())).map(mapper::toBusiness);
    }

    @Override
    public Optional<Limit> get(UUID id) {
        return findById(id).map(mapper::toBusiness).map(it -> {
            it.setResponsibles(getResponsibles(it.getId()));
            return it;
        });
    }

    @Override
    public void save(Limit source) {
        var database = findById(source.getId()).orElseGet(LimitRecord::new);
        mapper.update(database, source);
        save(database);
    }

    @Override
    public List<Limit> getChildren(UUID limitId) {
        var recursiveLimit = DSL.name("recursiveLimit");
        return context().withRecursive(recursiveLimit)
                .as(context().select(table().fields()).from(table()).where(table().ID.eq(limitId))
                        .union(context().select(table().fields()).from(table()).innerJoin(recursiveLimit).on(DSL.field(recursiveLimit.append(table().ID.getName())).eq(table().PARENT_ID))))
                .selectFrom(recursiveLimit)
                .fetch()
                .map(this::createRecord)
                .stream()
                .filter(Objects::nonNull)
                .map(mapper::toBusiness)
                .toList();
    }

    @Override
    public Optional<ModifiedLimit> get(@NonNull UUID limitId, @NonNull OffsetDateTime modifiedSince) {
        Optional<ModifiedLimit> limit;
        if (context().fetchExists(context().select(table().ID).from(table()).where(table().ID.eq(limitId).and(table().UPDATE_TIME.greaterThan(modifiedSince))))) {
            limit = findById(limitId).map(it -> new ModifiedLimit(true, mapper.toBusiness(it)));
        } else {
            limit = context().select(table().UPDATE_TIME, table().HASH)
                    .from(table())
                    .where(table().ID.eq(limitId))
                    .fetchOptionalInto(LimitRecord.class)
                    .map(mapper::toBusiness)
                    .map(it -> new ModifiedLimit(false, it));
        }
        return limit;
    }

    @Override
    public Optional<Limit> hash(@NonNull UUID limitId) {
        return context().select(table().UPDATE_TIME, table().HASH)
                .from(table())
                .where(table().ID.eq(limitId))
                .fetchOptionalInto(LimitRecord.class)
                .map(mapper::toBusiness);
    }

    @Override
    public Optional<ModifiedLimit> hash(@NonNull UUID limitId, @NonNull OffsetDateTime modifiedSince) {
        final boolean modified = context().fetchExists(context().select(table().ID).from(table()).where(table().ID.eq(limitId).and(table().UPDATE_TIME.greaterThan(modifiedSince))));
        return hash(limitId).map(it -> new ModifiedLimit(modified, it));
    }

    @Override
    public @NonNull Limit update(@NonNull Limit limit, @NonNull Limit newData, @NonNull List<String> updatedFields) {
        final var now = OffsetDateTime.now();
        final var map = new HashMap<Field<?>, Object>();
        final var responsibleFields = new ArrayList<String>();
        for (final var fieldName : updatedFields) {
            final var fieldParts = fieldName.split(":");
            final var op = fieldParts[0];
            final var effectiveField = fieldParts[1];
            final var value = parser.parseRaw(effectiveField).getValue(newData);
            final var add = "ADD".equals(op);
            if (effectiveField.startsWith("responsibles")) {
                responsibleFields.add(fieldName);
            } if (effectiveField.startsWith("useThisLimit")) {
                map.put(table().USE_MY_LIMIT, value);
            } else {
                Arrays.stream(table().fields())
                        .parallel()
                        .filter(it -> it.getName().replace("_", "").toLowerCase().endsWith(effectiveField.replace("_", "").toLowerCase()))
                        .findFirst()
                        .ifPresent(field -> map.put(field, getValue(field, add, value)));
            }
        }
        if (!responsibleFields.isEmpty()) {
            updateResponsibles(limit.getId(), responsibleFields, newData.getResponsibles());
        }

        final var hash = calculateHash(limit.getId(), now);
        final var saved = context().update(table())
                .set(table().UPDATE_TIME, now)
                .set(table().HASH, hash)
                .set(map)
                .where(table().ID.eq(limit.getId()))
                .returning()
                .fetchSingle();
        final var business = mapper.toBusiness(saved);
        business.setResponsibles(getResponsibles(limit.getId()));
        return business;
    }

    @NotNull
    private static String calculateHash(UUID id, OffsetDateTime now) {
        return String.valueOf(UUID.nameUUIDFromBytes("%s%s%s".formatted(Limit.class.getCanonicalName(), id, now).getBytes()).getMostSignificantBits());
    }

    private void updateResponsibles(UUID id, List<String> fields, List<UUID> responsibles) {
        final var existsEmployee = context()
                .select(LIMIT_RESPONSIBLE_EMPLOYEE.EMPLOYEE_ID)
                .from(LIMIT_RESPONSIBLE_EMPLOYEE)
                .where(LIMIT_RESPONSIBLE_EMPLOYEE.LIMIT_ID.eq(id))
                .and(LIMIT_RESPONSIBLE_EMPLOYEE.END_DATE.isNull())
                .orderBy(LIMIT_RESPONSIBLE_EMPLOYEE.START_DATE.asc())
                .fetchInto(UUID.class);

        final var indices = fields.stream()
                .map(this::getIndex)
                .map(it -> it.replace("]", ""))
                .map(Integer::parseInt)
                .toList();

        final var replace = indices.stream()
                .map(it -> {
                    if (it >= 0) {
                        return List.of(existsEmployee.get(it));
                    } else {
                        return existsEmployee;
                    }
                })
                .flatMap(Collection::stream)
                .toList();

        context().update(LIMIT_RESPONSIBLE_EMPLOYEE)
                .set(LIMIT_RESPONSIBLE_EMPLOYEE.END_DATE, OffsetDateTime.now())
                .where(LIMIT_RESPONSIBLE_EMPLOYEE.EMPLOYEE_ID.in(replace))
                .and(LIMIT_RESPONSIBLE_EMPLOYEE.LIMIT_ID.eq(id))
                .execute();

        for (final var responsible : responsibles.parallelStream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet())) {
            context().insertInto(LIMIT_RESPONSIBLE_EMPLOYEE)
                    .set(LIMIT_RESPONSIBLE_EMPLOYEE.LIMIT_ID, id)
                    .set(LIMIT_RESPONSIBLE_EMPLOYEE.EMPLOYEE_ID, responsible)
                    .set(LIMIT_RESPONSIBLE_EMPLOYEE.START_DATE, OffsetDateTime.now())
                    .execute();
        }
    }

    @Override
    public ru.sber.transport.database.limits.tables.Limit table() {
        return ru.sber.transport.database.limits.tables.Limit.LIMIT;
    }

    @NotNull
    private LimitRecord createRecord(Record source) {
        var result = new LimitRecord();
        result.fieldStream().parallel().forEach(it -> {
            var last = it.getQualifiedName().last();
            assert last != null;
            var value = source.get(last.toLowerCase(), it.getDataType().getType());
            result.set(ReflectionUtils.cast(it), value);
        });
        return result;
    }

    @Override
    public @NonNull Page<Limit> get(UUID searchOrganizationId, @NonNull LimitFilter filter, @NonNull Integer page, @NonNull Integer size, @NonNull String sort, @NonNull Direction direction) {
        final var request = PageRequest.of(page, size, Sort.Direction.valueOf(direction.name()), sort);
        var query = context().select(table().fields()).from(table());
        final var departmentId = filter.getDepartmentId();
        final var year = filter.getYear();
        final var status = filter.getStatus();
        final var serviceType = filter.getServiceType();
        final var limitOwner = filter.getLimitOwner();
        final var id = filter.getLimitId();
        final var type = filter.getType();
        final var employeeId = filter.getEmployeeId();
        final var humanReadableId = filter.getHumanReadableId();
        final var parentDepartment = filter.getParentDepartment();
        final var requestId = filter.getRequestId();
        final var parentId = filter.getParentId();

        query = joinFilters(limitOwner, query, parentDepartment, requestId);

        var queryData = query.where(DSL.val(true).isTrue());
        if (departmentId != null) {
            queryData = queryData.and(table().DEPARTMENT_ID.eq(departmentId));
        }
        if (searchOrganizationId != null) {
            queryData = queryData.and(table().ORGANIZATION_ID.eq(searchOrganizationId));
        }
        if (year != null) {
            queryData = queryData.and(table().YEAR.eq(year));
        }
        if (status != null) {
            queryData = queryData.and(table().LIMIT_STATUS.eq(Status.valueOf(status.name())));
        }
        if (serviceType != null) {
            queryData = queryData.and(table().SERVICE_TYPE.eq(serviceType));
        }
        if (id != null) {
            queryData = queryData.and(table().ID.eq(id));
        }
        if (type != null) {
            queryData = queryData.and(table().LIMIT_TYPE.eq(Type.valueOf(type.name())));
        }
        if (StringUtils.hasText(humanReadableId)) {
            queryData = queryData.and(table().HUMAN_READABLE_ID.likeIgnoreCase(LIKE_FORMAT.formatted(humanReadableId)));
        }
        if (employeeId != null) {
            queryData = queryData.and(table().EMPLOYEE_ID.eq(employeeId));
        }
        if (parentId != null) {
            queryData = queryData.and(table().PARENT_ID.eq(parentId));
        }

        if (StringUtils.hasText(limitOwner)) {
            queryData = queryData.and(DSL.or(
                    DSL.concat(EMPLOYEE.LAST_NAME, EMPLOYEE.FIRST_NAME, DSL.coalesce(EMPLOYEE.PATRONYMIC, "")).likeIgnoreCase(LIKE_FORMAT.formatted(limitOwner.replaceAll("[.\\s]", ""))),
                    DSL.concat(EMPLOYEE.FIRST_NAME, DSL.coalesce(EMPLOYEE.PATRONYMIC, ""), EMPLOYEE.LAST_NAME).likeIgnoreCase(LIKE_FORMAT.formatted(limitOwner.replaceAll("[.\\s]", ""))),
                    EMPLOYEE.PERSONNEL_NUMBER.likeIgnoreCase(LIKE_FORMAT.formatted(limitOwner.replaceAll("[.\\s]", "")))
            ));
        }
        if (StringUtils.hasText(parentDepartment)) {
            queryData = queryData.and(DSL.or(
                    DEPARTMENT.CODE.likeIgnoreCase(LIKE_FORMAT.formatted(parentDepartment.replaceAll("[.\\s]", ""))),
                    DEPARTMENT.DEPARTMENT_NAME.likeIgnoreCase(LIKE_FORMAT.formatted(parentDepartment.replaceAll("[.\\s]", "")))
            ));
        }
        if (requestId != null) {
            queryData = queryData.and(LIMIT_REQUEST.ID.eq(requestId));
        }

        final var total = context().fetchCount(queryData);
        final var wrappedQuery = wrap(queryData, request);
        final var content = wrappedQuery
                .fetchInto(LimitRecord.class)
                .stream()
                .map(mapper::toBusiness)
                .toList();
        final var contentMap = content.parallelStream().collect(Collectors.toMap(Limit::getId, it -> it));
        getResponsibles(content.parallelStream().map(Limit::getId).collect(Collectors.toList()))
                .entrySet()
                .parallelStream()
                .forEach(it -> contentMap.get(it.getKey()).setResponsibles(it.getValue().stream().distinct().toList()));
        return new Page<>(new PageImpl<>(content, request, total));
    }

    @Override
    public Optional<Limit> getOfSharing(UUID limitSharingId) {
        return context().select(table().fields())
                .from(table())
                .innerJoin(SHARINGS).on(SHARINGS.LIMIT_ID.eq(table().ID))
                .where(SHARINGS.ID.eq(limitSharingId))
                .fetchOptionalInto(LimitRecord.class)
                .map(mapper::toBusiness)
                .map(it -> {
                    it.setResponsibles(getResponsibles(it.getId()));
                    return it;
                });
    }

    private SelectJoinStep<Record> joinFilters(String limitOwner, SelectJoinStep<Record> query, String parentDepartment, UUID requestId) {
        if (StringUtils.hasText(limitOwner)) {
            query = query.innerJoin(EMPLOYEE).on(table().LIMIT_OWNER_ID.eq(EMPLOYEE.ID));
        }
        if (StringUtils.hasText(parentDepartment)) {
            query = query.innerJoin(DEPARTMENT).on(table().PARENT_DEPARTMENT_ID.eq(DEPARTMENT.ID));
        }
        if (requestId != null) {
            query = query.innerJoin(LIMIT_REQUEST).on(table().ID.eq(LIMIT_REQUEST.ID));
        }
        return query;
    }

    private String getIndex(String source) {
        final var fieldParts = source.split(":");
        final var operation = fieldParts[0];
        final var effectiveField = fieldParts[1];
        if ("ADD".equals(operation)) {
            return "-2";
        } else if ("REPLACE".equals(operation) && effectiveField.contains("[")) {
            return effectiveField.split("\\[")[1];
        }
        return "-1";
    }

    @SneakyThrows(NumberFormatException.class)
    private Object getValue(Field<?> field, boolean add, Object value) {
        if (String.valueOf(value).matches("\\d+")) {
            return add ? field.add(Double.parseDouble(String.valueOf(value))) : value;
        } else {
            return value;
        }
    }

    private List<UUID> getResponsibles(UUID id) {
        return getResponsibles(List.of(id)).getOrDefault(id, Collections.emptyList())
                .stream().distinct().toList();
    }

    private Map<UUID, List<UUID>> getResponsibles(List<UUID> ids) {
        final var recursiveLimit = DSL.name("recursive_limit");
        final var recursiveLimitParentField = DSL.field(recursiveLimit.append(table().PARENT_ID.getName().toLowerCase()));
        final var recursiveLimitStartDateField = DSL.field(recursiveLimit.append(LIMIT_RESPONSIBLE_EMPLOYEE.START_DATE.getName().toLowerCase()));
        final var recursiveLimitEndDateField = DSL.field(recursiveLimit.append(LIMIT_RESPONSIBLE_EMPLOYEE.END_DATE.getName().toLowerCase()));
        final var recursiveLimitIdField = DSL.field(recursiveLimit.append(table().ID.getName().toLowerCase()));
        final var recursiveLimitResponsibleField = DSL.field(recursiveLimit.append(LIMIT_RESPONSIBLE_EMPLOYEE.EMPLOYEE_ID.getName().toLowerCase()));
        final var sourceIdField = DSL.name("source");
        final var stepField = DSL.field(DSL.name("step"), Integer.class);

        final var step = new AtomicInteger(-1);

        return context()
                .withRecursive(recursiveLimit)
                .as(context().select(DSL.val(0).as(stepField), table().ID.as(sourceIdField), table().ID, table().PARENT_ID, LIMIT_RESPONSIBLE_EMPLOYEE.EMPLOYEE_ID, LIMIT_RESPONSIBLE_EMPLOYEE.START_DATE, LIMIT_RESPONSIBLE_EMPLOYEE.END_DATE).from(table()).leftJoin(LIMIT_RESPONSIBLE_EMPLOYEE).on(table().ID.eq(LIMIT_RESPONSIBLE_EMPLOYEE.LIMIT_ID)).where(table().ID.in(ids))
                .union(context().select(stepField.add(1).as(stepField), recursiveLimitIdField.as(sourceIdField).cast(UUID.class), table().ID, table().PARENT_ID, LIMIT_RESPONSIBLE_EMPLOYEE.EMPLOYEE_ID, LIMIT_RESPONSIBLE_EMPLOYEE.START_DATE, LIMIT_RESPONSIBLE_EMPLOYEE.END_DATE).from(table()).leftJoin(LIMIT_RESPONSIBLE_EMPLOYEE).on(table().ID.eq(LIMIT_RESPONSIBLE_EMPLOYEE.LIMIT_ID)).innerJoin(recursiveLimit).on(recursiveLimitParentField.eq(table().ID)).where(recursiveLimitStartDateField.isNull().or(recursiveLimitEndDateField.isNotNull().and(LIMIT_RESPONSIBLE_EMPLOYEE.START_DATE.isNotNull()).and(LIMIT_RESPONSIBLE_EMPLOYEE.END_DATE.isNull())))))
                .selectFrom(recursiveLimit)
                .where(recursiveLimitStartDateField.isNotNull())
                .and(recursiveLimitEndDateField.isNull())
                .orderBy(stepField.asc())
                .fetch()
                .map(it -> {
                    final var currentStep = it.get(stepField, Integer.class);
                    if (step.get() == -1 || step.get() == currentStep) {
                        step.set(currentStep);
                        return new AbstractMap.SimpleEntry<>(it.get(sourceIdField, UUID.class), it.get(recursiveLimitResponsibleField, UUID.class));
                    } else {
                        return null;
                    }
                })
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }
}
