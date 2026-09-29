package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.TripPurposeDate;

import java.util.UUID;

@Repository
public interface TripPurposeDateRepository extends JpaRepository<TripPurposeDate, UUID> {
}
