package ru.sber.transport.trip_reports.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import ru.sberbank.ditsib.transport.messaging.Message;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Jacksonized
@SuperBuilder
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TripReportMessage implements Message<UUID> {

    private UUID id;

    private UUID contractorId;

    private String tripHumanReadableId;

    private String requestIds;

    private String timeZone;

    private OffsetDateTime expectedStartTime;

    private OffsetDateTime factStartTime;

    private String routeStart;

    private String routeWaypoints;

    private String routeEnd;

    private Integer checkinQuantity;

    private String checkinStatuses;

    private String checkinWaypointConfirmation;

    private String status;

    private String comment;

    private String passenger;

    private String author;

    private String vehicleNumber;

    private String driver;

    private OffsetDateTime creationTime;

    private Double expectedDistance;

    private Duration expectedTime;

    private Duration expectedWaitTime;

    private Double expectedCost;

    private Double factDistance;

    private Duration factTime;

    private Duration factWaitTime;

    private String tripType;

    private String dispatcherName;

    private String dispatcherPhone;

    private String taxiClass;

    private int passengerCount;

    @Override
    public UUID getId() {
        return id;
    }
}