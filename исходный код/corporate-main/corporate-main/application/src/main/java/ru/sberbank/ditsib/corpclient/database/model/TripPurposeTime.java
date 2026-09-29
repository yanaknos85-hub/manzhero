package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;
import ru.sberbank.ditsib.transport.validation.HasId;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity of trip purpose attribute.
 */
@Entity
@Table(schema = "corporate", name = "trip_purpose_time")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TripPurposeTime implements HasId {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_purpose", nullable = false)
    private TripPurpose tripPurpose;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
}
