package ru.sberbank.ditsib.transport.sudir.providers.account.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.database.sudir.tables.Account;
import ru.sber.transport.database.sudir.tables.records.AccountRecord;
import ru.sberbank.ditsib.transport.sudir.providers.account.dao.AccountRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
class AccountRepositoryImpl implements AccountRepository {

    @Override
    public Optional<AccountRecord> findByActiveTrueAndPersonnelNumber(String personnelNumber) {
        return context().selectFrom(table()).where(table().ACTIVE.isTrue()).and(table().PERSONNEL_NUMBER.eq(personnelNumber))
                .fetchOptional();
    }

    @Override
    public Optional<AccountRecord> findByActiveAndId(UUID userId, boolean active) {
        return context().selectFrom(table()).where(table().ACTIVE.eq(active)).and(table().ID.eq(userId))
                .fetchOptional();
    }

    @Override
    public AccountRecord getByPersonnelNumber(String personnelNumber) {
        return context().selectFrom(table()).where(table().ACTIVE.isTrue()).and(table().PERSONNEL_NUMBER.eq(personnelNumber))
                .fetchOne();
    }

    @Override
    public Account table() {
        return Account.ACCOUNT;
    }
}
