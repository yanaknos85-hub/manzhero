package ru.sberbank.ditsib.corpclient.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;
import ru.sberbank.ditsib.corpclient.database.model.Department_;
import ru.sberbank.ditsib.corpclient.database.model.Employee_;
import ru.sberbank.ditsib.corpclient.database.model.ExecutorGroup_;
import ru.sberbank.ditsib.corpclient.database.model.Organization_;
import ru.sberbank.ditsib.corpclient.dto.DepartmentField;
import ru.sberbank.ditsib.corpclient.dto.EmployeeField;
import ru.sberbank.ditsib.corpclient.dto.ExecutorGroupField;
import ru.sberbank.ditsib.corpclient.dto.OrganizationField;
import ru.sberbank.ditsib.request.Direction;

/**
 * Утилиты сортировки.
 */
@UtilityClass
public class SortUtils {
    
    /**
     * Создать объект сортировки сотрудников.
     *
     * @param direction направление сортировки.
     * @param field поле сортировки.
     * @return сортировка.
     */
    public Sort createSort(Direction direction, EmployeeField field) {
        var dir = Sort.Direction.valueOf(direction.name());
        return switch (field) {
            case FULL_NAME -> Sort.by(dir, Employee_.LAST_NAME, Employee_.FIRST_NAME, Employee_.PATRONYMIC);
            case PERSONNEL_NUMBER -> Sort.by(dir, Employee_.PERSONNEL_NUMBER);
            case ID -> Sort.by(dir, Employee_.HUMAN_READABLE_ID);
            case EMAIL -> Sort.by(dir, Employee_.EMAIL);
            case MOBILE -> Sort.by(dir, Employee_.MOBILE_PHONE);
            case STATUS -> Sort.by(dir, Employee_.ACTIVE_STATUS);
        };
    }
    
    /**
     * Создание объекта сортировки подразделений.
     *
     * @param direction направление сортировки.
     * @param field поле сортировки.
     * @return сортировка.
     */
    public Sort createSort(Direction direction, DepartmentField field) {
        var dir = Sort.Direction.valueOf(direction.name());
        return switch (field) {
            case NAME -> Sort.by(dir, Department_.NAME);
            case CODE -> Sort.by(dir, Department_.CODE);
            case ID -> Sort.by(dir, Department_.HUMAN_READABLE_ID);
            case LOCATION -> Sort.by(dir, Department_.LOCATION);
            case STATUS -> Sort.by(dir, Department_.ACTIVE_STATUS);
        };
    }
    
    /**
     * Создание объекта сортировки организаций.
     *
     * @param direction направление сортировки.
     * @param field поле сортировки.
     * @return сортировка.
     */
    public Sort createSort(Direction direction, OrganizationField field) {
        var sort = Sort.Direction.valueOf(direction.name());
        return switch (field) {
            case OFFICIAL_NAME -> Sort.by(sort, Organization_.OFFICIAL_NAME);
            case ADDRESS -> Sort.by(sort, Organization_.ADDRESS);
            case MSRN -> Sort.by(sort, Organization_.MSRN);
            case TID -> Sort.by(sort, Organization_.TID);
            case ORGANIZATION_CODE -> Sort.by(sort, Organization_.ORGANIZATION_CODE);
            case GROUP_ID -> Sort.by(sort, Organization_.ORGANIZATION_GROUP);
        };
    }

    public Sort createSort(Direction direction, ExecutorGroupField field) {
        var sort = Sort.Direction.valueOf(direction.name());
        return switch (field) {
            case EXECUTOR_GROUP_NAME -> Sort.by(sort, ExecutorGroup_.NAME);
            case EXECUTOR_FIO,
                    EXECUTOR_PERSONAL_NUMBER -> Sort.by(sort, ExecutorGroup_.EXECUTORS);
            case EXECUTOR_ORGANIZATION -> Sort.by(sort, ExecutorGroup_.ORGANIZATION_ID);
            case CUSTOMER_ORGANIZATION -> Sort.by(sort, ExecutorGroup_.ORGANIZATIONS);
            case CUSTOMER_DEPARTMENT -> Sort.by(sort, ExecutorGroup_.DEPARTMENTS);
            case SERVICE_TYPE -> Sort.by(sort, ExecutorGroup_.SERVICE);
            case CUSTOMER_GEO_ZONE -> Sort.by(sort, ExecutorGroup_.GEO_ZONES);
            case ID -> Sort.by(sort, ExecutorGroup_.HUMAN_READABLE_ID);
        };
    }
}
