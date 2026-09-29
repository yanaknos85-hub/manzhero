package ru.sberbank.ditsib.corpclient.database.dao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.TripPurpose;
import ru.sberbank.ditsib.corpclient.database.model.TripPurpose_;

import java.util.*;

/**
 * Repository for business trip purpose
 */
@Repository
public interface TripPurposeRepository extends JpaRepository<TripPurpose, UUID> {


    /**
     * Search trip purposes by purpose substring
     *
     * @param searchString search string
     *
     * @return list of qualified entities
     */
    @Query("SELECT purpose from TripPurpose purpose where " +
            "LOWER(purpose.label) like lower(concat('%', :searchString,'%')) and " +
            "purpose.organization.id = :organization and " +
            "purpose.active = true")
    List<TripPurpose> findAllByLabelLike(@NotBlank String searchString, @NotNull UUID organization);

    /**
     * Get trip purpose by id.
     *
     * @param uuid id a trip purpose
     *
     * @return  purpose.
     */
    @Query("SELECT purpose from TripPurpose purpose where purpose.id = :uuid and " +
            "purpose.active = true")
    Optional<TripPurpose> findById(@NotNull UUID uuid);

    /**
     * Get trip purpose by organization
     *
     * @param organization organization a trip purpose
     *
     * @return  purpose.
     */
    @Query("SELECT purpose from TripPurpose purpose where " +
            "purpose.organization.id = :organization and " +
            "purpose.active = true")
    List<TripPurpose> findAllByOrganizationAndActive(@NotNull UUID organization);

    /**
     * Get all trip purpose by organization
     *
     * @param organization organization a trip purpose
     *
     * @return  purpose.
     */
    @Query("SELECT purpose from TripPurpose purpose where " +
            "purpose.organization.id = :organization and purpose.active = true")
    List<TripPurpose> findAllByOrganization(@NotNull UUID organization);

    /**
     * Get trip purpose by id and organization
     *
     * @param uuid id a trip purpose
     * @param organization organization a trip purpose
     *
     * @return  purpose.
     */
    @Query("SELECT purpose from TripPurpose purpose where " +
            "purpose.id = :uuid and " +
            "purpose.organization.id = :organization and purpose.active = true")
    Optional<TripPurpose> findByIdAndOrganization(@NotNull UUID uuid, @NotNull UUID organization);

    /**
     * Get trip purpose by label and organization
     *
     * @param label id a trip purpose
     * @param organization organization a trip purpose
     *
     * @return  purpose.
     */
    @Query("SELECT purpose from TripPurpose purpose where " +
            "lower(purpose.label) = lower(:label) and " +
            "purpose.organization.id = :organization and " +
            "purpose.active = true")
    Optional<TripPurpose> findByLabelAndOrganization(@NotBlank String label, @NotNull UUID organization);

    /**
     * Get trip purposes by empty attributes and organization
     *
     * @return list of trip purposes.
     */
    @Query("SELECT tripPurpose FROM TripPurpose tripPurpose " +
            "LEFT JOIN tripPurpose.tripPurposeAttributes tripPurposeAttribute " +
            "LEFT JOIN tripPurposeAttribute.attribute attribute " +
            "LEFT JOIN tripPurpose.organization organization " +
            "WHERE organization.id = :organization " +
            "and tripPurpose.active = true")
    List<TripPurpose> findAllByEmptyAttributes(@NotNull UUID organization);
    
    /**
     * Получение всех целей вместе с организациями.
     *
     * @param sort сортировка.
     * @return список целей.
     */
    @EntityGraph(attributePaths = TripPurpose_.ORGANIZATION)
    @Query("SELECT tripPurpose FROM TripPurpose tripPurpose INNER JOIN tripPurpose.organization organization ORDER BY tripPurpose.label")
    List<TripPurpose> findAllWithOrganization(Sort... sort);

    @Query(
            value = """
                    with trip_purposes_filtered AS (
                    SELECT
                    	tp.*
                    FROM
                    	corporate.trip_purpose tp
                    LEFT JOIN corporate.trip_purpose_attribute tpa on tpa.trip_purpose = tp.id
                    LEFT JOIN corporate.attribute a on a.id = tpa.attribute_id
                    LEFT JOIN corporate.organization o on o.id = tp.organization
                    where (a.id IN (:attributes) or a.id is null)
                        AND o.id = :organizationId
                        AND tp.active = true),
                    employee_purpose_stats AS (
                        SELECT
                            tp.purpose_parent_label,
                            SUM(tps.usage_count) as total_usage_count
                        FROM corporate.trip_purpose_statistic tps
                        INNER JOIN corporate.trip_purpose tp ON tps.trip_purpose = tp.id
                        WHERE tps.employee = :employeeId
                            AND tp.purpose_parent_label IN (SELECT purpose_parent_label FROM trip_purposes_filtered)
                        GROUP BY tp.purpose_parent_label
                    ),
                    trip_purposes_by_statistic AS (
                    SELECT
                        tt.*
                    FROM trip_purposes_filtered tt
                    INNER JOIN employee_purpose_stats eps ON tt.purpose_parent_label = eps.purpose_parent_label
                    ORDER BY eps.total_usage_count desc),
                    trip_purposes_filtered_by_statistic AS (
                    SELECT * FROM trip_purposes_filtered where id not in (SELECT id FROM trip_purposes_by_statistic))
                    SELECT * FROM trip_purposes_by_statistic
                        UNION ALL
                    SELECT * FROM trip_purposes_filtered_by_statistic
                    """, nativeQuery = true)
    LinkedList<TripPurpose> findAllByAttributesAndStatistic(Set<UUID> attributes,
                                                            @NotNull UUID organizationId,
                                                            @NotNull UUID employeeId);
}
