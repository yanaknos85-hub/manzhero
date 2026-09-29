package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;
import ru.sberbank.ditsib.transport.constants.TripPurposeType;
import ru.sberbank.ditsib.transport.validation.HasId;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity of trip purpose.
 */
@Entity
@Table(schema = "corporate", name = "trip_purpose")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TripPurpose implements HasId {

    @Builder.Default
    @OneToMany(mappedBy = "tripPurpose", cascade = CascadeType.REMOVE)
    private List<TripPurposeAttribute> tripPurposeAttributes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tripPurpose", cascade = CascadeType.REMOVE)
    private List<TripPurposeDepartment> tripPurposeDepartments = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tripPurpose", cascade = CascadeType.REMOVE)
    private final List<TripPurposeDate> tripPurposeDates = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tripPurpose", cascade = CascadeType.REMOVE)
    private final List<TripPurposeTime> tripPurposeTimes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "tripPurpose", cascade = CascadeType.REMOVE)
    private final List<TripPurposeWeekday> tripPurposeWeekdays = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String label;

    @Column(name = "purpose_type")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TripPurposeType purposeType = TripPurposeType.CORPORATE;

    @Builder.Default
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "organization", nullable = false)
    private Organization organization;

    @Column(name = "purpose_parent_label")
    private String purposeParentLabel;

    @Column
    private String icon;

}
