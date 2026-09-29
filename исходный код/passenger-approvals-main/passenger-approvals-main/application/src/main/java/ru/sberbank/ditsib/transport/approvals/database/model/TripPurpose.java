package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Цель поездки
 **/
@Entity
@Table(schema = "approvals", name = "trip_purpose")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripPurpose {
    /**
     * Первичный ключ
     */
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    
    /**
     * Label.
     */
    @Column(name = "label", nullable = false)
    private String label;
    
}
