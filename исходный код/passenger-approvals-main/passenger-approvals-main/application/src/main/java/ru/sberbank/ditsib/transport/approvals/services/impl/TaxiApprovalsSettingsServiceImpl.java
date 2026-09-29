package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.approvals.database.dao.TaxiApprovalsSettingsRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.Approval;
import ru.sberbank.ditsib.transport.approvals.database.model.Organization;
import ru.sberbank.ditsib.transport.approvals.database.model.PurposeAndRegionApprovalSettingsItem;
import ru.sberbank.ditsib.transport.approvals.database.model.TaxiApprovalsSettings;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.ApprovalsSettingsSender;
import ru.sberbank.ditsib.transport.approvals.services.ApprovalsSettingsService;
import ru.sberbank.ditsib.transport.approvals.services.GeoZoneService;
import ru.sberbank.ditsib.transport.approvals.services.OrganizationService;
import ru.sberbank.ditsib.transport.approvals.services.TripPurposeService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
@Transactional
public class TaxiApprovalsSettingsServiceImpl implements ApprovalsSettingsService<TaxiApprovalsSettings> {
    
    private final TaxiApprovalsSettingsRepository settingsRepository;
    private final OrganizationService organizationService;
    private final TripPurposeService tripPurposeService;
    private final GeoZoneService geoZoneService;
    private final ApprovalsSettingsSender sender;
    
    @Override
    public TaxiApprovalsSettings add(UUID organizationId, TaxiApprovalsSettings newSettings) {
        // убедится, что корп.клиент, все геозоны и цели поездки уже есть в БД, а также что настройки еще не созданы
        Organization organization = organizationService.getOrThrow(organizationId);
        organizationService.getOrThrow(organizationId);
        checkSettingsIsUnique(organizationId);
        // установить геозоны и цели из БД, или бросить исключение
        List<PurposeAndRegionApprovalSettingsItem> settingsItems = newSettings.getPurposeAndRegionItems();
        if (settingsItems != null) {
            settingsItems = geoZoneService.setAllGeoZonesToSettingsItems(settingsItems);
            settingsItems = tripPurposeService.setAllPurposesToSettingsItems(settingsItems);
        }
        //сохранить настройки
        newSettings.setTransportType("TAXI");
        newSettings.setOrganization(organization);
        newSettings = settingsRepository.save(newSettings);
        sender.send(newSettings);
        return newSettings;
    }
    
    @Override
    public TaxiApprovalsSettings get(UUID organizationId) {
        organizationService.getOrThrow(organizationId);
        return getByOrganizationId(organizationId);
    }
    
    @Override
    public void update(UUID organizationId, UUID settingId, TaxiApprovalsSettings updateData) {
        // убедиться, что корп.клиент, все геозоны и цели поездки уже есть в БД, а также что настройки уже созданы
        Organization organization = organizationService.getOrThrow(organizationId);
        getByOrganizationId(organizationId);
        validateSettingExists(settingId);
        // установить геозоны и цели из БД, или бросить исключение
        List<PurposeAndRegionApprovalSettingsItem> settingsItems = updateData.getPurposeAndRegionItems();
        if (settingsItems != null) {
            settingsItems = geoZoneService.setAllGeoZonesToSettingsItems(settingsItems);
            settingsItems = tripPurposeService.setAllPurposesToSettingsItems(settingsItems);
        }
        //сохранить настройки
        updateData.setTransportType("TAXI");
        updateData.setId(settingId);
        updateData.setOrganization(organization);
        updateData = settingsRepository.save(updateData);
        sender.send(updateData);
    }
    
    @Override
    public void delete(UUID organizationId, UUID settingId) {
        organizationService.getOrThrow(organizationId);
        TaxiApprovalsSettings deleted = getByOrganizationId(organizationId);
        if (!deleted.getId().equals(settingId)) {
            throw getNotFoundException(organizationId);
        }
        sender.sendDeleted(deleted);
        settingsRepository.delete(deleted);
    }
    
    @Override
    public TaxiApprovalsSettings restoreValues(UUID organizationId, UUID settingId) {
        organizationService.getOrThrow(organizationId);
        TaxiApprovalsSettings setting = getByOrganizationId(organizationId);
        validateSettingExists(settingId);
        TaxiApprovalsSettings newValue = TaxiApprovalsSettings.builder()
                                                              .id(settingId)
                                                              .organization(setting.getOrganization())
                                                              .transportType("TAXI")
                                                              .build();
        newValue = settingsRepository.save(newValue);
        sender.send(newValue);
        return newValue;
    }
    
    /**
     * Возвращает существуеющую настройку для данной организации
     *
     * @param organizationId id корп.клиента
     *
     * @return настройка, если существует
     */
    private TaxiApprovalsSettings getByOrganizationId(UUID organizationId) {
        return settingsRepository.findByOrganizationId(organizationId)
                                 .orElseGet(TaxiApprovalsSettings::new);
    }
    
    /**
     * Получить исключение
     *
     * @param organizationId id корп.клиента
     *
     * @return EntityNotFoundException
     */
    private EntityNotFoundException getNotFoundException(UUID organizationId) {
        return new EntityNotFoundException(Approval.class, Map.of("oprganizationId", organizationId, "transportType", "TAXI"));
    }
    
    /**
     * Проверяет, существует ли настройка
     *
     * @param settingId
     */
    private void validateSettingExists(UUID settingId) {
        settingsRepository.findById(settingId).orElseThrow(() -> new EntityNotFoundException(
                TaxiApprovalsSettings.class, settingId
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
                    TaxiApprovalsSettings.class,
                    "organizationId",
                    organizationId);
        });
    }
}
