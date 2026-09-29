package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.TripPurposeStatistic;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripPurposeStatisticRepository extends JpaRepository<TripPurposeStatistic, UUID> {

    Optional<TripPurposeStatistic> findByEmployeeAndTripPurposeId(UUID employee, UUID tripPurposeId);

}