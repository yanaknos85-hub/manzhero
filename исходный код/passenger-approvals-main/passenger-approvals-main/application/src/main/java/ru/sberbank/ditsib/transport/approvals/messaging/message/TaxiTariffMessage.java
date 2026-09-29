package ru.sberbank.ditsib.transport.approvals.messaging.message;

import lombok.*;
import ru.sber.transport.messaging.Message;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaxiTariffMessage implements Message<UUID> {
    
    private UUID id;
    
    private String contractorTariffId;
    
    private String humanReadableId;
    
    private String serviceType;
    
    /** ID геозоны */
    private UUID regionId;
    
    private UUID organizationId;
    
    private String transportType;
    
    private UUID contractId;
    
    private UUID contractorId;
    
    private boolean active = true;
    
    private String taxiClass;
    
    private Integer rideCostPerKm;
    
    private Integer minRideDistanceCost = 0;
    
    private Integer rideCostPerMin;
    
    private Integer minRideTimeCost = 0;
    
    private Integer waitCostPerMin;
    
    private Integer carServiceCost;
    
    private Integer waitCostPerMinIntermediate;
    
    private boolean deleted;
    
    private Double distanceIncluded = 0d;
    private Integer timeIncluded = 0;
    private Integer freeWaitingTime = 0;
    
    private Integer maxDiffComputedDistancePercent;
    private Integer maxDiffFactDistancePercent;
    private Integer maxDiffComputedCostPercent;
    private Integer maxDiffContractorCostPercent;
    private Integer maxDiffComputedWaitingPercent;
    
    private Double coefWorkDayMorning = 1d;
    private Double coefWorkDayNoon = 1d;
    private Double coefWorkDayEvening = 1d;
    private Double coefWorkDayNight = 1d;
    private Double coefDayOff = 1d;
    
    private Double savingsDeviationPct = 0d;
    private Double distanceDeviationKm = 0d;
    private Integer timeDeviationMin = 0;
    private Integer minCancelTimeMin = 30;
    
    private String workGroup;
    
    @Builder.Default
    private Integer triggerTime = 60;
    
}
