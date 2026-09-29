package ru.sber.transport.tariff_fleet.service;

import ru.sber.transport.tariff_fleet.database.model.EdfOperator;
import ru.sber.transport.tariff_fleet.dto.EdfOperatorDto;

import java.util.Optional;
import java.util.Set;

/**
 * Сервис по работе с операторомами ЭДО
 */
public interface EdfOperatorService {
    
    /**
     * Получение оператора ЭДО по идентификатору
     *
     * @param id Идентификатор записи об операторе ЭДО
     *
     * @return {@link EdfOperator}
     */
    Optional<EdfOperator> getById(String id);
    
    /**
     * Получение набора активных операторов ЭДО
     *
     * @return {@link Set<EdfOperatorDto>}
     */
    Set<EdfOperatorDto> getAllActive();
}
