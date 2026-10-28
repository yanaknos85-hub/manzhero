package ru.sber.transport.trips_reports.providers.column_settings.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.springframework.stereotype.Repository;
import ru.sber.transport.trips_reports.database.trips_reports.Keys;
import ru.sber.transport.trips_reports.database.trips_reports.Tables;
import ru.sber.transport.trips_reports.database.trips_reports.tables.records.ColumnSettingsRecord;
import ru.sber.transport.trips_reports.providers.column_settings.ColumnSettingsProvider;
import ru.sber.transport.trips_reports.providers.column_settings.mapping.ColumnSettingsMapper;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
@Slf4j
public class ColumnSettingsProviderImpl implements ColumnSettingsProvider {

    private final DSLContext dslContext;

    private final ColumnSettingsMapper columnSettingsMapper;

    @Override
    public Map<String, Object> save(UUID userId, Map<String, Object> setting) {
        var settings = new ColumnSettingsRecord(userId, columnSettingsMapper.toJson(setting));
        dslContext.insertInto(Tables.COLUMN_SETTINGS).set(settings)
                .onConflict(Keys.PK_COLUMN_SETTINGS.getFields()).doUpdate().set(settings).execute();
        return get(userId);
    }

    @Override
    public void update(UUID userId, Map<String, Object> setting) {
        var settings = new ColumnSettingsRecord(userId, columnSettingsMapper.toJson(setting));
        dslContext.update(Tables.COLUMN_SETTINGS).set(settings).execute();
    }

    @Override
    public void delete(UUID userId) {
        dslContext.deleteFrom(Tables.COLUMN_SETTINGS).where(Tables.COLUMN_SETTINGS.USER_ID.eq(userId)).execute();
    }

    @Override
    public Map<String, Object> get(UUID userId) {
        var result = dslContext.select(Tables.COLUMN_SETTINGS.SETTING).from(Tables.COLUMN_SETTINGS)
                .where(Tables.COLUMN_SETTINGS.USER_ID.eq(userId))
                .fetchOptional();

        return result.map(Record1::value1)
                .map(columnSettingsMapper::toMap)
                .orElseGet(() -> {
                    log.debug("Column settings not found for user {}", userId);
                    return Collections.emptyMap();
                });
    }
}
