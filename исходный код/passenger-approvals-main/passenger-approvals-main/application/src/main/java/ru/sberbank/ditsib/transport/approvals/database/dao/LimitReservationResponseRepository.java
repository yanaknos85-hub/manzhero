package ru.sberbank.ditsib.transport.approvals.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sberbank.ditsib.transport.approvals.database.model.LimitReservationResponse;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LimitReservationResponseRepository extends JpaRepository<LimitReservationResponse, UUID> {

    Optional<LimitReservationResponse> findByTripRequestId(UUID requestId);
}
