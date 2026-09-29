package ru.sber.transport.tariff_fleet.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sber.transport.tariff_fleet.constant.ServiceType;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.model.ContractorFilter;

import java.util.List;
import java.util.UUID;

/**
 * Repository of department
 */
@Repository
public interface ContractorRepository extends JpaRepository<Contractor, UUID> {
    
    List<Contractor> findAllByServiceTypeAndActiveTrueOrderByNameAsc(ServiceType serviceType);
    
    @Query(value =
                   """
                   SELECT DISTINCT c.*
                   FROM tariff_fleet.contractor c
                   INNER JOIN (
                       SELECT 'FUEL' AS doc_type, contractor_id, contract_id, organization_id FROM tariff_fleet.fuel_contract
                       UNION ALL
                       SELECT 'REPAIR_AND_MAINTENANCE' AS doc_type, contractor_id, contract_id, organization_id FROM tariff_fleet.repair_contract
                   ) AS t ON t.contractor_id = c.id AND t.doc_type = :#{#filter.documentType()?.name()}
                   INNER JOIN tariff_fleet.contract ct ON t.contract_id = ct.id
                   WHERE ct.active = :#{#filter.active()}
                   AND c.service_type = (:#{#filter.serviceType()?.name()})::tariff_fleet.service_type
                   AND (:#{#filter.selfOnly()} IS FALSE OR t.organization_id = :#{#filter.organizationId()})
                   """,
           nativeQuery = true)
    List<Contractor> findContractorsByFilter(@Param("filter") ContractorFilter filter);
}

