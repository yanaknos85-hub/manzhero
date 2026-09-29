package ru.sber.transport.corporate.web.http.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.sber.transport.corporate.business.model.Employee;
import ru.sber.transport.web.model.EmployeeData;

/**
 * Веб-маппер данных сотрудников.
 */
@Mapper
public interface EmployeesWebMapper {

    /**
     * Конвертация бизнес-модели в веб.
     *
     * @param source бизнес-модель.
     * @return веб-модель.
     */
    @Mapping(target = "isDepartmentHead", source = "departmentHead")
    @Mapping(target = "mobilePhone", source = "phone")
    @Mapping(target = "orgStructureType", source = "type")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "isPhoneConfirmed", source = "phoneConfirmed")
    EmployeeData toWeb(Employee source);

}
