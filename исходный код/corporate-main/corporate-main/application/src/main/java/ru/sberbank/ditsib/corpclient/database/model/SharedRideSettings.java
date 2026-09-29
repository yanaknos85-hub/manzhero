package ru.sberbank.ditsib.corpclient.database.model;

import lombok.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.validation.HasId;

import jakarta.persistence.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Сущность БД - Настройки совместных поездок.
 * Включают в себя настройки индикации экономии.
 * Существует три уровня экономии:
 * <ul><li>минимальная (красный, от 0% до economyIndicationYellowRangeLowerBorder)</li>
 *     <li>средняя (желтый, от economyIndicationYellowRangeLowerBorder до economyIndicationYellowRangeUpperBorder)</li>
 *     <li>максимальная (зеленый, от economyIndicationYellowRangeUpperBorder до 100%)</li></ul>
 */
@Entity
@Table(
        schema = "corporate",
        name = "shared_ride_settings",
        uniqueConstraints = { @UniqueConstraint( columnNames = { "transport_type", "organization_id" } ) }
        )
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedRideSettings implements HasId {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type", nullable = false)
    private TransportTypeEnum transportType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;
    
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "shared_ride_settings_id", referencedColumnName = "id")
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "setting_type", nullable = true)
    private Map<SharedRideSettingType, SharedRideSettingsItem> settings = new EnumMap<>(SharedRideSettingType.class);
    
    @Column(name = "yellow_range_lower_border", nullable = false, scale = 3)
    @Builder.Default
    private int economyIndicationYellowRangeLowerBorder = 30;
    
    @Column(name = "yellow_range_upper_border", nullable = false, scale = 3)
    @Builder.Default
    private int economyIndicationYellowRangeUpperBorder = 70;
}
