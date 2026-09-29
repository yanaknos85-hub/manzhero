package ru.sber.transport.tariff_fleet.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sber.transport.tariff_fleet.helper.UserAuthorizationHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "contract")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Contract {
    
    /**
     * Идентификатор записи
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * Дата и время создания
     */
    @NotNull
    @Column(updatable = false)
    private LocalDateTime creationTime;
    
    /**
     * Идентификатор записи с таблицы corporate.user сотрудника, создавшего запись
     */
    @NotNull
    @Column(updatable = false)
    private UUID creatorUserId;
    
    /**
     * Дата начала действия
     */
    @NotNull
    @Column(updatable = false)
    private LocalDate start;
    
    /**
     * Дата окончания действия
     */
    @NotNull
    @Column(name = "\"end\"")
    private LocalDate end;
    
    /**
     * Номер
     */
    @NotBlank
    @Size(min = 1, max = 50)
    private String number;
    
    /**
     * Номер договора УВХД
     */
    @Size(min = 1, max = 50)
    private String uvhd;
    
    /**
     * Флаг активности
     */
    private boolean active;
    
    @PrePersist
    private void onCreation() {
        this.creationTime = LocalDateTime.now();
        this.creatorUserId = UserAuthorizationHelper.getUserId(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        var that = (Contract) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
