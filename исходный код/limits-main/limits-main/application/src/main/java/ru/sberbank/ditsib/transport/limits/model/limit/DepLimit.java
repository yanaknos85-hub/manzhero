package ru.sberbank.ditsib.transport.limits.model.limit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;

import java.math.BigDecimal;

@Entity
@Setter
@Getter
@DiscriminatorValue(value = LimitType.Values.DEPARTMENT)
public class DepLimit extends Limit {
    
    /**
     * Department.
     */
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
    
    /**
     * Reserve
     */
    @Column(name = "reserve", nullable = false)
    private BigDecimal reserve;
    
    /**
     * Экономия
     */
    @Column(name = "economy", nullable = false)
    private BigDecimal economy;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
