package ru.sber.transport.limits.providers.departments.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.database.limits.tables.records.DepartmentRecord;
import ru.sber.transport.limits.business.model.Department;

/**
 * Маппер для подразделений базы данных
 */
@Mapper
public interface DepartmentsDatabaseMapper {

    /**
     * Преобразовать подразделение базы данных в бизнес-модель
     *
     * @param source подразделение базы данных
     * @return бизнес-модель подразделения
     */
    @Mapping(target = "humanReadableId", source = "humanreadableid")
    @Mapping(target = "name", source = "departmentName")
    @Mapping(target = "departmentHead", ignore = true)
    Department toBusiness(DepartmentRecord source);

}
