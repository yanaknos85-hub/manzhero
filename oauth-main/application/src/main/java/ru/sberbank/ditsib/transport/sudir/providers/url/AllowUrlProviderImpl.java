package ru.sberbank.ditsib.transport.sudir.providers.url;

import org.springframework.stereotype.Repository;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.sudir.tables.Allow;
import ru.sber.transport.database.sudir.tables.records.AllowRecord;
import ru.sberbank.ditsib.transport.sudir.sudir.providers.AllowUrlProvider;

import static org.jooq.impl.DSL.inline;

@Repository
class AllowUrlProviderImpl implements AllowUrlProvider, JooqRepository<Allow, AllowRecord, String> {

    @Override
    public boolean allow(String url) {
        return context().fetchExists(context().selectFrom(table()).where(inline(url).likeRegex(table().URL)));
    }

    @Override
    public Allow table() {
        return Allow.ALLOW;
    }
}
