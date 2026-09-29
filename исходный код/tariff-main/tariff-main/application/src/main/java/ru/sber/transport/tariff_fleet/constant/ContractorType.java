package ru.sber.transport.tariff_fleet.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

import static ru.sber.transport.tariff_fleet.constant.ServiceType.*;


@Getter
@AllArgsConstructor
public enum ContractorType {
    
    API("Интеграция ПО контрагента по API",
            "Интеграция ПО контрагента по API",
            Set.of(AUTOSERVICE, EMPLOYEE_TRANSPORTATION, CARGO_TRANSPORTATION)),
    DISPATCHER_INTERNAL("Диспетчерская СберТранспорт (Банк)",
            "Диспетчерская СберТранспорт (ДЗО)",
            Set.of(EMPLOYEE_TRANSPORTATION, CARGO_TRANSPORTATION)),
    DISPATCHER_EXTERNAL("Диспетчерская СберТранспорт (ДЗО)", null,
            Set.of(EMPLOYEE_TRANSPORTATION, CARGO_TRANSPORTATION)),
    AUTOSERVICE_INTERNAL("АвтоСервис (Банк)", "АвтоСервис (ДЗО)",
            Set.of(AUTOSERVICE)),
    AUTOSERVICE_EXTERNAL("АвтоСервис (ДЗО)", null,
            Set.of(AUTOSERVICE)),
    OFFLINE("Без интеграции", "Без интеграции",
            Set.of(AUTOSERVICE));

    private final String internalRusName;
    private final String externalRusName;
    private final Set<ServiceType> services;
}
