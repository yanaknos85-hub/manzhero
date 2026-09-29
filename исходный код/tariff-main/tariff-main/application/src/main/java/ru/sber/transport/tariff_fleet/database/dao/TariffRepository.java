package ru.sber.transport.tariff_fleet.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.model.Tariff;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository of tariff
 */
public interface TariffRepository extends JpaRepository<Tariff, UUID> {

    @Query(value =
                   """
                   SELECT
                       CASE
                           WHEN EXISTS(SELECT 1 FROM tariff_fleet.ewb_tariff et WHERE et.tariff_id = :id) THEN 'EWB'
                           WHEN EXISTS(SELECT 1 FROM tariff_fleet.repair_tariff rt WHERE rt.tariff_id = :id) THEN 'REPAIR_AND_MAINTENANCE'
                           WHEN EXISTS(SELECT 1 FROM tariff_fleet.fuel_tariff ft WHERE ft.tariff_id = :id) THEN 'FUEL'
                           ELSE NULL
                       END AS result
                   """, nativeQuery = true)
    Optional<DocumentType> getTypeById(UUID id);

    @Modifying
    @Query(value = "UPDATE Tariff t SET t.active = true WHERE t.contractId = :contractId and t.activationType != 'MANUAL'")
    void activateByContractId(UUID contractId);

    @Modifying
    @Query(value = "UPDATE Tariff t SET t.active = false, t.activationType = 'AUTO' WHERE t.contractId = :contractId")
    void deactivateByContractId(UUID contractId);

    List<Tariff> findAllByContractId(UUID contractId);
}
