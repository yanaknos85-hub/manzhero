package ru.sberbank.ditsib.transport.approvals.database.model;

import lombok.*;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.GeoZone;

import jakarta.persistence.*;

/**
 * Элемент настроек по региону и целям поездки
 */
@Getter
@Setter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PurposeAndRegionApprovalSettingsItem {
    
    /**
     * Ссылка на цель поездки
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "trip_purpose_id", referencedColumnName = "id", nullable = false)
    private TripPurpose tripPurpose;
    
    /**
     * Геозона. В случае, если она не указана, действие настройки распространяется на все регионы
     */
    @JoinColumn(name = "region", referencedColumnName = "id")
    @ManyToOne(fetch = FetchType.EAGER)
    private GeoZone region;
    
    /**
     * Сумма, не требующая согласования
     */
    @Builder.Default
    @Column(name = "min_cost_to_be_approved")
    private long minCostToBeApproved = 0;
}
