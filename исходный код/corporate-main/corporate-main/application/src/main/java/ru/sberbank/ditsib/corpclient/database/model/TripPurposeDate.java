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
@Table(schema = "corporate", name = "trip_purpose_date")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TripPurposeDate implements HasId {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_purpose", nullable = false)
    private TripPurpose tripPurpose;

    @Column(name = "purpose_date")
    private LocalDateTime startDate;

    @Column(name = "purpose_date_end")
    private LocalDateTime endDate;
}
