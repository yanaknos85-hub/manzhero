package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.corpclient.database.dao.SharedRideSettingsItemRepository;
import ru.sberbank.ditsib.corpclient.database.dao.SharedRideSettingsRepository;
import ru.sberbank.ditsib.corpclient.dto.SharedRideSettingsCreateDTO;
import ru.sberbank.ditsib.corpclient.dto.SharedRideSettingsUpdateDTO;
import ru.sberbank.ditsib.corpclient.dto.mapper.SettingsItemDTOMapper;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.service.SharedRideSettingService;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class SharedRideSettingServiceImpl implements SharedRideSettingService {
    
    private final SharedRideSettingsRepository settingsRepository;
    private final SharedRideSettingsItemRepository itemRepository;
    private final SettingsItemDTOMapper mapper;
    
    @Override
    public List<SharedRideSettingsUpdateDTO> getAllSettingsByOrganizationId(UUID organizationId) {
        return settingsRepository.findByOrganizationId(organizationId)
                                 .stream()
                                 .map(mapper::sharedRideSettingsToUpdateDto)
                                 .toList();
    }
    
    @Override
    public SharedRideSettingsUpdateDTO getSingleSettingsByOrganizationIdAndSettingId(
            UUID organizationId, UUID settingsId) {
        return mapper.sharedRideSettingsToUpdateDto(
                checkSettingsIdAndReturnFoundedSettings(organizationId, settingsId)
        );
    }
   
    @Override
    public SharedRideSettingsUpdateDTO saveNewSettings(
            UUID organizationId,
            SharedRideSettingsCreateDTO newSettingsDto
        ) {
        newSettingsDto.setOrganizationId(organizationId);
        checkOrganizationIdAndTransportTypeKeyIsUnique(organizationId, newSettingsDto.getTransportType());
        var newSettings= mapper.createDtoToSharedRideSettings(newSettingsDto);
        checkAndCompleteAllTypesOfSettings(newSettings);
        resolveConflicts(newSettings);
        SharedRideSettings savedSettings = settingsRepository.save(newSettings);
        
        return mapper.sharedRideSettingsToUpdateDto(savedSettings);
    }
    
    @Override
    public SharedRideSettingsUpdateDTO updateSettings(
            UUID organizationId,
            SharedRideSettingsUpdateDTO updateSettingsDto
    ) {
        updateSettingsDto.setOrganizationId(organizationId);
        checkAllSettingsItemsId(updateSettingsDto);
        SharedRideSettings settingsFromDb =
                checkSettingsIdAndReturnFoundedSettings(organizationId, updateSettingsDto.getId());
        //если было изменение типа транспорта, проверить уникальность ключа
        if (!settingsFromDb.getTransportType().name().equals(updateSettingsDto.getTransportType())) {
            checkOrganizationIdAndTransportTypeKeyIsUnique(organizationId,updateSettingsDto.getTransportType());
        }
        var updatedSettings = mapper.updateDtoToSharedRideSettings(updateSettingsDto, settingsFromDb);
        resolveConflicts(updatedSettings);
        settingsRepository.save(updatedSettings);
    
        return mapper.sharedRideSettingsToUpdateDto(updatedSettings);
    }
    
    @Override
    public void deleteSettings(UUID organizationId, UUID settingsId) {
        SharedRideSettings settingsFromDb = checkSettingsIdAndReturnFoundedSettings(organizationId, settingsId);
        settingsRepository.delete(settingsFromDb);
    }
    
    /**
     * Найти подмножество пересечений двух множеств одного типа
     * @param set1 первое множество
     * @param set2 второе множество
     * @param <T> общий тип
     * @return подмножество пересечений
     */
    static <T> Set<T> getCommonElementsForSets(Set<T> set1, Set<T> set2) {
        Set<T> commonElements = new HashSet<>();
        set1.forEach(el -> {
            if (set2.contains(el)) commonElements.add(el);
        });
        return commonElements;
    }
    
    /**
     * Разрешение конфликтов для сущностей c одинаковым приоритетом.<br>
     * Приоритеты при разрешении конфликта (по убыванию): <i>сотрудник - признак - должность</i><br>
     * Конфликт: если в настройках "Может ездить только индивидуально" и "Индивидуальная поездка недоступна"
     * одни и те же сотрудники, признаки или должности (одинаковый приоритет) - убрать эти сущности из <b>обеих
     * настроек</b>
     * @param settings настройки совместной поездки
     */
    static void resolveConflicts(SharedRideSettings settings) {
        SharedRideSettingsItem individualRideIsNotAvailableSetting =
                settings.getSettings().get(SharedRideSettingType.INDIVIDUAL_RIDE_IS_NOT_AVAILABLE);
        SharedRideSettingsItem canRideIndividualOnlySetting =
                settings.getSettings().get(SharedRideSettingType.CAN_RIDE_INDIVIDUALLY_ONLY);
        //проверить, присутствуют ли обе настройки
        if (individualRideIsNotAvailableSetting == null || canRideIndividualOnlySetting == null) return;
        
        //получить список конфликтующих сотрудников и удалить конфликты из обеих настроек
        Set<Employee> individualRideIsNotAvailableEmployees = individualRideIsNotAvailableSetting.getEmployees();
        Set<Employee> canRideIndividualOnlyEmployees = canRideIndividualOnlySetting.getEmployees();
        if (individualRideIsNotAvailableEmployees == null || canRideIndividualOnlyEmployees == null) return;
        Set<Employee> commonEmployees = getCommonElementsForSets(
                individualRideIsNotAvailableEmployees,
                canRideIndividualOnlyEmployees
        );
        individualRideIsNotAvailableEmployees.removeAll(commonEmployees);
        canRideIndividualOnlyEmployees.removeAll(commonEmployees);
        
        //получить список конфликтующих признаков и удалить конфликты из обеих настроек
        Set<Attribute> individualRideIsNotAvailableAttributes = individualRideIsNotAvailableSetting.getAttributes();
        Set<Attribute> canRideIndividualOnlyAttributes = canRideIndividualOnlySetting.getAttributes();
        if (individualRideIsNotAvailableAttributes == null || canRideIndividualOnlyAttributes == null) return;
        Set<Attribute> commonAttributes = getCommonElementsForSets(
                individualRideIsNotAvailableAttributes,
                canRideIndividualOnlyAttributes
        );
        individualRideIsNotAvailableAttributes.removeAll(commonAttributes);
        canRideIndividualOnlyAttributes.removeAll(commonAttributes);
    
        //получить список конфликтующих должностей и удалить конфликты из обеих настроек
        Set<Position> individualRideIsNotAvailablePositions = individualRideIsNotAvailableSetting.getPositions();
        Set<Position> canRideIndividualOnlyPositions = canRideIndividualOnlySetting.getPositions();
        if (individualRideIsNotAvailablePositions == null || canRideIndividualOnlyPositions == null) return;
        Set<Position> commonPositions = getCommonElementsForSets(
                individualRideIsNotAvailablePositions,
                canRideIndividualOnlyPositions
        );
        individualRideIsNotAvailablePositions.removeAll(commonPositions);
        canRideIndividualOnlyPositions.removeAll(commonPositions);
    }
    
    /**
     * Проверка уникальности ключа "ID организации - Тип транспорта" в БД
     * @param organizationId идентификатор организацц
     * @param transportType тип транспорта (строка)
     */
    private void checkOrganizationIdAndTransportTypeKeyIsUnique(UUID organizationId, String transportType) {
        var transportTypeEnum =
                TransportTypeEnum.getByName(transportType).orElseThrow(() -> new EntityNotFoundException(
                        TransportTypeEnum.class,
                        transportType
                ));
    
        Optional<SharedRideSettings> potentialSettingsFromDb =
                settingsRepository.findByOrganizationIdAndTransportType(
                        organizationId,
                        transportTypeEnum
                );
                        
        if (potentialSettingsFromDb.isPresent()) {
            SharedRideSettings settingsFromDb = potentialSettingsFromDb.get();
            throw new DuplicateDataException(
                    SharedRideSettings.class,
                    Map.of("organizationId", settingsFromDb.getOrganization().getId(), "transportType", settingsFromDb.getTransportType().name())
            );
        }
    }
    
    /**
     * Проверить, есть ли в БД настройка совместных поездок по идентификаторам настройки и организации
     * @param organizationId идентификатор организации
     * @param settingsId идентификатор настройки совместных поездок
     * @return найденную настройку
     */
    private SharedRideSettings checkSettingsIdAndReturnFoundedSettings(UUID organizationId, UUID settingsId){
        return settingsRepository.findByOrganizationIdAndId(organizationId, settingsId)
                                 .orElseThrow(() -> new EntityNotFoundException(
                                         SharedRideSettings.class,
                                         settingsId
                                 ));
    }
    
    /**
     * Проверить, есть ли в БД элементы настройки совместных поездок по DTO
     * @param updateSettingsDto DTO для изменения настройки совместных поездок
     */
    private void checkAllSettingsItemsId(SharedRideSettingsUpdateDTO updateSettingsDto){
        updateSettingsDto.getSettings()
            .forEach((key, value) -> checkSettingsItemIdWithSettingType(value.getId()));
    }
    
    /**
     * Проверить, есть ли в БД элемент настройки совместных поездок по указанному идентификатору элемента
     * @param itemId идентификатор элемента настройки
     */
    private void checkSettingsItemIdWithSettingType(
            UUID itemId
    ) {
        if (itemRepository.findById(itemId).isEmpty())
            throw new EntityNotFoundException(
                    SharedRideSettings.class,
                    itemId
            );
    }
    
    /**
     * Проверка, если нет какого-то из типов настроек, то дописать
     * @param newSettings новые настройки
     */
    private void checkAndCompleteAllTypesOfSettings(SharedRideSettings newSettings) {
        Map<SharedRideSettingType, SharedRideSettingsItem> settingsItems = newSettings.getSettings();
        for (SharedRideSettingType type : SharedRideSettingType.values()) {
            settingsItems.putIfAbsent(type, new SharedRideSettingsItem());
        }
    }
}
