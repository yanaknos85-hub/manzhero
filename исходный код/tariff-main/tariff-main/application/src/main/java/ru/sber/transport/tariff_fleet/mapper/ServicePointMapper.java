package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.Mapper;
import ru.sber.transport.tariff_fleet.dto.service_point.ServicePointExcelDto;
import ru.sber.transport.tariff_fleet.dto.service_point.ValidatedServicePointDto;

/**
 * Маппер по точкам обслуживания
 */
@Mapper(componentModel = "spring")
public interface ServicePointMapper {

    ValidatedServicePointDto servicePointExcelDtoToValidatedServicePointDto(ServicePointExcelDto source, String error);

}
