package ru.sber.transport.tariff_fleet.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sber.transport.tariff_fleet.database.model.EdfOperator;

import java.util.Set;

/**
 * Repository of edf operator
 */
@Repository
public interface EdfOperatorRepository extends JpaRepository<EdfOperator, String> {
    
    /**
     * Находим всех активных операторов ЭДО
     *
     * @return {@link Set<EdfOperator>}
     */
    Set<EdfOperator> findAllByActiveIsTrue();
}
