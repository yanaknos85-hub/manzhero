package ru.sber.transport.limits.providers.employees.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.database.limits.tables.records.EmployeeRecord;
import ru.sber.transport.limits.business.model.Employee;

/**
 * Маппер для данных сотрудников
 */
@Mapper
public interface EmployeeDatabaseMapper {

    /**
     * Маппер для данных сотрудников
     *
     * @param source исходные данные
     * @return целевые данные
     */
    @Mapping(target = "humanReadableId", source = "humanreadableid")
    Employee toBusiness(EmployeeRecord source);

    /**
     * Маппер для данных сотрудников
     *
     * @param source исходные данные
     * @return целевые данные
     */
    @Mapping(target = "humanreadableid", source = "humanReadableId")
    EmployeeRecord toDatabase(Employee source);

}
