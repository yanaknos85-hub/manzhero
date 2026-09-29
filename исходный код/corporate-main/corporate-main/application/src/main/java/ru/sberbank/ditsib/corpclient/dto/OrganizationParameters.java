package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

import java.util.UUID;

@Setter
@Schema(title = "Параметры запроса огранизации",
        description = "Параметры запроса данных организаций (сортировка, фильтрация, пагинация)")
public class OrganizationParameters extends PageSortFilterParameters<OrganizationField> {
    
    public OrganizationParameters() {
        super(OrganizationField.OFFICIAL_NAME);
    }
    
    @Schema(title = "Официальное название", description = "Фильтр организации по официальному названию")
    private String officialName;
    
    @Schema(title = "Адрес", description = "Фильтр организации по адресу")
    private String address;
    
    @Schema(title = "ОГРН", description = "Фильтр организации по ОГРН")
    private String msrn;
    
    @Schema(title = "ИНН", description = "Фильтр организации по ИНН")
    private String tid;

    @Schema(title = "Группа организаций", description = "Фильтр организации по группе организаций")
    private UUID groupId;
}
