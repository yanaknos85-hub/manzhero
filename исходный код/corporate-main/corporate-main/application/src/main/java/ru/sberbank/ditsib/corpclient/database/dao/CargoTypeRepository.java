package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.CargoType;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for cargo type
 */
@Repository
public interface CargoTypeRepository extends JpaRepository<CargoType, UUID> {
    /**
     * Get type by name and status
     *
     * @param name name of a type
     * @return type.
     */
    @Query("select c from CargoType c where " +
            "lower(c.name) = lower(:name) and c.active = true")
    Optional<CargoType> findByNameAndActive(@NotBlank String name);

    /**
     * Get all status types
     *
     * @return type.
     */
    List<CargoType> findAllByActiveTrue();

    /**
     * Get status type by id
     *
     * @param id id
     * @return type.
     */
    Optional<CargoType> findByIdAndActiveTrue(UUID id);

    /**
     * Get status type by id and organization
     *
     * @param id id
     * @param organizationId organization
     * @return type.
     */
    Optional<CargoType> findByIdAndOrganizationIdAndActiveTrue(UUID id, UUID organizationId);

    /**
     * Get types by name
     *
     * @param name name of a type
     * @return type.
     */
    @Query("SELECT c from CargoType c where " +
            "lower(c.name) like concat(lower(:name), '%') and c.active = true")
    List<CargoType> findAllByNameLikeAndActiveTrue(@NotBlank String name);

    /**
     * Get types by name and organization
     *
     * @param name name of a type
     * @param organizationId organization
     * @return type.
     */
    @Query("SELECT c from CargoType c where " +
            "lower(c.name) like concat(lower(:name), '%') and c.active = true and (c.organization.id is null or c.organization.id = :organizationId)")
    List<CargoType> findAllByOrganizationAndNameLikeAndActiveTrue(UUID organizationId, @NotBlank String name);

    /**
     * Get types by name with empty organization
     *
     * @param name name of a type
     * @return type.
     */
    @Query("SELECT c from CargoType c where " +
            "lower(c.name) like concat(lower(:name), '%') and c.active = true and c.organization.id is null")
    List<CargoType> findAllByEmptyOrganizationAndNameLikeAndActiveTrue(@NotBlank String name);

    /**
     * Get types by organizationId
     *
     * @param organizationId organization
     * @return type.
     */
    @Query("select c from CargoType c where c.active = true and (c.organization.id is null or c.organization.id = :organizationId)")
    List<CargoType> findAllByOrganizationIdAndActive(UUID organizationId);
}
