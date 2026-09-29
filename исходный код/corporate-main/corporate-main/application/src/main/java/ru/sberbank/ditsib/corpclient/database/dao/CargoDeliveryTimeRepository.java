package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.CargoDeliveryTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for cargo delivery times
 */
@Repository
public interface CargoDeliveryTimeRepository extends JpaRepository<CargoDeliveryTime, UUID>,
        JpaSpecificationExecutor<CargoDeliveryTime> {
    
    /**
     * Get all status delivery times
     *
     * @return  delivery times.
     */
    @Query("SELECT c from CargoDeliveryTime c where c.active = true")
    List<CargoDeliveryTime> findAllActive();

    /**
     * Get all status delivery times by label.
     *
     * @param label label of record.
     * @return  delivery times.
     */
    @Query("select c from CargoDeliveryTime c where c.active = true and c.label = :label")
    Optional<CargoDeliveryTime> findCargoDeliveryTimeByLabel(String label);
}
