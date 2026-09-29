package ru.sberbank.ditsib.transport.limits.model.bonus;

import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "bonus_request", schema = "limits")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BonusRequest {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus", nullable = false)
    @ToString.Exclude
    private Bonus bonus;
    
    @Column(name = "sum", nullable = false)
    private BigDecimal sum;
    
    @Column(name = "operation", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private BonusOperation operation;
    
    @Column(name = "creation_time", nullable = false)
    private LocalDateTime creationTime;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private BonusRequestStatus status;
    
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;
    
    @Column(name = "reason")
    private String reason;
    
    @Column(name = "requestId", nullable = false)
    private UUID requestId;
    
    @Column(name = "transport_type")
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private TransportTypeEnum transportType;
    
    @PrePersist
    void create() {
        creationTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    void update() {
        updateTime = LocalDateTime.now();
    }
}