package ru.sber.transport.tariff_fleet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.tariff_fleet.database.model.EdfOperator;
import ru.sber.transport.tariff_fleet.dto.EdfOperatorDto;

import java.util.Set;

/**
 * Маппер оператора ЭДО
 */
@Mapper(componentModel = "spring")
public interface EdfOperatorMapper {
    
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    EdfOperatorDto edfOperatorToEdfOperatorDto(EdfOperator source);
    
    Set<EdfOperatorDto> setEdfOperatorToSetEdfOperatorDto(Set<EdfOperator> source);
}
