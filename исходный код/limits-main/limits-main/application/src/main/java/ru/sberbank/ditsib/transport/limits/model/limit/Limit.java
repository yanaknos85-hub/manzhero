package ru.sberbank.ditsib.transport.limits.model.limit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Table(schema = "limits", name = "limit")
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "limit_type", discriminatorType = DiscriminatorType.STRING)
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(of = "id")
public abstract class Limit {

    /**
     * Unique idetificator.
     */
    @Id
    @GeneratedValue
    @ToString.Include
    private UUID id;

    /**
     * Limit type
     */
    @Column(name = "limit_type", nullable = false, insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private LimitType limitType;

    /**
     * Limit ID formed by Limit-ID rules
     */
    @JsonIgnore
    @Column(name = "human_readable_id", nullable = false)
    private String humanReadableId;

    /**
     * Limit owner
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "author_id", nullable = false)
    private Employee author;

    /**
     * Limit owner
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "limit_owner_id")
    private Employee limitOwner;

    /**
     * Status of limit
     */
    @Column(name = "limit_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private LimitStatus limitStatus;

    /**
     * Year
     */
    @Column(name = "year", nullable = false)
    private int year;

    /**
     * Limit sharing type
     */
    @Column(name = "limit_sharing_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private LimitSharingType limitSharingType;

    /**
     * Limit sharing type
     */
    @Column(name = "service_type")
    private String limitServiceType;

    /**
     * Sum
     */
    @JsonIgnore
    @Column(name = "sum", nullable = false)
    private BigDecimal sum;

    /**
     * Final sharing flag
     */
    @JsonIgnore
    @Column(name = "final_sharing", nullable = false)
    private boolean finalSharing;

    /**
     * Use my limit flag
     */
    @JsonIgnore
    @Column(name = "use_my_limit", nullable = false)
    private boolean useThisLimit;

    /**
     * Date and time or request creation
     */
    @JsonIgnore
    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;

    /**
     * Organization.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "organization_id")
    private Organization organization;

    /**
     * Parent department - for searching by distributing department without joining.
     *
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;

    /**
     * Parent limit
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "parent_id")
    private Limit parent;

    /**
     * Child limits.
     */
    @Setter
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Limit> children = new ArrayList<>();

    /**
     * Limit sharings.
     */
    @Setter
    @OneToMany(mappedBy = "limit", cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    private List<LimitSharing> sharings = new ArrayList<>();

    @Column(name = "update_time")
    private OffsetDateTime updateTime = OffsetDateTime.now();

    @Column(name = "hash")
    private String hash;

    @PrePersist
    void hashing() {
        hash = String.valueOf(UUID.nameUUIDFromBytes("%s%s".formatted(getClass().getCanonicalName(), updateTime).getBytes()).getMostSignificantBits());
    }
}
