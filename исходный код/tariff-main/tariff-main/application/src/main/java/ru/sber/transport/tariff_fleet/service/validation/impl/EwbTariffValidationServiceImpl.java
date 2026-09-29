package ru.sber.transport.tariff_fleet.service.validation.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.tariff_fleet.exception.HaveActiveEwbException;
import ru.sber.transport.tariff_fleet.service.grpc.EwbGrpcService;
import ru.sber.transport.tariff_fleet.service.validation.EwbTariffValidationService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static ru.sber.transport.tariff_fleet.exception.HaveActiveEwbException.DEACTIVATE_TARIFF_ERR_MSG;

@Service
@RequiredArgsConstructor
public class EwbTariffValidationServiceImpl implements EwbTariffValidationService {


    private final EwbGrpcService ewbGrpcService;

    @Override
    public void validateActiveEwbExistence(List<UUID> departmentIds, LocalDate checkStartDate) {
        if (ewbGrpcService.haveActiveEwb(departmentIds, checkStartDate)) {
            throw new HaveActiveEwbException(DEACTIVATE_TARIFF_ERR_MSG);
        }
    }
}
