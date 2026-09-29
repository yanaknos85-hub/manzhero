package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Table(schema = "approvals", name = "limit")
@Getter
@Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "limit_type", discriminatorType = DiscriminatorType.STRING)
@SuperBuilder
@AllArgsConstructor
public class Limit {
    /**
     * Unique id
     */
    @Id
    private UUID id;
    
    /**
     * Year
     */
    @Column(name = "year", nullable = false)
    private Integer year;
    
    /**
     * Sum
     */
    @Column(name = "sum", nullable = false)
    private Long sum;
    
    /**
     * Reserve
     */
    @Column(name = "reserve", nullable = false)
    private Long reserve;
    
    /**
     * Parent limit
     */
    @Column(name = "parent_id")
    private UUID parentId;
    
    /**
     * limit owner
     */
    @Column(name = "owner_id")
    private UUID ownerId;
    
    /**
     * deleted
     */
    @Column(name = "deleted")
    private Boolean deleted;
    
    @Column(name = "period", columnDefinition = "numeric")
    private Integer period;
}
