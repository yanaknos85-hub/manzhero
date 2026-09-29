package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

@Setter
@Schema(title = "Параметры запроса подразделений",
        description = "Параметры запроса данных подразделений (сортировка, фильтрация, пагинация)")
public class DepartmentParameters extends PageSortFilterParameters<DepartmentField> {
    
    public DepartmentParameters() {
        super(DepartmentField.NAME);
    }
    
    @Schema(title = "Название", description = "Фильтр подразделения по названию")
    private String departmentName;
    
    @Schema(title = "Код", description = "Фильтр подразделения по коду")
    private String code;
    
    @Schema(title = "Местоположение", description = "Фильтр подразделения по местоположению")
    private String location;
    
    @Schema(title = "ИД", description = "Фильтр подразделения по человекочитаемому идентификатору")
    private String humanReadableId;
    
    @Schema(title = "Статус", description = "Фильтр подразделения по статусу")
    private String status;
}
