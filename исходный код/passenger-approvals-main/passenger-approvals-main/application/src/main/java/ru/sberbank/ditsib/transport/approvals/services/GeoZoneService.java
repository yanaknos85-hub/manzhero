package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.PurposeAndRegionApprovalSettingsItem;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.GeoZone;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с геозонами.
 */
public interface GeoZoneService {
    
    /**
     * Получение гео-зоны.
     *
     * @param id идентификатор.
     * @return геозона.
     */
    Optional<GeoZone> get(UUID id);

    /**
     * Проверить все геозоны из настроек согласований на предмет наличия в БД
     * @param settingsItems элементы настроек согласований
     */
    List<PurposeAndRegionApprovalSettingsItem> setAllGeoZonesToSettingsItems(
            List<PurposeAndRegionApprovalSettingsItem> settingsItems);
}
