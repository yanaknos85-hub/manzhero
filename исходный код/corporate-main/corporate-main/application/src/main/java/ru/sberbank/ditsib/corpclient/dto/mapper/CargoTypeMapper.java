package ru.sberbank.ditsib.corpclient.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sberbank.ditsib.corpclient.database.model.CargoType;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoTypeDto;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoTypeFileDto;
import ru.sberbank.ditsib.transport.constants.cargo.CargoCategoryEnum;
import ru.sberbank.ditsib.transport.constants.cargo.CargoTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.CargoTypeMessage;

/**
 * Mapper of cargo types.
 */
@Mapper
public interface CargoTypeMapper {

    /**
     * Convert entity to web object.
     *
     * @param source entity.
     * @return web object.
     */
    @Mapping(target = "isUniversal", expression = "java(source.getOrganization() == null)")
    @Mapping(target = "organizationId", source = "organization.id")
    CargoTypeDto entityToDto(CargoType source);

    /**
     * Convert data object for files from entity.
     *
     * @param source entity.
     * @return data object for files.
     */
    @Mapping(target = "category", source = "category.name")
    @Mapping(target = "type", source = "type.name")
    @Mapping(target = "organizationId", source = "organization.id")
    CargoTypeFileDto entityToFileDto(CargoType source);

    /**
     * Convert data object for files to entity.
     *
     * @param source data object for files.
     * @return entity.
     */
    @Mapping(target = "category", source = "category", qualifiedByName = "mapCargoCategory")
    @Mapping(target = "type", source = "type", qualifiedByName = "mapCargoType")
    CargoType fileDtoToEntity(CargoTypeFileDto source);

    /**
     * Convert web object to entity.
     *
     * @param source web object.
     * @return entity.
     */
    CargoType newDtoToEntity(CargoTypeDto source);

    /**
     * Convert entity to message.
     *
     * @param source entity.
     * @return web object.
     */
    CargoTypeMessage toMessage(CargoType source);

    @Named("mapCargoCategory")
    default CargoCategoryEnum mapCargoCategory(String category) {
        return CargoCategoryEnum.getByName(category).orElse(CargoCategoryEnum.REGULAR);
    }

    @Named("mapCargoType")
    default CargoTypeEnum mapCargoType(String type) {
        return CargoTypeEnum.getByName(type).orElse(CargoTypeEnum.OTHER);
    }
}