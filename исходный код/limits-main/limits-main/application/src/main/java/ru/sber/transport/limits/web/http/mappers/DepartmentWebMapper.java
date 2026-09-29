package ru.sber.transport.limits.web.http.mappers;

import org.mapstruct.Mapper;
import ru.sber.transport.limits.web.model.Department;

/**
 * Маппер данных подразделений
 */
@Mapper
public interface DepartmentWebMapper {

    /**
     * Маппер данных подразделений
     * @param source источник
     * @return результат
     */
    Department toWeb(ru.sber.transport.limits.business.model.Department source);

}
