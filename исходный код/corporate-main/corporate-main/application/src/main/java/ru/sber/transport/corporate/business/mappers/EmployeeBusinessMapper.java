package ru.sber.transport.corporate.business.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.sber.transport.corporate.business.model.Employee;

/**
 * Бизнес-маппер данных.
 */
@Mapper
public interface EmployeeBusinessMapper {

    /**
     * Обновление данных.
     *
     * @param target цель обновления.
     * @param source исходные данные.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "humanReadableId", ignore = true)
    void update(@MappingTarget Employee target, Employee source);
}
