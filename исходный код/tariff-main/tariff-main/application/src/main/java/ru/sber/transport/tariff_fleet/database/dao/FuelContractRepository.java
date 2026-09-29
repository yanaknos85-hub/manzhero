package ru.sber.transport.tariff_fleet.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.tariff_fleet.database.model.FuelContract;
import ru.sber.transport.tariff_fleet.database.model.FuelContract_;
import ru.sber.transport.tariff_fleet.database.projection.GetFuelContractProjection;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository of fuel contract
 */
@Repository
public interface FuelContractRepository extends JpaRepository<FuelContract, UUID> {

    @EntityGraph(attributePaths = {FuelContract_.CONTRACT, FuelContract_.SERVICE_POINTS})
    Optional<FuelContract> findWithContractByContractId(UUID contractId);

    @SuppressWarnings("java:S100")
    boolean existsByOrganizationIdAndContractorIdAndContract_NumberAndContract_ActiveIsTrue(UUID organizationId, UUID contractorId, String number);

    @Query(value = """
            SELECT  fc.contractor_id         AS contractorId,
                    co.name                  AS contractorName,
                    o.official_name          AS organizationName,
                    c.id                     AS id,
                    c."number"               AS number,
                    c.uvhd                   AS uvhd,
                    c."start"                AS start,
                    c."end"                  AS end,
                    fc.amount_without_vat    AS amount,
                    c.active                 AS active
            FROM tariff_fleet.fuel_contract fc
            JOIN tariff_fleet.contract c ON fc.contract_id = c.id
            JOIN tariff_fleet.organization o ON fc.organization_id = o.id
            JOIN tariff_fleet.contractor co ON fc.contractor_id = co.id
            WHERE (CAST(:number AS text) IS NULL OR UPPER(c."number") LIKE UPPER(CONCAT('%',:number ,'%')))
            AND (CAST(:active AS text) IS NULL OR c.active = :active)
            AND (CAST(:contractorId AS text) IS NULL OR fc.contractor_id = :contractorId)
            AND (CAST(:organizationId AS text) IS NULL OR o.id = :organizationId)
            AND ((CAST(:start AS date) IS NULL AND CAST(:end AS date) IS NULL)
            OR ((CAST(:start AS date) IS NULL OR c.start >= :start) AND (CAST(:end AS date) IS NULL OR c.end <= :end)))
            """,
            countQuery = """
                    SELECT  COUNT(*)
                    FROM tariff_fleet.fuel_contract fc
                    JOIN tariff_fleet.contract c ON fc.contract_id = c.id
                    JOIN tariff_fleet.organization o ON fc.organization_id = o.id
                    WHERE (CAST(:number AS text) IS NULL OR UPPER(c."number") LIKE UPPER(CONCAT('%',:number ,'%')))
                     AND (CAST(:active AS text) IS NULL OR c.active = :active)
                     AND (CAST(:contractorId AS text) IS NULL OR fc.contractor_id = :contractorId)
                     AND (CAST(:organizationId AS text) IS NULL OR o.id = :organizationId)
                        AND ((CAST(:start AS date) IS NULL AND CAST(:end AS date) IS NULL)
                         OR ((CAST(:start AS date) IS NULL OR c.start >= :start) AND (CAST(:end AS date) IS NULL OR c.end <= :end)))
                    """,
            nativeQuery = true)
    Page<GetFuelContractProjection> searchFuelContracts(
            String number, UUID contractorId, UUID organizationId, LocalDate start, LocalDate end, Boolean active,
            Pageable pageable
    );

    @SuppressWarnings("java:S100")
    boolean existsByContractorIdAndContract_ActiveTrue(UUID contractorId);
}
