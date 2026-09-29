package ru.sberbank.ditsib.corpclient.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

@Getter
@Setter
@Schema(description = "Данные для посика групп организаций")
public class OrganizationGroupSearchDTO extends PageSortFilterParameters<OrganizationGroupSearchParameters> {

    public OrganizationGroupSearchDTO() {
        super(OrganizationGroupSearchParameters.NAME);
    }

    @Schema(description = "Наименование группы организаций")
    private String name;

    @Schema(description = "Принадлежность к внутренней группе компаний")
    private Boolean internal;
}
