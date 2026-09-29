package ru.sber.transport.corporate.providers.human_readable.position;

import lombok.RequiredArgsConstructor;
import org.jooq.Field;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.providers.human_readable.BaseHumanReadableProvider;

import static ru.sber.transport.database.corporate.Tables.POSITION;

@Transactional
@RequiredArgsConstructor
@Repository
class PositionHumanReadableProviderImpl extends BaseHumanReadableProvider<ru.sber.transport.database.corporate.tables.Position, Position> {

    @Override
    protected String prefix() {
        return "PS";
    }

    @Override
    protected ru.sber.transport.database.corporate.tables.Position entityTable() {
        return POSITION;
    }

    @Override
    protected Field<String> humanReadableField(ru.sber.transport.database.corporate.tables.Position table) {
        return table.HUMANREADABLEID;
    }

    @Override
    public Class<Position> entityClass() {
        return Position.class;
    }
}
