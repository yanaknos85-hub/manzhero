package ru.sberbank.ditsib.transport.approvals.services;

import ru.sberbank.ditsib.transport.approvals.database.model.PurposeAndRegionApprovalSettingsItem;
import ru.sberbank.ditsib.transport.approvals.database.model.TripPurpose;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис для работы с целями
 **/
public interface TripPurposeService {
    void save(TripPurpose purpose);
    
    List<TripPurpose> getByIds(Set<UUID> idList);

    /**
     * Проверить каждую цель из настроек согласований на предмет наличия в БД
     * @param settingsItems элементы настроек согласований
     */
    List<PurposeAndRegionApprovalSettingsItem> setAllPurposesToSettingsItems(
            List<PurposeAndRegionApprovalSettingsItem> settingsItems);
    
    /**
     * Получить цель по ID или исключение
     * @param id ID цели
     * @return цель поездки
     */
    TripPurpose get(UUID id);
}
