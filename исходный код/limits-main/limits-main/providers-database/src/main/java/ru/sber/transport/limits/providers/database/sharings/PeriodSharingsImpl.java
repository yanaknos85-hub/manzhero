package ru.sber.transport.limits.providers.database.sharings;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jooq.SelectConditionStep;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.limits.Tables;
import ru.sber.transport.database.limits.enums.Period;
import ru.sber.transport.database.limits.enums.Status;
import ru.sber.transport.database.limits.tables.LimitSharingPerPeriod;
import ru.sber.transport.database.limits.tables.records.LimitSharingPerPeriodRecord;
import ru.sber.transport.limits.model.*;
import ru.sber.transport.limits.providers.Departments;
import ru.sber.transport.limits.providers.PeriodSharings;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static ru.sber.transport.database.limits.Tables.LIMIT;
import static ru.sber.transport.database.limits.Tables.SHARINGS;

@RequiredArgsConstructor
public class PeriodSharingsImpl implements PeriodSharings, JooqRepository<LimitSharingPerPeriod, LimitSharingPerPeriodRecord, UUID> {

    private final Departments departments;

    @Override
    public LimitSharingPerPeriod table() {
        return Tables.LIMIT_SHARING_PER_PERIOD;
    }

    @Override
    public Optional<PeriodSharing> get(@NonNull Service service, @NonNull Type type, @NonNull OffsetDateTime date, @NonNull Employee employee, boolean personal) {
        var query = (Supplier<SelectConditionStep<org.jooq.Record>>) () -> context().select(table().fields())
                .from(table())
                .innerJoin(SHARINGS).on(SHARINGS.ID.eq(table().LIMIT_SHARING_ID))
                .innerJoin(LIMIT).on(LIMIT.ID.eq(SHARINGS.LIMIT_ID))
                .where(LIMIT.LIMIT_STATUS.eq(Status.SHARED))
                .and(LIMIT.SERVICE_TYPE.eq(service.name()))
                .and(SHARINGS.TRANSPORT_TYPE.eq(type.name()))
                .and(LIMIT.YEAR.eq(date.getYear()))
                .and(table().PERIOD.eq(Period.valueOf(date.getMonth().name())));
        if (personal) {
            return query.get().and(LIMIT.EMPLOYEE_ID.eq(employee.id())).fetchOptionalInto(LimitSharingPerPeriodRecord.class)
                    .map(this::toBusiness);
        } else {
            Optional<LimitSharingPerPeriodRecord> result;
            Department department = departments.get(employee.departmentId());
            do {
                result = query.get().and(LIMIT.DEPARTMENT_ID.eq(department.id())).fetchOptionalInto(LimitSharingPerPeriodRecord.class);
                department = Optional.ofNullable(department.parentId()).map(departments::get).orElse(null);
            } while (result.isEmpty() && department != null);
            return result.map(this::toBusiness);
        }
    }

    @Override
    public void update(PeriodSharing periodSharing, BigDecimal remains) {
        context().update(table())
                .set(table().BALANCE, remains)
                .where(table().ID.eq(periodSharing.id()))
                .execute();
    }

    @Override
    public Optional<PeriodSharing> get(UUID id) {
        return findById(id).map(this::toBusiness);
    }

    private PeriodSharing toBusiness(LimitSharingPerPeriodRecord sharingsRecord) {
        return new PeriodSharing() {
            @Override
            public UUID id() {
                return sharingsRecord.getId();
            }

            @Override
            public BigDecimal remains() {
                return sharingsRecord.getBalance();
            }
        };
    }
}
