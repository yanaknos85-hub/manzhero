package ru.sber.transport.tariff_fleet.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.tariff_fleet.constant.InspectionType;
import ru.sber.transport.tariff_fleet.database.model.EwbContract;
import ru.sber.transport.tariff_fleet.database.model.EwbContract_;
import ru.sber.transport.tariff_fleet.database.projection.GetEwbContractProjection;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository of ewb contract
 */
@Repository
public interface EwbContractRepository extends JpaRepository<EwbContract, UUID> {
    @SuppressWarnings("java:S100")
    boolean existsByOrganizationIdAndInspectionTypeAndContract_NumberAndContract_ActiveTrue(
            UUID organizationId,
            InspectionType inspectionType,
            String number
                                                                     );
    
    @Query(value = """
                   select c1_0.id                                           as id,
                          c1_0.number                                       as number,
                          c1_0.start                                        as start,
                          c1_0."end"                                        as "end",
                          c1_0.active                                       as active,
                          (select o1_0.official_name
                           from tariff_fleet.organization o1_0
                           where o1_0.id = e1_0.organization_id) as organizationName,
                          e1_0.inspection_type                              as inspectionType,
                          e1_0.amount                                       as amount
                   from tariff_fleet.ewb_contract e1_0
                            join tariff_fleet.contract c1_0 on e1_0.contract_id = c1_0.id
                   where (e1_0.inspection_type = :inspectionType or (cast(:inspectionType as text) is null))
                      and (e1_0.organization_id = :organizationId or (cast(:organizationId as text) is null))
                      and (upper(c1_0.number) like upper('%' || :number || '%') or cast(:number as text) is null)
                      and ((cast(:start as text) is null and cast(:end as text) is null)
                        or ((cast(:start as text) is null or c1_0.start >= :start)
                        and (cast(:end as text) is null or c1_0.end <= :end)))
                        and (c1_0.active = :active or (cast(:active as boolean) is null))
                   """,
           countQuery = """
                        select count(*)
                        from tariff_fleet.ewb_contract e1_0
                                 join tariff_fleet.contract c1_0 on e1_0.contract_id = c1_0.id
                        where (e1_0.inspection_type = :inspectionType or (cast(:inspectionType as text) is null))
                          and (e1_0.organization_id = :organizationId or (cast(:organizationId as text) is null))
                          and (upper(c1_0.number) like upper('%' || :number || '%') or cast(:number as text) is null)
                          and ((cast(:start as text) is null and cast(:end as text) is null)
                            or ((cast(:start as text) is null or c1_0.start >= :start)
                            and (cast(:end as text) is null or c1_0.end <= :end)))
                            and (c1_0.active = :active or (cast(:active as boolean) is null))
                        """,
           nativeQuery = true)
    Page<GetEwbContractProjection> searchEwbContracts(
            UUID organizationId,
            String inspectionType,
            String number,
            LocalDate start,
            LocalDate end,
            Boolean active,
            Pageable pageable
                                                     );
    
    @EntityGraph(attributePaths = { EwbContract_.CONTRACT})
    @Query("select c from EwbContract c where c.contractId = :id")
    Optional<EwbContract> findByIdWithContract(UUID id);
    
    @EntityGraph(attributePaths = { EwbContract_.CONTRACT,
                                    EwbContract_.ORGANIZATION_MEDICAL_LICENSE })
    @Query("select c from EwbContract c where c.contractId = :id")
    Optional<EwbContract> findByIdWithMedicalLicense(UUID id);
}
