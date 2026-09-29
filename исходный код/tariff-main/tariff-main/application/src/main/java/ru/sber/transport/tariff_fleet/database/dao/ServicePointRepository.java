package ru.sber.transport.tariff_fleet.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.tariff_fleet.database.model.ServicePoint;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ServicePoint entity
 */
@Repository
public interface ServicePointRepository extends JpaRepository<ServicePoint, UUID> {

    List<ServicePoint> findAllByContractId(UUID contractId);

    @Modifying
    @Query(value = "DELETE FROM ServicePoint WHERE contractId = :id")
    void deleteAllByContractId(UUID id);

    @Modifying
    @Query(value = "UPDATE ServicePoint SET active = false WHERE contractId = :id")
    void deactivateByContractId(UUID id);
}
