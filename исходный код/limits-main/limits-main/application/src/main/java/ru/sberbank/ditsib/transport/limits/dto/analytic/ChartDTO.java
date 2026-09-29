package ru.sberbank.ditsib.transport.limits.dto.analytic;

import java.util.List;

public record ChartDTO(
    String type,
    List<MetricDTO> data,
    List<MetricDTO> dataPerTransportType
) {
}
