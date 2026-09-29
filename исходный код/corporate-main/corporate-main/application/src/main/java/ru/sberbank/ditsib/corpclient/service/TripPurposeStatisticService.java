package ru.sberbank.ditsib.corpclient.service;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public interface TripPurposeStatisticService {
    /**
     * add tripPurpose to statistic
     *
     * @param employeeId ID of trip employee.
     * @param tripPurposeId ID of trip purpose.
     */
    void addTripPurposeToStatistic(@NotNull UUID employeeId, @NotNull UUID tripPurposeId);
}
