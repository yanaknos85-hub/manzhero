package ru.sberbank.ditsib.corpclient.dto.mapper;

import org.mapstruct.*;
import ru.sberbank.ditsib.corpclient.dto.purpose.*;
import ru.sberbank.ditsib.corpclient.database.model.*;

import java.util.List;
import java.util.UUID;

@Mapper
public interface PurposeMapper {

    @Mapping(source = "organization.id", target = "organization")
    TripPurposeDTO tripPurposeToDTO(TripPurpose tripPurpose);

    @Mapping(source = "organization", target = "organization.id")
    TripPurpose newDTOToTripPurpose(NewTripPurposeDTO tripPurposeDTO);

    @Mapping(source = "organizationId", target = "organization.id")
    TripPurpose newDTOToTripPurpose(NewTripPurposeDTO tripPurposeDTO, UUID organizationId);
    
    default TripPurposeAttribute idContainerDTOToTripPurposeAttribute(IdContainerDTO idContainerDTO) {
        if (idContainerDTO == null) {
            return null;
        }
        return TripPurposeAttribute.builder().attribute(
                Attribute.builder().id(idContainerDTO.getId()).build()
        ).build();
    }
    default TripPurposeDepartment idContainerDTOToTripPurposeDepartment(IdContainerDTO idContainerDTO) {
        if (idContainerDTO == null) {
            return null;
        }
        return TripPurposeDepartment.builder().department(
                Department.builder().id(idContainerDTO.getId()).build()
        ).build();
    }

    TripPurposeAttributeDTO tripPurposeAttributeToDTO(TripPurposeAttribute tripPurposeAttribute);
    TripPurposeAttribute dtoToTripPurposeAttribute(TripPurposeAttributeDTO attributeDto);

    TripPurposeDepartmentDTO tripPurposeDepartmentToDTO(TripPurposeDepartment tripPurposeDepartment);
    TripPurposeDepartment dtoToTripPurposeDepartment(TripPurposeDepartmentDTO tripPurposeDepartment);

    TripPurposeDateDTO tripPurposeDateToDTO(TripPurposeDate tripPurposeDate);
    TripPurposeDate dtoToTripPurposeDate(TripPurposeDateDTO tripPurposeDateDTO);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<TripPurposeDate> dtoToTripPurposeDateList(List<TripPurposeDateDTO> tripPurposeDateList);
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<TripPurposeDateDTO> tripPurposeDateToDTOList(List<TripPurposeDate> tripPurposeDateList);

    TripPurposeTimeRangeDTO tripPurposeTimeToDTO(TripPurposeTime tripPurposeTime);
    TripPurposeTime dtoToTripPurposeTime(TripPurposeTimeRangeDTO tripPurposeTimeRangeDTO);

    TripPurposeWeekdayDTO tripPurposeWeekdayToDTO(TripPurposeWeekday tripPurposeWeekday);
    TripPurposeWeekday dtoToTripPurposeWeekday(TripPurposeWeekdayDTO tripPurposeWeekdayDTO);

}