package ru.sberbank.ditsib.transport.limits.model.limit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;

@Entity
@Getter
@Setter
@DiscriminatorValue(value = LimitType.Values.EMPLOYEE)
public class EmpLimit extends Limit {
    
    /**
     * Employee.
     */
    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
