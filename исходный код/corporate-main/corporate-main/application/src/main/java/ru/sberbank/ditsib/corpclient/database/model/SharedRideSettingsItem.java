package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;
import ru.sberbank.ditsib.transport.validation.HasId;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Сущность БД - Один элемент настройки совместных поездок
 */
@Entity
@Table(schema = "corporate", name = "shared_ride_settings_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(of = {"id"})
public class SharedRideSettingsItem implements HasId{
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "shared_ride_settings_item_position",
               schema = "corporate",
               joinColumns = @JoinColumn(name = "settings_item_id"),
               inverseJoinColumns = @JoinColumn(name = "position_id"))
    @Builder.Default
    private Set<Position> positions = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "shared_ride_settings_item_attribute",
               schema = "corporate",
               joinColumns = @JoinColumn(name = "settings_item_id"),
               inverseJoinColumns = @JoinColumn(name = "attribute_id"))
    @Builder.Default
    private Set<Attribute> attributes = new HashSet<>();
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "shared_ride_settings_item_employee",
               schema = "corporate",
               joinColumns = @JoinColumn(name = "settings_item_id"),
               inverseJoinColumns = @JoinColumn(name = "employee_id"))
    @Builder.Default
    private Set<Employee> employees = new HashSet<>();
}
