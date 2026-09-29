package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.approvals.database.dao.PublicTrApprovalsSettingsRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.Organization;
import ru.sberbank.ditsib.transport.approvals.database.model.PublicTrApprovalsSettings;
import ru.sberbank.ditsib.transport.approvals.database.model.PurposeAndRegionApprovalSettingsItem;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.ApprovalsSettingsSender;
import ru.sberbank.ditsib.transport.approvals.services.ApprovalsSettingsService;
import ru.sberbank.ditsib.transport.approvals.services.GeoZoneService;
import ru.sberbank.ditsib.transport.approvals.services.OrganizationService;
import ru.sberbank.ditsib.transport.approvals.services.TripPurposeService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class PublicTrApprovalsSettingsServiceImpl
        implements ApprovalsSettingsService<PublicTrApprovalsSettings> {
    
    private final PublicTrApprovalsSettingsRepository settingsRepository;
    private final OrganizationService organizationService;
    private final TripPurposeService tripPurposeService;
    private final GeoZoneService geoZoneService;
    private final ApprovalsSettingsSender sender;
    
    @Override
    public PublicTrApprovalsSettings add(UUID organizationId, PublicTrApprovalsSettings newSettings) {
        // убедиться, что корп.клиент и цели поездки уже есть в БД, а также что настройки еще не созданы
        Organization organization = organizationService.getOrThrow(organizationId);
        checkSettingsIsUnique(organizationId);
        // установить геозоны и цели из БД, или бросить исключение
        List<PurposeAndRegionApprovalSettingsItem> settingsItems = newSettings.getPurposeAndRegionItems();
        if (settingsItems != null) {
            settingsItems = geoZoneService.setAllGeoZonesToSettingsItems(settingsItems);
            settingsItems = tripPurposeService.setAllPurposesToSettingsItems(settingsItems);
        }
        //сохранить настройки
        newSettings.setTransportType("PUBLIC");
        newSettings.setOrganization(organization);
        newSettings = settingsRepository.save(newSettings);
        sender.send(newSettings);
        return newSettings;
    }
    
    @Override
    public PublicTrApprovalsSettings get(UUID organizationId) {
        organizationService.getOrThrow(organizationId);
        return getSettingOrThrow(organizationId);
    }
    
    @Override
    public void update(UUID organizationId, UUID settingId, PublicTrApprovalsSettings updateData) {
        // убедиться, что корп.клиент, все геозоны и цели поездки уже есть в БД, а также что настройки уже созданы
        Organization organization = organizationService.getOrThrow(organizationId);
        getSettingOrThrow(organizationId);
        validateSettingExists(settingId);
        // установить геозоны и цели из БД, или бросить исключение
        List<PurposeAndRegionApprovalSettingsItem> settingsItems = updateData.getPurposeAndRegionItems();
        if (settingsItems != null) {
            settingsItems = geoZoneService.setAllGeoZonesToSettingsItems(settingsItems);
            settingsItems = tripPurposeService.setAllPurposesToSettingsItems(settingsItems);
        }
        updateData.setTransportType("PUBLIC");
        updateData.setOrganization(organization);
        updateData.setId(settingId);
        updateData = settingsRepository.save(updateData);
        sender.send(updateData);
    }
    
    @Override
    public void delete(UUID organizationId, UUID settingId) {
        PublicTrApprovalsSettings deleted = get(organizationId);
        if (!deleted.getId().equals(settingId)) {
            throw getNotFoundException(organizationId);
        }
        sender.sendDeleted(deleted);
        settingsRepository.delete(deleted);
    }
    
    @Override
    public PublicTrApprovalsSettings restoreValues(UUID organizationId, UUID settingId) {
        PublicTrApprovalsSettings setting = get(organizationId);
        PublicTrApprovalsSettings newValue = PublicTrApprovalsSettings.builder()
                                                                      .id(settingId)
                                                                      .organization(setting.getOrganization())
                                                                      .transportType("PUBLIC")
                                                                      .build();
        newValue = settingsRepository.save(newValue);
        sender.send(newValue);
        return newValue;
    }
    
    /**
     * Возвращает существуеющую настройку для данной организации, либо бросает исключение
     *
     * @param organizationId id корп.клиента
     *
     * @return настройка, если существует
     */
    private PublicTrApprovalsSettings getSettingOrThrow(UUID organizationId) {
        return settingsRepository.findByOrganizationId(organizationId)
                                 .orElseGet(PublicTrApprovalsSettings::new);
    }

    /**
     * Получить исключение
     *
     * @param organizationId id корп.клиента
     *
     * @return EntityNotFoundException
     */
    private EntityNotFoundException getNotFoundException(UUID organizationId) {
        return new EntityNotFoundException(PublicTrApprovalsSettings.class, Map.of("organizationId", organizationId, "transportType", "PUBLIC"));
    }
    
    /**
     * Проверяет, существует ли настройка
     *
     * @param settingId
     */
    private void validateSettingExists(UUID settingId) {
        settingsRepository.findById(settingId).orElseThrow(() -> new EntityNotFoundException(
                PublicTrApprovalsSettings.class, settingId
        ));
    }
    
    /**
     * Проверить, что настроек для данной организации еще нет в БД
     *
     * @param organizationId ID организации
     */
    private void checkSettingsIsUnique(UUID organizationId) {
        settingsRepository.findByOrganizationId(organizationId).ifPresent(settings -> {
            throw new DuplicateDataException(
                    PublicTrApprovalsSettings.class,
                    "id",
                    settings.getId());
        });
    }
}
