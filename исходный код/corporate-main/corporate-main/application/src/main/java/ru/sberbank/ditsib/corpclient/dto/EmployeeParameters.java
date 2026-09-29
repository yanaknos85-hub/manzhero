package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

@Setter
@Schema(title = "Параметры запроса сотрудников",
        description = "Параметры запроса данных сотрудников (сортировка, фильтрация, пагинация)")
public class EmployeeParameters extends PageSortFilterParameters<EmployeeField> {
    
    public EmployeeParameters() {
        super(EmployeeField.FULL_NAME);
    }
    
    @Schema(title = "Полное имя", description = "Фильтр сотрудника по полному имени")
    private String fullName;
    
    @Schema(title = "ТН", description = "Фильтр сотрудника по табельному номеру")
    private String personnelNumber;
    
    @Schema(title = "ИД", description = "Фильтр сотрудника по человекочитаемому идентификатору")
    private String humanReadableId;
    
    @Schema(title = "Статус", description = "Фильтр сотрудника по статусу")
    private String status;
    
    @Schema(title = "Телефон", description = "Фильтр сотрудника по телефону")
    private String mobilePhone;
    
    @Schema(title = "Почта", description = "Фильтр сотрудника по почте")
    private String email;
}
