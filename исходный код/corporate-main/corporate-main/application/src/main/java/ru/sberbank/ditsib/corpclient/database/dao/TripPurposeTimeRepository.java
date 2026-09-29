package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.TripPurposeTime;

import java.util.UUID;

@Repository
public interface TripPurposeTimeRepository extends JpaRepository<TripPurposeTime, UUID> {

}
