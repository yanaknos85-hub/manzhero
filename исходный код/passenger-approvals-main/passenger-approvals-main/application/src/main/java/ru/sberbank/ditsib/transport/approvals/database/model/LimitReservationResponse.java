package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "limit_reservation_responses", schema = "approvals")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LimitReservationResponse {
    /**
     * Уникальный идентификатор записи
     */
    @Id
    @GeneratedValue
    private UUID id;
    /**
     * id request
     */
    @Setter
    @Column(name = "trip_request_id", unique = true)
    private UUID tripRequestId;

    @Setter
    @Column(name = "status")
    private String status;

}
