package ru.sber.transport.tariff_fleet.database.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sber.transport.tariff_fleet.constant.ActivationType;
import ru.sber.transport.tariff_fleet.helper.UserAuthorizationHelper;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tariff")
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Tariff {
    
    /**
     * Идентификатор записи
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    /**
     * Идентификатор записи о договоре
     */
    @NotNull
    private UUID contractId;
    
    /**
     * Флаг активности
     */
    private boolean active;
    
    /**
     * Тип активации
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    private ActivationType activationType;
    
    /**
     * Человекочитаемый идентификатор
     */
    @NotBlank
    @Size(max = 17)
    private String humanReadableId;
    
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
        var that = (Tariff) o;
        return id != null && Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
