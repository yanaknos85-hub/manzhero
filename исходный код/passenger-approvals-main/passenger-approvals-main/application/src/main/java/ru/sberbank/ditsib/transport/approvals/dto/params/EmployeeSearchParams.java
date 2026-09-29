package ru.sberbank.ditsib.transport.approvals.dto.params;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmployeeSearchParams extends PageSortFilterParameters<EmployeeField>{
    
    public EmployeeSearchParams() {
        super(EmployeeField.FULL_NAME);
    }
    
    @Schema(title = "Полное имя", description = "Фильтр сотрудника по полному имени")
    private String fullName;
    
    @Schema(title = "ТН", description = "Фильтр сотрудника по табельному номеру")
    private String personnelNumber;
    
    @Override
    public boolean isFieldsEmpty() {
        return fullName == null && personnelNumber == null;
    }
}
