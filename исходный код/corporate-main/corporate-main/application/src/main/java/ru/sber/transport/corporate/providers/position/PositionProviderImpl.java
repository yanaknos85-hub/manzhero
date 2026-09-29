package ru.sber.transport.corporate.providers.position;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.Record;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.corporate.business.model.Position;
import ru.sber.transport.corporate.business.model.PositionFilter;
import ru.sber.transport.corporate.business.providers.HumanReadableProvider;
import ru.sber.transport.corporate.providers.BaseProvider;
import ru.sber.transport.database.corporate.tables.records.PositionRecord;
import ru.sber.transport.corporate.providers.position.mappers.PositionDatabaseMapper;

import java.time.OffsetDateTime;
import java.util.*;

@RequiredArgsConstructor
@Repository
@Transactional
@Getter(AccessLevel.PROTECTED)
class PositionProviderImpl extends BaseProvider<Position, PositionFilter, PositionRecord> implements PositionProvider {

    private final PositionDatabaseMapper mapper;

    private final HumanReadableProvider<Position> humanReadableProvider;

    @Override
    protected PositionRecord createItem() {
        return new PositionRecord();
    }

    @Override
    protected List<TableField<PositionRecord, ?>> minProjection() {
        return List.of(table().ID, table().NAME, table().ORGANIZATION_ID);
    }

    @Override
    protected SelectConditionStep<Record> filtering(SelectJoinStep<Record> query, Table<PositionRecord> table, PositionFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected TableField<PositionRecord, UUID> id() {
        return table().ID;
    }

    @Override
    protected TableField<PositionRecord, OffsetDateTime> updateTime() {
        return table().UPDATE_TIME;
    }

    @Override
    public ru.sber.transport.database.corporate.tables.Position table() {
        return ru.sber.transport.database.corporate.tables.Position.POSITION;
    }

    @Override
    protected TableField<PositionRecord, String> humanReadableId() {
        return table().HUMANREADABLEID;
    }

    @Override
    protected TableField<PositionRecord, UUID> organizationId() {
        return table().ORGANIZATION_ID;
    }

    @Override
    protected TableField<PositionRecord, String> syncId() {
        return table().SYNC_ID;
    }
}
