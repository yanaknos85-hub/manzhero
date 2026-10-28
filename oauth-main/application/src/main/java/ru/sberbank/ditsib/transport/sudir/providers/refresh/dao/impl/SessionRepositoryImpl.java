package ru.sberbank.ditsib.transport.sudir.providers.refresh.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.database.sudir.tables.Account;
import ru.sber.transport.database.sudir.tables.Session;
import ru.sber.transport.database.sudir.tables.records.SessionRecord;
import ru.sberbank.ditsib.transport.sudir.providers.refresh.dao.SessionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
class SessionRepositoryImpl implements SessionRepository {

    @Override
    public Optional<SessionRecord> findByToken(String accessToken) {
        return context().selectFrom(table())
                .where(table().TOKEN.eq(accessToken))
                .fetchOptional();
    }

    @Override
    public List<SessionRecord> findAllByAccountAndExpiredAtAfter(String personnelNumber, LocalDateTime expiration) {
        var account = Account.ACCOUNT;
        return context().select()
                .from(table())
                .innerJoin(account).on(account.ID.eq(table().ACCOUNT_ID))
                .where(table().EXPIRE_AT.greaterThan(expiration))
                .fetchInto(SessionRecord.class);
    }

    @Override
    public Optional<SessionRecord> findByIdAndExpiredAtAfter(UUID id, LocalDateTime expiration) {
        return context().selectFrom(table())
                .where(table().EXPIRE_AT.greaterThan(expiration))
                .and(table().ID.eq(id))
                .fetchOptional();
    }

    @Override
    public List<SessionRecord> findAllByExpiredAtBefore(LocalDateTime expiration) {
        return context().selectFrom(table())
                .where(table().EXPIRE_AT.lessOrEqual(expiration))
                .fetchInto(SessionRecord.class);
    }

    @Override
    public Session table() {
        return Session.SESSION;
    }
}
