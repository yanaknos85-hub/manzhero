package ru.sber.transport.tariff_fleet.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.tariff_fleet.database.model.EwbTariff;
import ru.sber.transport.tariff_fleet.database.model.EwbTariff_;
import ru.sber.transport.tariff_fleet.database.projection.GetEwbTariffByIdProjection;
import ru.sber.transport.tariff_fleet.database.projection.GetEwbTariffProjection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository of ewb tariff
 */
@Repository
public interface EwbTariffRepository extends JpaRepository<EwbTariff, UUID> {
    @SuppressWarnings("java:S100")
    boolean existsByDepartmentIdAndTariff_ContractIdAndTariff_ActiveIsTrue(UUID departmentId, UUID contractId);
    
    @Query(value = """
                   select t1_0.id                                as id,
                          t1_0.human_readable_id                 as humanReadableId,
                          t1_0.active                            as active,
                          e2_0.inspection_type                   as inspectionType,
                          (select o1_0.official_name
                           from tariff_fleet.organization o1_0
                           where o1_0.id = e1_0.organization_id) as organizationName,
                          (select d1_0.department_name
                           from tariff_fleet.department d1_0
                           where d1_0.id = e1_0.department_id)   as departmentName,
                          (select o2_0.official_name
                           from tariff_fleet.organization o2_0
                           where o2_0.id = e2_0.organization_id) as contractOrganizationName,
                          c1_0.number                            as contractNumber
                   from tariff_fleet.ewb_tariff e1_0
                            join tariff_fleet.tariff t1_0 on e1_0.tariff_id = t1_0.id
                            join tariff_fleet.ewb_contract e2_0 on e2_0.contract_id = t1_0.contract_id
                            join tariff_fleet.contract c1_0 on c1_0.id = e2_0.contract_id
                   where (e1_0.organization_id = :organizationId or (cast(:organizationId as text) is null))
                     and (e1_0.department_id = :departmentId or (cast(:departmentId as text) is null))
                     and (e2_0.inspection_type = :inspectionType or (cast(:inspectionType as text) is null))
                     and (e2_0.organization_id = :contractOrganizationId or (cast(:contractOrganizationId as text) is null))
                     and (upper(t1_0.human_readable_id) like upper('%' || :humanReadableId || '%') or cast(:humanReadableId as text) is null)
                     and (t1_0.active = :active or (cast(:active as boolean) is null))
                   """,
           countQuery = """
                        select count(*)
                        from tariff_fleet.ewb_tariff e1_0
                                 join tariff_fleet.tariff t1_0 on e1_0.tariff_id = t1_0.id
                                 join tariff_fleet.ewb_contract e2_0 on e2_0.contract_id = t1_0.contract_id
                        where (e1_0.organization_id = :organizationId or (cast(:organizationId as text) is null))
                          and (e1_0.department_id = :departmentId or (cast(:departmentId as text) is null))
                          and (e2_0.inspection_type = :inspectionType or (cast(:inspectionType as text) is null))
                          and (e2_0.organization_id = :contractOrganizationId or (cast(:contractOrganizationId as text) is null))
                          and (upper(t1_0.human_readable_id) like upper('%' || :humanReadableId || '%') or cast(:humanReadableId as text) is null)
                          and (t1_0.active = :active or (cast(:active as boolean) is null))
                        """,
           nativeQuery = true)
    Page<GetEwbTariffProjection> searchEwbTariffs(
            UUID organizationId,
            UUID departmentId,
            UUID contractOrganizationId,
            String inspectionType,
            String humanReadableId,
            Boolean active,
            Pageable pageable
                                                 );
    
    @Query(value = """
                   SELECT et.tariff_id         AS id,
                          ec.inspection_type   AS inspectionType,
                          floo.official_name   AS fleetOwnerName,
                          flod.department_name AS fleetOwnerDepartmentName,
                          con.official_name    AS contractorName,
                          c."number"           AS contractNumber,
                          et.amount            AS amount
                   FROM tariff_fleet.ewb_tariff et
                   JOIN tariff_fleet."tariff" t ON et.tariff_id = t.id
                   JOIN tariff_fleet.contract c ON t.contract_id = c.id
                   JOIN tariff_fleet.ewb_contract ec ON t.contract_id = ec.contract_id
                   JOIN tariff_fleet.organization con ON ec.organization_id = con.id
                   JOIN tariff_fleet.organization floo ON et.organization_id = floo.id
                   JOIN tariff_fleet.department flod ON et.department_id = flod.id
                   WHERE et.tariff_id = :tariffId
                   LIMIT 1
                   """, nativeQuery = true)
    Optional<GetEwbTariffByIdProjection> findByTariffId(UUID tariffId);
    
    @Query("""
           select t.departmentId from EwbTariff t
            where t.tariff.contractId = :contractId
            and t.tariff.active is true
           """)
    List<UUID> findAllActiveTariffDepartmentIdsByContractId(UUID contractId);
    
    @EntityGraph(attributePaths = { EwbTariff_.TARIFF })
    @Query("select t from EwbTariff t where t.tariffId = :id")
    Optional<EwbTariff> findByIdWithTariff(UUID id);
    
    @Query("""
           SELECT et FROM EwbTariff et
           JOIN FETCH et.tariff t
           WHERE t.contractId = :contractId AND t.active IS NOT TRUE AND t.activationType <> 'MANUAL'
           """)
    List<EwbTariff> findNotActiveNotManualByContractIdWithTariff(UUID contractId);
    
    @Query("""
           SELECT et FROM EwbTariff et
           JOIN FETCH et.tariff t
           WHERE t.contractId = :contractId AND t.active IS TRUE
           """)
    List<EwbTariff> findActiveByContractIdWithTariff(UUID contractId);

    @Query(value = """
            select exists (
                select *
                from tariff_fleet.contract c
                join tariff_fleet.ewb_contract ec on c.id = ec.contract_id
                join tariff_fleet.tariff t on c.id = t.contract_id
                join tariff_fleet.ewb_tariff et on t.id = et.tariff_id
                where et.department_id = :departmentId
                    and t.active is true
                    and ec.inspection_type in ('MEDIC', 'TELEMEDIC')
                )
            """, nativeQuery = true)
    boolean existsByDepartmentIdAndActiveIsTrueAndMedicineContractType(UUID departmentId);
}
