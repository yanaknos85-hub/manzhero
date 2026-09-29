package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.*;
import ru.sberbank.ditsib.corpclient.dto.AttributeDto;
import ru.sberbank.ditsib.corpclient.database.model.Attribute;

import java.util.Set;

@Mapper
public interface AttributeMapper {

    Attribute stringToAttribute(String name);

    default String dtoToString(AttributeDto attributeDto){
        return attributeDto.getName();
    }
    default String attributeToString(Attribute attribute){
        return attribute.getName();
    }

    @Mapping(target = "status", source = "status",
            defaultExpression = "java(ru.sberbank.ditsib.corpclient.database.model.AttributeStatus.ACTIVE)")
    AttributeDto attributeToDTO(Attribute attribute);

    @Mapping(target = "status", source = "status",
            defaultExpression = "java(ru.sberbank.ditsib.corpclient.database.model.AttributeStatus.ACTIVE)")
    Attribute attributeDTOToattribute(AttributeDto attribute);

    Set<Attribute> attributeDTOSetToAttributeSet(Set<AttributeDto> attributeSet);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    Set<Attribute> stringToAttribute(Set<String> nameSet);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    Set<String> attributeToString(Set<AttributeDto> attributes);

}
