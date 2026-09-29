package ru.sberbank.ditsib.corpclient.dto.mapper;

import org.mapstruct.*;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.dto.*;

import java.util.Map;

/**
 * Маппер из сущностей в DTO и обратно для настроек совместных поездок
 */
@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
)
public interface SettingsItemDTOMapper {

    SettingsAttributeDTO attributeToDto(Attribute attribute);

    Attribute dtoToAttribute(SettingsAttributeDTO dto);

    SettingsPositionDTO positionToDto(Position position);

    Position dtoToPosition(SettingsPositionDTO positionDto);
    
    @Mapping(target = "positionId", source = "position.id")
    SettingsEmployeeDTO employeeToDto(Employee employee);
    
    @Mapping(target = "position.id", source = "positionId")
    Employee dtoToEmployee(SettingsEmployeeDTO employeeDto);
    
    SharedRideSettingsItemCreateDTO sharedRideSettingsItemToCreateDto(SharedRideSettingsItem setting);
    SharedRideSettingsItemUpdateDTO sharedRideSettingsItemToUpdateDto(SharedRideSettingsItem setting);
    SharedRideSettingsItem createDtoToSharedRideSettingsItem(SharedRideSettingsItemCreateDTO dto);
    SharedRideSettingsItem updateDtoToSharedRideSettingsItem(SharedRideSettingsItemUpdateDTO dto);
    
    @Named("createdDtoMap")
    @MapMapping(keyTargetType = String.class, valueTargetType = SharedRideSettingsItemCreateDTO.class)
    Map<String, SharedRideSettingsItemCreateDTO> settingsMapToCreateDtoMap(
            Map<SharedRideSettingType, SharedRideSettingsItem> settingsMap
    );
    
    @Named("updatedDtoMap")
    @MapMapping(keyTargetType = String.class, valueTargetType = SharedRideSettingsItemUpdateDTO.class)
    Map<String, SharedRideSettingsItemUpdateDTO> settingsMapToUpdateDtoMap(
            Map<SharedRideSettingType, SharedRideSettingsItem> settingsMap
    );
    
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "settings", source = "settings", qualifiedByName = "createdDtoMap")
    SharedRideSettingsCreateDTO sharedRideSettingsToCreateDto(SharedRideSettings setting);
    
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "settings", source = "settings", qualifiedByName = "updatedDtoMap")
    SharedRideSettingsUpdateDTO sharedRideSettingsToUpdateDto(SharedRideSettings setting);
    
    @Named("createdMap")
    @MapMapping(keyTargetType = SharedRideSettingType.class, valueTargetType = SharedRideSettingsItem.class)
    Map<SharedRideSettingType, SharedRideSettingsItem> createDtoMapToSettingsMap(
            Map<String, SharedRideSettingsItemCreateDTO> createDtoMap
    );
    
    @Named("updatedMap")
    @MapMapping(keyTargetType = SharedRideSettingType.class, valueTargetType = SharedRideSettingsItem.class)
    Map<SharedRideSettingType, SharedRideSettingsItem> updateDtoMapToSettingsMap(
            Map<String, SharedRideSettingsItemUpdateDTO> updateDtoMap
    );
    
    @Mapping(target = "organization.id", source = "organizationId")
    @Mapping(target = "settings", source = "settings", qualifiedByName = "createdMap")
    SharedRideSettings createDtoToSharedRideSettings(SharedRideSettingsCreateDTO dto);
    
    @Mapping(target = "organization.id", source = "organizationId")
    @Mapping(target = "settings", source = "settings", qualifiedByName = "updatedMap")
    @Mapping(target = "id", ignore = true)
    SharedRideSettings updateDtoToSharedRideSettings(
            SharedRideSettingsUpdateDTO dto,
            @MappingTarget SharedRideSettings settingsFromDb
    );
}
