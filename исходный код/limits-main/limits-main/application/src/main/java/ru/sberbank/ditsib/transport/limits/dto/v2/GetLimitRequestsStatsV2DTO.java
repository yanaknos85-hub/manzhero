package ru.sberbank.ditsib.transport.limits.dto.v2;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

/**
 * Дто количества заявок на согласование по типу транспорта.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetLimitRequestsStatsV2DTO {

    /**
     * Тип транспорта.
     */
    private TransportTypeEnum transportType;

    /**
     * Количество заявок.
     */
    private Long count;
}
