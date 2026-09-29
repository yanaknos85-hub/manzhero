package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.corpclient.database.dao.TripPurposeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.TripPurposeStatisticRepository;
import ru.sberbank.ditsib.corpclient.database.model.TripPurposeStatistic;
import ru.sberbank.ditsib.corpclient.service.TripPurposeStatisticService;

import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Implementation of trip purpose statistic service
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TripPurposeStatisticServiceImpl implements TripPurposeStatisticService {
    private final TripPurposeStatisticRepository tripPurposeStatisticRepository;
    private final TripPurposeRepository tripPurposeRepository;

    @Override
    public void addTripPurposeToStatistic(@NotNull UUID employeeId, @NotNull UUID tripPurposeId) {
        tripPurposeRepository.findById(tripPurposeId)
                .ifPresent(tripPurpose -> {
                    final var purposeStatistic =
                            tripPurposeStatisticRepository.findByEmployeeAndTripPurposeId(employeeId, tripPurposeId)
                                    .orElse(TripPurposeStatistic.builder().employee(employeeId).tripPurpose(tripPurpose).build());

                    purposeStatistic.setUsages(purposeStatistic.getUsages() + 1);
                    tripPurposeStatisticRepository.save(purposeStatistic);
                });
    }
}
