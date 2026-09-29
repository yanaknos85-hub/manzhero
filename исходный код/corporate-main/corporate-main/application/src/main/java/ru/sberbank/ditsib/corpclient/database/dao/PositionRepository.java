package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sberbank.ditsib.corpclient.database.model.Position_;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Position repository
 */
@Repository
public interface PositionRepository extends JpaSpecificationExecutor<Position>, JpaRepository<Position, UUID> {

    /**
     * Find all positiotions that belong to specified organization
     *
     * @param orgId organization if
     * @return collecton of positions
     */
    @EntityGraph(attributePaths = Position_.AVAILABLE_CLASSES, type = EntityGraph.EntityGraphType.LOAD)
    List<Position> findByOrganizationId(UUID orgId);

    /**
     * Check if same unique data already exists.
     *
     * @param positionName position name
     * @param orgId        organization id
     * @return <code>true</code> if entity with same data already exists
     */
    @Query("SELECT position from Position position WHERE (lower(position.name) = lower(:positionName) AND " +
            "position.organization.id = :orgId)")
    Optional<Position> findByPositionNameAndOrganizationId(final String positionName, final UUID orgId);

    /**
     * Check if same unique data already exists.
     *
     * @param positionName position name
     * @param orgId        organization id
     * @return <code>true</code> if entity with same data already exists
     */
    @Query("SELECT position from Position position WHERE lower(position.name) = lower(:positionName) " +
            "AND position.organization.id = :orgId AND position.id <> :exclude")
    Optional<Position> findByPositionNameAndOrganizationIdAndIdNot(final String positionName, final UUID orgId,
                                                                   UUID exclude);

    /**
     * Получить должность по организации и идентификатору
     *
     * @param organizationId id  организации
     * @param positionId     id должносьт
     * @return должность
     */
    Optional<Position> findByOrganizationIdAndId(UUID organizationId, UUID positionId);

    /**
     * Достать должности для организации
     *
     * @param organizationId ID организации.
     * @return список должностей
     */
    List<Position> findAllByOrganizationId(UUID organizationId);

    /**
     * Достать должность по имени и организации
     *
     * @param organizationId ID организации.
     * @param name           имя должности.
     * @return должностью
     */
    Optional<Position> findByOrganizationIdAndName(UUID organizationId, String name);
}
