package ru.sberbank.ditsib.corpclient.service;

import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sberbank.ditsib.corpclient.dto.NewPositionDTO;
import ru.sberbank.ditsib.corpclient.dto.PositionDTO;
import ru.sberbank.ditsib.corpclient.dto.PositionSearchDTO;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Position crud and other operations
 */
public interface PositionService {
    
    /**
     * @param source data for creation or update
     *
     * @return createdEntity
     */
    PositionDTO savePosition(@NotNull NewPositionDTO source);
    
    /**
     * Сохранение должности.
     *
     * @param source должность для сохранения.
     * @return сохраненная должность.
     */
    Position save(Position source);
    
    /**
     * @param id identifier of position
     *
     * @return found position
     */
    PositionDTO getPosition(@NotNull UUID id);

    /**
     * Получить должность организации по названию
     *
     * @param organizationId id организации.
     * @param name           название должности.
     * @return found position
     */
    Optional<Position> getPosition(@NotNull UUID organizationId, String name);
    
    /**
     * @param id identifier of position to delete
     */
    void deletePosition(@NotNull UUID id);
    
    /**
     * Восстановить деактиввированную должность
     * @param id идентификатор удаленной должности
     */
    void restorePosition(@NotNull UUID id);
    
    /**
     * @param source data for update
     *
     * @return updated entity
     */
    PositionDTO updatePosition(@NotNull PositionDTO source);
    
    /**
     * Get all positions.
     * @param orgId  id организации
     * @return collection of positions.
     */
    List<PositionDTO> getPositions(@NotNull UUID orgId);
    
    /**
     * Get all positions.
     * @return collection of positions.
     */
    List<Position> getPositions();
    
    /**
     * Провалидировать должность по идентификаторам
     * @param id id должности
     * @param orgId  id организации
     * @return DTO должности
     */
    PositionDTO validatePositionByIdAndOrgId(@NotNull UUID id, @NotNull UUID orgId);
    
    /**
     * Поиск должностей
     * @param positionSearchDTO ipositionSearchDTO
     * @return Список должностей
     */
    List<PositionDTO> search(PositionSearchDTO positionSearchDTO);

    /**
     * Достать должности для организации
     *
     * @param organizationId ID организации.
     *
     * @return список должностей
     */
    List<Position> findAllByOrganizationId(UUID organizationId);
}
