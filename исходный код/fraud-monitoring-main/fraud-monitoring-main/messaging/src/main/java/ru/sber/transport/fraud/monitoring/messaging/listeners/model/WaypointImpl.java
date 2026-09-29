package ru.sber.transport.fraud.monitoring.messaging.listeners.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.fraud.monitoring.model.Waypoint;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WaypointImpl implements Waypoint {
    
    private UUID id;

    private UUID tripRequestId;

    private String country;

    private String region;

    private String city;

    private String street;

    private String house;

    private String structure;

    private String building;

    private Integer orderingIndex;

    private Long waitTime;

    private String address;
}
