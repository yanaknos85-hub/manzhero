package ru.sberbank.ditsib.transport.limits.dto.analytic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(title = "Параметры общего аналитического отчета", description = "Параметры и значения фильтров общего аналитического отчета")
public class GeneralAnalyticalReportRequestDTO {
    
    @Schema(description = "Организация")
    UUID organizationId;
    
    @Schema(description = "Год отчета")
    Integer year;
    
    @Schema(description = "Месяц отчета")
    Integer month;
    
    @Schema(description = "Виды транспорта")
    List<String> transportTypes;
}
