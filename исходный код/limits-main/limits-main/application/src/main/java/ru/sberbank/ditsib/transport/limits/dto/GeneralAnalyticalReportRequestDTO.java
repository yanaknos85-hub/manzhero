package ru.sberbank.ditsib.transport.limits.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.transport.limits.dto.serde.OrganizationIdDeserializer;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(title = "Параметры общего аналитического отчета", description = "Параметры и значения фильтров общего аналитического отчета")
public class GeneralAnalyticalReportRequestDTO {

    @JsonDeserialize(using = OrganizationIdDeserializer.class)
    @Schema(description = "Организация")
    List<UUID> organizationId;
    
    @Schema(description = "Год отчета")
    Integer year;
    
    @Schema(description = "Месяц отчета")
    List<Integer> monthList;
    
    @Schema(description = "Виды транспорта")
    List<String> transportTypes;
}
