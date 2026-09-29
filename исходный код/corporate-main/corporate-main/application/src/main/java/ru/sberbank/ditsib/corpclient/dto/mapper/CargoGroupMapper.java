package ru.sberbank.ditsib.corpclient.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sberbank.ditsib.corpclient.database.model.CargoGroup;
import ru.sberbank.ditsib.corpclient.dto.cargo.CagroGroupTypeDto;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoGroupDto;

/**
 * Mapper of cargo groups.
 */
@Mapper
public interface CargoGroupMapper {

    @Mapping(target = "isUniversal", expression = "java(cargoGroup.getCargoType().getOrganization() == null)")
    @Mapping(target = "organizationId", source = "cargoType.organization.id")
    @Mapping(source = "cargoType", target = "...")
    CagroGroupTypeDto entityToDto(CargoGroup cargoGroup);

    CargoGroup newDtoToEntity(CargoGroupDto newData);
}
