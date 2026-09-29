package ru.sberbank.ditsib.transport.approvals.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.sberbank.ditsib.transport.approvals.database.model.Approval_;
import ru.sberbank.ditsib.transport.approvals.database.model.Employee_;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeField;
import ru.sberbank.ditsib.transport.approvals.dto.params.EmployeeSearchParams;

@UtilityClass
public class PageableUtils {
    
    public static Pageable addPassengerParamsToPageable(Pageable pageable, EmployeeSearchParams employeeSearchParams) {
        if (employeeSearchParams == null || employeeSearchParams.isFieldsEmpty()) {
            return pageable;
        }
        Sort existedSort = pageable.getSort();
    
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), createSort(existedSort, employeeSearchParams.getDirection(),
                                                                                           employeeSearchParams.getField()));
    }
    
    public static Sort createSort(Sort existedSort, Sort.Direction direction, EmployeeField field) {
        Sort newSort = switch (field) {
            case FULL_NAME -> Sort.by(direction, Approval_.PASSENGER + "." + Employee_.LAST_NAME,
                                      Approval_.PASSENGER + "." + Employee_.FIRST_NAME,
                                      Approval_.PASSENGER + "." + Employee_.PATRONYMIC);
            case PERSONNEL_NUMBER -> Sort.by(direction, Approval_.PASSENGER + "." + Employee_.PERSONNEL_NUMBER);
            case ID -> Sort.by(direction, Approval_.PASSENGER + "." + Employee_.HUMAN_READABLE_ID);
        };
        if (existedSort == null) {
            return newSort;
        }
        return existedSort.and(newSort);
    }
}
