package ru.sberbank.ditsib.corpclient.service;

import org.springframework.lang.NonNull;
import ru.sberbank.ditsib.corpclient.dto.AttributeDto;
import ru.sberbank.ditsib.corpclient.dto.NewAttributeDto;
import ru.sberbank.ditsib.corpclient.database.model.Attribute;

import jakarta.validation.constraints.NotNull;
import java.util.*;

/**
 * Сервис для работы с признаками сотрудников.
 */
public interface AttributeService {
    
    /**
     * Сохранение признаков.
     *
     * @param nameSet признаки для сохранения.
     * @return сохраненные признаки.
     */
    @NotNull
    List<Attribute> saveAll(Set<String> nameSet);
    
    /**
     * Сохранение признаков, синхронизация с существующими.
     *
     * @param attributeSet признаки для сохранения.
     * @return сохраненные признаки.
     */
    @NotNull
    List<Attribute> mergeAll(Collection<Attribute> attributeSet);

    /**
     * Добавление признака.
     *
     * @param newData данные нового признака.
     * @return добавленный признак.
     */
    @NotNull
    AttributeDto add(NewAttributeDto newData);

    /**
     * Изменение признака.
     *
     * @param id идентификатор признака для изменения.
     * @param newData новые данные признака.
     * @return измененный признак.
     */
    @NotNull
    AttributeDto edit(UUID id, AttributeDto newData);

    /**
     * Удаление признака.
     *
     * @param id Идентификатор для удаления.
     */
    @NotNull
    void delete(UUID id);

    /**
     * Получение всех признаков.
     *
     * @return список признаков.
     */
    @NotNull
    Collection<AttributeDto> getAll();
    
    /**
     * Получение активных признаков.
     *
     * @return список признаков.
     */
    @NotNull
    Collection<AttributeDto> getActive();
    
    /**
     * Получение активного признака с названием.
     *
     * @param name название признака.
     * @return список признаков.
     */
    @NonNull
    Optional<Attribute> get(String name);
}
