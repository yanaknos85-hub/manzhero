package ru.sber.transport.tariff_fleet.dto.service_point;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.sber.transport.tariff_fleet.service.excel.ExcelFieldInfo;

import java.math.BigDecimal;
import java.util.List;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ServicePointExcelDto {
    
    private Integer number;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    
    public static List<ExcelFieldInfo> getExcelFieldsInfo() {
        return List.of(
                new ExcelFieldInfo("Номер по порядку", "number"),
                new ExcelFieldInfo("Адрес точки обслуживания", "address"),
                new ExcelFieldInfo("Широта", "latitude"),
                new ExcelFieldInfo("Долгота", "longitude"));
    }
    
}