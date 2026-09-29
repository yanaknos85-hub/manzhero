package ru.sber.transport.limits.web.http.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.limits.web.model.Employee;

/**
 * Маппер данных сотрудников
 */
@Mapper
public interface EmployeeWebMapper {

    /**
     * Маппер данных сотрудников
     * @param source источник
     * @return результат
     */
    Employee toWeb(ru.sber.transport.limits.business.model.Employee source);

}
