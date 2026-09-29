package ru.sberbank.ditsib.transport.limits.dto.analytic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(title = "Тип метрики", description = "Доступные типы метрик")
public enum MetricType {
 
    BUDGET("budget", "Лимит", "rub"),
    BUDGET_PER_TRANSPORT_TYPE("budget_per_transport_type", "Лимит по типу транспорта", "rub");
    
    private final String code;
    private final String typeName;
    private final String typeValue;
    
    public String getCode() {
        return code;
    }
    
    public String getName() {
        return typeName;
    }
    
    public String getTypeValue() {
        return typeValue;
    }
}
