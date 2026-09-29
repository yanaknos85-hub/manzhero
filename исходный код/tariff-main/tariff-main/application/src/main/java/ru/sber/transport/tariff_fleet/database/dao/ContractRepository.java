package ru.sber.transport.tariff_fleet.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.model.Contract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository of contract
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID>, JpaSpecificationExecutor<Contract> {
    boolean existsByUvhdAndActiveTrue(String uvhd);
    
    @Override
    Page<Contract> findAll(Specification<Contract> spec, Pageable pageable);
    
    @Query("SELECT c FROM Contract c WHERE c.active = false AND c.start <= CURRENT_DATE AND c.end > CURRENT_DATE")
    List<Contract> findStartedContracts();
    
    @Query("SELECT c FROM Contract c WHERE c.active = true AND c.end < CURRENT_DATE")
    List<Contract> findEndedContracts();
    
    @Query(value =
                   """
                   SELECT
                       CASE
                           WHEN EXISTS(SELECT 1 FROM tariff_fleet.ewb_contract ec WHERE ec.contract_id = :id) THEN 'EWB'
                           WHEN EXISTS(SELECT 1 FROM tariff_fleet.repair_contract rc WHERE rc.contract_id = :id) THEN 'REPAIR_AND_MAINTENANCE'
                           WHEN EXISTS(SELECT 1 FROM tariff_fleet.fuel_contract rc WHERE rc.contract_id = :id) THEN 'FUEL'
                           ELSE NULL
                       END AS result
                   """, nativeQuery = true)
    Optional<DocumentType> getTypeById(UUID id);
}
