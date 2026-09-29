package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;
import ru.sberbank.ditsib.transport.validation.HasId;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Entity of trip purpose attribute.
 */
@Entity
@Table(schema = "corporate", name = "trip_purpose_attribute")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TripPurposeAttribute implements HasId {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_purpose", nullable = false)
    private TripPurpose tripPurpose;

    @ManyToOne
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

}