package ru.sber.transport.limits.providers.database.spendings;

import org.jooq.impl.DSL;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.limits.Tables;
import ru.sber.transport.database.limits.enums.SpendingStatus;
import ru.sber.transport.database.limits.tables.records.SpendingsRecord;
import ru.sber.transport.limits.model.PeriodSharing;
import ru.sber.transport.limits.model.Reserve;
import ru.sber.transport.limits.model.ReserveStatus;
import ru.sber.transport.limits.model.Spending;
import ru.sber.transport.limits.providers.Spendings;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Transactional
public class SpendingsImpl implements Spendings, JooqRepository<ru.sber.transport.database.limits.tables.Spendings, SpendingsRecord, UUID> {

    @Override
    public Optional<Spending> get(UUID id) {
        return context().selectFrom(table())
                .where(table().REQUEST_ID.eq(id))
                .fetchOptional()
                .map(this::createSpending);
    }

    @Override
    public void update(Spending spending, ReserveStatus status) {
        var query = context().update(table())
                .set(table().STATUS, SpendingStatus.valueOf(status.name()));
        if (ReserveStatus.SPENT == status && status != spending.status()) {
            query = query.set(table().SPENDING_TIME, OffsetDateTime.now())
                    .set(table().SPENT, Tables.SPENDINGS.RESERVED);
        }
        if (ReserveStatus.CANCELED == status && status != spending.status()) {
            query = query.set(table().CANCEL_TIME, OffsetDateTime.now());
        }
        query.where(table().ID.eq(spending.id())).execute();
    }

    @Override
    public void create(PeriodSharing sharing, Reserve data) {
        context().insertInto(table())
                .set(table().ID, UUID.randomUUID())
                .set(table().RESERVED, data.cost())
                .set(table().PERIOD_SHARING_ID, sharing.id())
                .set(table().STATUS, SpendingStatus.RESERVED)
                .set(table().EMPLOYEE_ID, data.consumerId())
                .set(table().REQUEST_ID, data.id())
                .set(table().RESERVATION_TIME, OffsetDateTime.now())
                .onConflict(table().REQUEST_ID)
                .doUpdate()
                .set(table().RESERVED, DSL.excluded(table().RESERVED))
                .set(table().RESERVATION_TIME, DSL.excluded(table().RESERVATION_TIME))
                .execute();
    }

    @Override
    public ru.sber.transport.database.limits.tables.Spendings table() {
        return Tables.SPENDINGS;
    }

    private Spending createSpending(SpendingsRecord spendingsRecord) {
        return new Spending() {

            @Override
            public UUID id() {
                return spendingsRecord.getId();
            }

            @Override
            public BigDecimal reserved() {
                return spendingsRecord.getReserved();
            }

            @Override
            public UUID periodSharing() {
                return spendingsRecord.getPeriodSharingId();
            }

            @Override
            public ReserveStatus status() {
                return ReserveStatus.valueOf(spendingsRecord.getStatus().name());
            }
        };
    }
}