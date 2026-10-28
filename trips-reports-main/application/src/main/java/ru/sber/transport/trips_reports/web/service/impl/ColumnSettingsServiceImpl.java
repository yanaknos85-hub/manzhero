package ru.sber.transport.trips_reports.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.trips_reports.providers.column_settings.ColumnSettingsProvider;
import ru.sber.transport.trips_reports.web.service.ColumnSettingsService;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ColumnSettingsServiceImpl implements ColumnSettingsService {

    private final ColumnSettingsProvider columnSettingsProvider;

    @Override
    public Map<String, Object> createColumnSetting(UUID userId, Map<String, Object> setting) {
        return columnSettingsProvider.save(userId, setting);
    }

    @Override
    public void updateColumnSetting(UUID userId, Map<String, Object> setting) {
        columnSettingsProvider.update(userId, setting);
    }

    @Override
    public void deleteColumnSetting(UUID userId) {
        columnSettingsProvider.delete(userId);
    }

    @Override
    public Map<String, Object> getColumnSetting(UUID userId) {
        return columnSettingsProvider.get(userId);
    }
}
