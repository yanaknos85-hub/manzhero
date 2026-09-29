package ru.sberbank.ditsib.corpclient.dto.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.sberbank.ditsib.corpclient.database.model.CargoDeliveryTime;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoDeliveryTimeDto;

/**
 * Mapper of delivery time info.
 */
@Mapper
public interface CargoDeliveryTimeMapper {

    /**
     * Convert entity to web object.
     *
     * @param entity source entity.
     * @return web object.
     */
    CargoDeliveryTimeDto entityToDto(CargoDeliveryTime entity);

    /**
     * Convert web object to entity.
     *
     * @param dto web object.
     * @return entity.
     */
    @Mapping(target = "active", ignore = true)
    CargoDeliveryTime dtoToEntity(CargoDeliveryTimeDto dto);
}