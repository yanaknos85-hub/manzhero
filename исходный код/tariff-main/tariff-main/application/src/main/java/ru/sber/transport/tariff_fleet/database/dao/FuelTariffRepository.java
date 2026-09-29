package ru.sber.transport.tariff_fleet.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sber.transport.tariff_fleet.constant.ActivationType;
import ru.sber.transport.tariff_fleet.database.model.FuelTariff;
import ru.sber.transport.tariff_fleet.database.projection.GetTariffProjection;
import ru.sber.transport.tariff_fleet.model.TariffFilter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuelTariffRepository extends JpaRepository<FuelTariff, UUID> {
    @SuppressWarnings("java:S100")
    Optional<FuelTariff> findFirstByTariff_ContractIdAndDepartmentIdAndTariff_ActiveIsTrue(UUID contractId, UUID departmentId);

    @Query(nativeQuery = true,
    value = """ 
                SELECT  ft.tariff_id as id,
                        t.human_readable_id as humanReadableId,
                        o.official_name as organizationName,
                        co.name as contractorName,
                        c.number as contractNumber,
                        t.active as active
                 FROM tariff_fleet.fuel_tariff ft
                   JOIN tariff_fleet.tariff t on ft.tariff_id = t.id
                   JOIN tariff_fleet.fuel_contract fc ON t.contract_id = fc.contract_id
                   JOIN tariff_fleet.contract c ON fc.contract_id = c.id
                   JOIN tariff_fleet.organization o ON fc.organization_id = o.id
                   JOIN tariff_fleet.contractor co ON fc.contractor_id = co.id
                   WHERE
                    (cast(:#{#filter.contractorId} as uuid) IS NULL OR fc.contractor_id = :#{#filter.contractorId}) AND
                    (cast(:#{#filter.organizationId} as uuid) IS NULL OR fc.organization_id = :#{#filter.organizationId}) AND
                    (cast(:#{#filter.contractId} as uuid) IS NULL OR t.contract_id = :#{#filter.contractId}) AND
                    (cast(:#{#filter.humanReadableId} as text) IS NULL OR t.human_readable_id like  '%' || :#{#filter.humanReadableId} || '%') AND
                    (cast(:#{#filter.active} as boolean) IS NULL OR t.active = :#{#filter.active})
            """,
    countQuery = """
                 SELECT count(*)
                 FROM tariff_fleet.fuel_tariff ft
                   JOIN tariff_fleet.tariff t on ft.tariff_id = t.id
                   JOIN tariff_fleet.fuel_contract fc ON t.contract_id = fc.contract_id
                   JOIN tariff_fleet.contract c ON fc.contract_id = c.id
                   JOIN tariff_fleet.organization o ON fc.organization_id = o.id
                   JOIN tariff_fleet.contractor co ON fc.contractor_id = co.id
                   WHERE
                   (cast(:#{#filter.contractorId} as uuid) IS NULL OR fc.contractor_id = :#{#filter.contractorId}) AND
                   (cast(:#{#filter.organizationId} as uuid) IS NULL OR fc.organization_id = :#{#filter.organizationId}) AND
                   (cast(:#{#filter.contractId} as uuid) IS NULL OR t.contract_id = :#{#filter.contractId}) AND
                   (cast(:#{#filter.humanReadableId} as text) IS NULL OR t.human_readable_id = '%' ||:#{#filter.humanReadableId} || '%') AND
                   (cast(:#{#filter.active} as boolean) IS NULL OR t.active = :#{#filter.active})
                 """)
    Page<GetTariffProjection> searchByFilter(TariffFilter filter, Pageable paging);

    @SuppressWarnings("java:S100")
    List<FuelTariff> findAllByTariff_ContractIdAndTariff_ActivationTypeNot(UUID contractId, ActivationType activationType);

    @SuppressWarnings("java:S100")
    List<FuelTariff> findAllByTariff_ContractId(UUID contractId);
}
