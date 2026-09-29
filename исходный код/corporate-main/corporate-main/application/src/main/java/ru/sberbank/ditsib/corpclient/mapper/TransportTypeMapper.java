package ru.sberbank.ditsib.corpclient.mapper;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface TransportTypeMapper {

    default List<String> getTypes() {
        return List.of(
                "TAXI",
                "PERSONAL",
                "PUBLIC",
                "CARSHARING",
                "GROUP_TRANSFER",
                "BICYCLE",
                "WALK",
                "SCOOTER",
                "DEDICATED",
                "COURIER",
                "DOMESTIC_COURIER",
                "INTERREGIONAL",
                "INDIVIDUAL",
                "OFFICIAL",
                "PRIVATE",
                "SPECIAL",
                "YANDEX"
        );
    }

    default String getRusName(String source) {
        return switch (source) {
            case "TAXI" -> "Такси";
            case "PERSONAL" -> "Личный транспорт";
            case "PUBLIC" -> "Общественный транспорт";
            case "CARSHARING" -> "Каршеринг";
            case "GROUP_TRANSFER" -> "Трансфер";
            case "BICYCLE" -> "Велосипед";
            case "WALK" -> "Пешком";
            case "SCOOTER" -> "Самокат";
            case "DEDICATED" -> "Доставка сборного груза";
            case "COURIER" -> "Курьерская доставка";
            case "DOMESTIC_COURIER" -> "Внутренний курьер";
            case "INTERREGIONAL" -> "Межрегиональная доставка";
            case "INDIVIDUAL" -> "Доставка выделенным транспортом";
            case "PRIVATE" -> "Личный";
            case "SPECIAL" -> "Специальный";
            case "YANDEX" -> "Яндекс GO";
            case "OFFICIAL" -> "Служебный";
            default -> throw new IllegalArgumentException("Unknown transport type %s".formatted(source));
        };
    }

    default String getServiceType(String source) {
        return switch (source) {
            case "TAXI", "PERSONAL", "PUBLIC", "CARSHARING", "GROUP_TRANSFER", "BICYCLE", "WALK", "SCOOTER" -> "EMPLOYEE_TRANSPORTATION";
            case "DEDICATED", "COURIER", "DOMESTIC_COURIER", "INTERREGIONAL", "INDIVIDUAL" -> "CARGO_TRANSPORTATION";
            case "PRIVATE", "SPECIAL", "OFFICIAL" -> "REPAIR";
            case "YANDEX" -> "EXTERNAL";
            default -> throw new IllegalArgumentException("Unknown transport type %s".formatted(source));
        };
    }
}
