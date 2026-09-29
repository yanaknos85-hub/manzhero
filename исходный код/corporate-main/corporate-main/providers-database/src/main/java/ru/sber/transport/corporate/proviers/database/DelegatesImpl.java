package ru.sber.transport.corporate.proviers.database;

import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.corporate.providers.Delegates;
import ru.sber.transport.database.corporate.Tables;
import ru.sber.transport.database.corporate.tables.Delegate;
import ru.sber.transport.database.corporate.tables.records.DelegateRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация работы делегатов
 */
@Transactional
public class DelegatesImpl implements Delegates, JooqRepository<Delegate, DelegateRecord, UUID> {

    @Override
    public Delegate table() {
        return Tables.DELEGATE;
    }

    @Override
    public long count() {
        return context().fetchCount(context().select(table().ID).from(table()).where(table().STATUS.eq("ACTIVE")));
    }

    @Override
    public List<ru.sber.transport.corporate.model.Delegate> get(int start, int limit) {
        return context().selectFrom(table())
                .where(table().STATUS.eq("ACTIVE"))
                .orderBy(table().START_DATE.asc())
                .offset(start)
                .limit(limit)
                .fetchInto(DelegateRecord.class)
                .stream()
                .map(this::toModel)
                .toList();
    }

    private ru.sber.transport.corporate.model.Delegate toModel(DelegateRecord delegateRecord) {
        return new ru.sber.transport.corporate.model.Delegate() {

            @Override
            public UUID id() {
                return delegateRecord.getId();
            }

            @Override
            public UUID delegateId() {
                return delegateRecord.getUserId();
            }

            @Override
            public UUID supervisorId() {
                return delegateRecord.getSupervisorId();
            }

            @Override
            public LocalDate startDate() {
                return delegateRecord.getStartDate();
            }

            @Override
            public LocalDate endDate() {
                return delegateRecord.getEndDate();
            }

            @Override
            public String type() {
                return delegateRecord.getTransportType();
            }

            @Override
            public boolean deleted() {
                return "INACTIVE".equals(delegateRecord.getStatus());
            }
        };
    }

    @Override
    public Optional<ru.sber.transport.corporate.model.Delegate> get(UUID id) {
        return findById(id).map(this::toModel);
    }
}
