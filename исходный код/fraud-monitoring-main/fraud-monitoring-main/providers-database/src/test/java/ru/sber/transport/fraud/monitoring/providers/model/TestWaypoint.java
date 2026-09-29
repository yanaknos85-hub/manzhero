package ru.sber.transport.fraud.monitoring.providers.model;

import ru.sber.transport.fraud.monitoring.model.Waypoint;

import java.util.UUID;


public record TestWaypoint(
        UUID id,
        UUID tripRequestId,
        String country,
        String region,
        String city,
        String house,
        String street,
        String structure,
        String building,
        Integer orderingIndex,
        Long waitTime,
        String address
) implements Waypoint {

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public UUID getTripRequestId() {
        return tripRequestId;
    }

    @Override
    public String getCountry() {
        return country;
    }

    @Override
    public String getRegion() {
        return region;
    }

    @Override
    public String getCity() {
        return city;
    }

    @Override
    public String getStreet() {
        return street;
    }

    @Override
    public String getHouse() {
        return house;
    }

    @Override
    public String getStructure() {
        return structure;
    }

    @Override
    public String getBuilding() {
        return building;
    }

    @Override
    public Integer getOrderingIndex() {
        return orderingIndex;
    }

    @Override
    public Long getWaitTime() {
        return waitTime;
    }

    @Override
    public String getAddress() {
        return address;
    }
}