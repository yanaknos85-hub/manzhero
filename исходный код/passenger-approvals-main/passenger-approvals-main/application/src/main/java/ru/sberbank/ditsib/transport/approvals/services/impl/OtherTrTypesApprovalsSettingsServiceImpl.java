package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sberbank.ditsib.transport.approvals.database.dao.OtherTrTypesApprovalsSettingsRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.Organization;
import ru.sberbank.ditsib.transport.approvals.database.model.OtherTrTypesApprovalsSettings;
import ru.sberbank.ditsib.transport.approvals.database.model.PurposeAndRegionApprovalSettingsItem;
import ru.sberbank.ditsib.transport.approvals.messaging.senders.ApprovalsSettingsSender;
import ru.sberbank.ditsib.transport.approvals.services.GeoZoneService;
import ru.sberbank.ditsib.transport.approvals.services.OrganizationService;
import ru.sberbank.ditsib.transport.approvals.services.OtherTrTypesApprovalsSettingsService;
import ru.sberbank.ditsib.transport.approvals.services.TripPurposeService;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.exceptions.WrongTransportTypeException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class OtherTrTypesApprovalsSettingsServiceImpl implements OtherTrTypesApprovalsSettingsService {
    
    private final OtherTrTypesApprovalsSettingsRepository settingsRepository;
    private final OrganizationService organizationService;
    private final TripPurposeService tripPurposeService;
    private final GeoZoneService geoZoneService;
    private final ApprovalsSettingsSender sender;
    
    @Override
    public OtherTrTypesApprovalsSettings add(
            UUID organizationId,
            OtherTrTypesApprovalsSettings newSettings) {
        // убедиться, что корп.клиент и цели поездки уже есть в БД, а также что настройки еще не созданы
        checkTransportType(newSettings.getTransportType());
        Organization organization = organizationService.getOrThrow(organizationId);
        checkSettingsIsUnique(organizationId, newSettings.getTransportType());
        // установить геозоны и цели из БД, или бросить исключение
        List<PurposeAndRegionApprovalSettingsItem> settingsItems = newSettings.getPurposeAndRegionItems();
        if (settingsItems != null) {
            settingsItems = geoZoneService.setAllGeoZonesToSettingsItems(settingsItems);
            settingsItems = tripPurposeService.setAllPurposesToSettingsItems(settingsItems);
        }
        newSettings.setOrganization(organization);
        newSettings = settingsRepository.save(newSettings);
        sender.send(newSettings);
        return newSettings;
    }
    
    @Override
    public OtherTrTypesApprovalsSettings get(UUID organizationId, String transportType) {
        checkTransportType(transportType);
        organizationService.getOrThrow(organizationId);
        return getByOrganizationIdAndTransportType(organizationId, transportType);
    }
    
    @Override
    public void update(UUID organizationId, String transportType, OtherTrTypesApprovalsSettings updateData) {
        checkTransportType(transportType);
        Organization organization = organizationService.getOrThrow(organizationId);
        OtherTrTypesApprovalsSettings savedSettings = getByOrganizationIdAndTransportType(organizationId, transportType);
        // установить геозоны и цели из БД, или бросить исключение
        List<PurposeAndRegionApprovalSettingsItem> settingsItems = updateData.getPurposeAndRegionItems();
        if (settingsItems != null) {
            settingsItems = geoZoneService.setAllGeoZonesToSettingsItems(settingsItems);
            settingsItems = tripPurposeService.setAllPurposesToSettingsItems(settingsItems);
        }
        updateData.setOrganization(organization);
        updateData.setId(savedSettings.getId());
        updateData = settingsRepository.save(updateData);
        sender.send(updateData);
    }
    
    @Override
    public void delete(UUID organizationId, String transportType) {
        organizationService.getOrThrow(organizationId);
        OtherTrTypesApprovalsSettings deleted = getByOrganizationIdAndTransportType(organizationId, transportType);
        sender.sendDeleted(deleted);
        settingsRepository.delete(deleted);
    }
    
    @Override
    public OtherTrTypesApprovalsSettings restoreValues(UUID organizationId, String transportType) {
        organizationService.getOrThrow(organizationId);
        OtherTrTypesApprovalsSettings setting = getByOrganizationIdAndTransportType(organizationId, transportType);
        OtherTrTypesApprovalsSettings newValue = OtherTrTypesApprovalsSettings.builder()
                                                                              .id(setting.getId())
                                                                              .organization(setting.getOrganization())
                                                                              .transportType(setting.getTransportType())
                                                                              .build();
        newValue = settingsRepository.save(newValue);
        sender.send(newValue);
        return newValue;
    }
    
    /**
     * Проверить тип транспорта (для такси и общественного - свои контроллеры)
     *
     * @param transportType тип транспорта
     */
    private void checkTransportType(String transportType) {
        if (transportType.equals("TAXI") ||
            transportType.equals("PUBLIC")) {
            throw new WrongTransportTypeException(TransportTypeEnum.valueOf(transportType),
                                                  Arrays.asList(
                                                          TransportTypeEnum.PERSONAL,
                                                          TransportTypeEnum.CARSHARING,
                                                          TransportTypeEnum.BICYCLE,
                                                          TransportTypeEnum.WALK,
                                                          TransportTypeEnum.SCOOTER));
        }
    }

    /**
     * Возвращает существуеющую настройку для данной организации
     * @param organizationId id корп.клиента
     * @param transportType тип транспорта
     * @return настройка, если существует
     */
    private OtherTrTypesApprovalsSettings getByOrganizationIdAndTransportType(UUID organizationId, String transportType) {
        var list = settingsRepository.findAllByOrganizationId(organizationId)
                                     .stream().filter(setting -> setting.getTransportType().equals(transportType))
                                     .toList();
        if (list.size() > 1) {
            throw new DuplicateDataException(
                    OtherTrTypesApprovalsSettings.class,
                    Map.of("transportType",
                    transportType, "organizationId", organizationId));
        }
        if (list.isEmpty()) {
            return OtherTrTypesApprovalsSettings.builder().transportType(transportType)
                                                .organization(organizationService.getOrThrow(organizationId)).build();
        }
        return list.get(0);
    }
    
    
    
    /**
     * Проверить, что настроек для данной организации еще нет в БД
     *
     * @param organizationId идентификатор организации
     */
    private void checkSettingsIsUnique(UUID organizationId, String transportType) {
        if (settingsRepository.findAllByOrganizationId(organizationId).stream()
                              .anyMatch(setting -> setting.getTransportType().equals(transportType))) {
            throw new DuplicateDataException(
                    OtherTrTypesApprovalsSettings.class,
                    Map.of("transportType",
                            transportType, "organizationId", organizationId));
        }
    }
}
