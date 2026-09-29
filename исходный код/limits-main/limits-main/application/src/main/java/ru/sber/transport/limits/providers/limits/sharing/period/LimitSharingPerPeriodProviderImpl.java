package ru.sber.transport.limits.providers.limits.sharing.period;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.limits.enums.Period;
import ru.sber.transport.database.limits.enums.Status;
import ru.sber.transport.database.limits.tables.records.LimitSharingPerPeriodRecord;
import ru.sber.transport.limits.business.model.LimitSharingPerPeriod;
import ru.sber.transport.limits.business.providers.LimitSharingPerPeriodProvider;
import ru.sber.transport.limits.providers.limits.sharing.period.mappers.LimitSharingPerPeriodDatabaseMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static ru.sber.transport.database.limits.Tables.LIMIT;
import static ru.sber.transport.database.limits.Tables.SHARINGS;
import static ru.sber.transport.database.limits.tables.LimitSharingPerPeriod.LIMIT_SHARING_PER_PERIOD;

@Repository
@Transactional
@RequiredArgsConstructor
public class LimitSharingPerPeriodProviderImpl implements LimitSharingPerPeriodProvider, JooqRepository<ru.sber.transport.database.limits.tables.LimitSharingPerPeriod, LimitSharingPerPeriodRecord, UUID> {

    private final LimitSharingPerPeriodDatabaseMapper mapper;

    @Override
    public List<LimitSharingPerPeriod> getAll(UUID limitSharingId) {
        return context().selectFrom(table()).where(table().LIMIT_SHARING_ID.eq(limitSharingId))
                .fetchInto(LimitSharingPerPeriodRecord.class).stream().map(mapper::toBusiness).toList();
    }

    @Override
    public void save(LimitSharingPerPeriod limitSharing) {
        var found = findById(limitSharing.getId()).orElseGet(LimitSharingPerPeriodRecord::new);
        mapper.update(found, limitSharing);
        if (found.getId() == null) {
            found.setId(UUID.randomUUID());
        }
        if (found.getCreationTime() == null) {
            found.setCreationTime(LocalDateTime.now());
        }
        save(found);
    }

    @Override
    public List<LimitSharingPerPeriod> get(LocalDate date) {
        return context()
                .select(table().fields())
                .from(table())
                .innerJoin(SHARINGS).on(SHARINGS.ID.eq(table().LIMIT_SHARING_ID))
                .innerJoin(LIMIT).on(LIMIT.ID.eq(SHARINGS.LIMIT_ID))
                .where(table().PERIOD.eq(Period.valueOf(date.getMonth().name())))
                .and(LIMIT.YEAR.eq(date.getYear()))
                .and(LIMIT.LIMIT_STATUS.eq(Status.SHARED))
                .fetchInto(LimitSharingPerPeriodRecord.class)
                .stream()
                .map(mapper::toBusiness)
                .toList();
    }

    @Override
    public List<LimitSharingPerPeriod> getNotMoved(LocalDate date) {
        return context()
                .select(table().fields())
                .from(table())
                .innerJoin(SHARINGS).on(SHARINGS.ID.eq(table().LIMIT_SHARING_ID))
                .innerJoin(LIMIT).on(LIMIT.ID.eq(SHARINGS.LIMIT_ID))
                .where(table().PERIOD.eq(Period.valueOf(date.getMonth().name())))
                .and(LIMIT.YEAR.eq(date.getYear()))
                .and(LIMIT.LIMIT_STATUS.eq(Status.SHARED))
                .and(LIMIT_SHARING_PER_PERIOD.MOVED_TO_NEXT.isFalse())
                .fetchInto(LimitSharingPerPeriodRecord.class)
                .stream()
                .map(mapper::toBusiness)
                .toList();
    }

    @Override
    public Optional<LimitSharingPerPeriod> get(UUID id) {
        return findById(id).map(mapper::toBusiness);
    }

    @Override
    public List<LimitSharingPerPeriod> get(Set<UUID> ids, Month month) {
        return context().selectFrom(table())
                .where(table().LIMIT_SHARING_ID.in(ids))
                .and(table().PERIOD.eq(Period.valueOf(month.name())))
                .fetchInto(LimitSharingPerPeriodRecord.class)
                .stream()
                .map(mapper::toBusiness)
                .toList();
    }

    @Override
    public void setNotified(UUID id) {
        context().update(table())
                .set(table().NOTICED_AT, OffsetDateTime.now())
                .where(table().ID.eq(id))
                .execute();
    }

    @Override
    public ru.sber.transport.database.limits.tables.LimitSharingPerPeriod table() {
        return LIMIT_SHARING_PER_PERIOD;
    }
}
