package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.validation.HasId;

import jakarta.persistence.*;
import java.util.*;

/**
 * Entity of organization
 */
@Entity
@Table(schema = "corporate", name = "organization")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization implements HasId {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "digit_id", insertable = false, updatable = false)
    private Long digitId;

    @Column(name = "sync_id")
    private String syncId;

    @Column(nullable = false, unique = true)
    private String officialName;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(schema = "corporate", name = "organization_contacts",
               joinColumns = @JoinColumn(name = "organization_id"),
               inverseJoinColumns = @JoinColumn(name = "contact_id"))
    @Builder.Default
    private List<Contact> contacts = new ArrayList<>();

    @Column(nullable = false)
    private String address;

    @Column(name = "msrn")
    private String msrn;

    @Column(name = "tin")
    private String tid;
    
    @Column(name = "organization_code")
    private Integer organizationCode;

    @OneToMany(mappedBy = "organization", orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Position> positions = new HashSet<>();
    
    @OneToMany(mappedBy = "organization", orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Department> departments = new HashSet<>();
    
    @OneToMany(mappedBy = "organization", orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TripPurpose> tripPurposes = new ArrayList<>();
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Builder.Default
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    @OneToMany(mappedBy = "organization")
    @Builder.Default
    private List<Employee> employees = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_group_id")
    private OrganizationGroup organizationGroup;

    @OneToMany(mappedBy = "organization", orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CargoType> cargoTypes = new ArrayList<>();

    @Transient
    private List<String> availableClasses = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Organization other) {
            return id != null &&
                    id.equals(other.getId());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return 31;
    }
    
}
