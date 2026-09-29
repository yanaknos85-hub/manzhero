package ru.sberbank.ditsib.corpclient.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.corpclient.database.model.TripPurposeWeekday;

import java.util.UUID;

@Repository
public interface TripPurposeWeekdayRepository extends JpaRepository<TripPurposeWeekday, UUID> {

}
