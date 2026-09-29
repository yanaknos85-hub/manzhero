package ru.sberbank.ditsib.transport.limits.dto;

import ru.sberbank.ditsib.transport.limits.dto.analytic.ChartDTO;

import java.util.List;

public record GeneralAnalyticalReportResponseDTO(List<ChartDTO> charts) {}
