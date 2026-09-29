package ru.sber.transport.tariff_fleet.database.dao;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sber.transport.tariff_fleet.database.model.RepairTariff;
import ru.sber.transport.tariff_fleet.database.projection.GetTariffProjection;
import ru.sber.transport.tariff_fleet.model.TariffFilter;

import java.util.Optional;
import java.util.UUID;

public interface RepairTariffRepository extends JpaRepository<RepairTariff, UUID> {
    @SuppressWarnings("java:S100")
    Optional<RepairTariff> findFirstByTariff_ContractIdAndTariff_ActiveIsTrue(UUID contractId);

    @Query(nativeQuery = true,
            value = """
                SELECT  rt.tariff_id as id,
                        t.human_readable_id as humanReadableId,
                        o.official_name as organizationName,
                        co.name as contractorName,
                        d.department_name as departmentName,
                        c.number as contractNumber,
                        t.active as active
                 FROM tariff_fleet.repair_tariff rt
                   JOIN tariff_fleet.tariff t on rt.tariff_id = t.id
                   JOIN tariff_fleet.repair_contract rc ON t.contract_id = rc.contract_id
                   JOIN tariff_fleet.contract c ON rc.contract_id = c.id
                   JOIN tariff_fleet.organization o ON rc.organization_id = o.id
                   JOIN tariff_fleet.department d ON rt.department_id = d.id
                   JOIN tariff_fleet.contractor co ON rc.contractor_id = co.id
                   WHERE
                    (cast(:#{#filter.contractorId} as uuid) IS NULL OR rc.contractor_id = :#{#filter.contractorId}) AND
                    (cast(:#{#filter.organizationId} as uuid) IS NULL OR rc.organization_id = :#{#filter.organizationId}) AND
                    (cast(:#{#filter.contractId} as uuid) IS NULL OR t.contract_id = :#{#filter.contractId}) AND
                    (cast(:#{#filter.humanReadableId} as text) IS NULL OR t.human_readable_id like '%' || :#{#filter.humanReadableId} || '%') AND
                    (cast(:#{#filter.active} as boolean) IS NULL OR t.active = :#{#filter.active})
            """,
            countQuery = """
                 SELECT count(*)
                 FROM tariff_fleet.tariff t
                   JOIN tariff_fleet.repair_contract rc ON t.contract_id = rc.contract_id
                   JOIN tariff_fleet.contract c ON rc.contract_id = c.id
                   JOIN tariff_fleet.organization o ON rc.organization_id = o.id
                   JOIN tariff_fleet.repair_tariff rt ON rt.tariff_id = t.id
                   JOIN tariff_fleet.department d ON rt.department_id = d.id
                   JOIN tariff_fleet.contractor co ON rc.contractor_id = co.id
                   WHERE
                   (cast(:#{#filter.contractorId} as uuid) IS NULL OR rc.contractor_id = :#{#filter.contractorId}) AND
                   (cast(:#{#filter.organizationId} as uuid) IS NULL OR rc.organization_id = :#{#filter.organizationId}) AND
                   (cast(:#{#filter.contractId} as uuid) IS NULL OR t.contract_id = :#{#filter.contractId}) AND
                   (cast(:#{#filter.humanReadableId} as text) IS NULL OR t.human_readable_id like '%' || :#{#filter.humanReadableId} || '%') AND
                   (cast(:#{#filter.active} as boolean) IS NULL OR t.active = :#{#filter.active})
                 """)
    Page<GetTariffProjection> searchByFilter(TariffFilter filter, Pageable paging);

    @NotNull
    @Override
    @EntityGraph(attributePaths = { "department" })
    Optional<RepairTariff> findById(@NotNull UUID id);
}
