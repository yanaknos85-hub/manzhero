package ru.sberbank.ditsib.corpclient.database.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.sberbank.ditsib.corpclient.database.model.ActivePersonalCar;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for working with status transport.
 */
public interface ActivePersonalCarRepository extends JpaRepository<ActivePersonalCar, UUID> {

    /**
     * Find status transport of user.
     *
     * @param ownerId ID of user.
     * @return transport data.
     */
    Optional<ActivePersonalCar> findByOwnerId(UUID ownerId);

    /**
     * Remove status personal transport from user.
     *
     * @param carId ID of transport.
     */
    void deleteByPersonalCarId(UUID carId);
}
