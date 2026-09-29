package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;
import ru.sberbank.ditsib.transport.validation.HasId;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.util.UUID;

/**
 * Entity of trip purpose attribute.
 */
@Entity
@Table(schema = "corporate", name = "trip_purpose_weekday")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TripPurposeWeekday implements HasId {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_purpose", nullable = false)
    private TripPurpose tripPurpose;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Set a week day")
    private DayOfWeek weekday;
}
