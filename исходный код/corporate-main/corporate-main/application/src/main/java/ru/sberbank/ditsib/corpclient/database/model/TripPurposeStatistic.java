package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Table(schema = "corporate", name = "trip_purpose_statistic")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TripPurposeStatistic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column
    private UUID employee;

    @ManyToOne
    @JoinColumn(name = "trip_purpose", nullable = false)
    private TripPurpose tripPurpose;

    @Column(name = "usage_count")
    @Builder.Default
    private int usages = 0;

}
