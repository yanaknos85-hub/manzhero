package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sberbank.ditsib.corpclient.database.model.Attribute;
import ru.sberbank.ditsib.corpclient.database.model.AttributeStatus;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Репозиторий признаков сотрудника.
 */
public interface AttributeRepository extends JpaRepository<Attribute, UUID> {
    
    /**
     * Получение признаков со статусом.
     *
     * @param status статус признака.
     * @return список признаков.
     */
    @Query("SELECT attribute FROM Attribute attribute WHERE attribute.status = :status ")
    List<Attribute> findAllByStatus(@NotNull AttributeStatus status);

    /**
     * Получение признаков по названиям.
     *
     * @param nameSet названия.
     * @return признаки.
     */
    @Query("SELECT attribute FROM Attribute attribute WHERE attribute.name in (:nameSet) " +
            " and attribute.status = 'ACTIVE' ")
    List<Attribute> findAllByName(Set<String> nameSet);

    /**
     * Получение признака по названию.
     *
     * @param name название.
     * @return признак.
     */
    @Query("SELECT attribute FROM Attribute attribute WHERE lower(attribute.name) = lower(:name) " +
            " and attribute.status = 'ACTIVE' ")
    Optional<Attribute> findActiveByName(String name);

    /**
     * Получение признака по названию.
     *
     * @param name название.
     * @return признак.
     */
    @Query("SELECT attribute FROM Attribute attribute WHERE lower(attribute.name) = lower(:name)")
    Optional<Attribute> findByName(String name);
}
