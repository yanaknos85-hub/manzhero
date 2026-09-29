package ru.sberbank.ditsib.transport.limits.model.bonus;

import lombok.*;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Table(name = "bonus", schema = "limits")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Bonus {
    @Id
    @Column(name = "bonus_owner_id", nullable = false)
    private UUID ownerId;
    
    @Column(name = "sum", nullable = false)
    @Builder.Default
    private BigDecimal sum = BigDecimal.ZERO;
    
    @Column(name = "balance", nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    
    @OneToMany(mappedBy = "bonus")
    @OrderBy("updateTime DESC")
    @Builder.Default
    @ToString.Exclude
    private List<BonusRequest> requests = new ArrayList<>();
}