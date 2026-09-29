package ru.sber.transport.limits.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.limits.business.Reserves;
import ru.sber.transport.limits.business.exceptions.ReserveNotFoundException;
import ru.sber.transport.limits.business.exceptions.ReserveNotSufficientException;
import ru.sber.transport.limits.business.exceptions.ServiceNotAvailableException;
import ru.sber.transport.limits.business.exceptions.TypeNotAvailableException;
import ru.sber.transport.limits.model.Reserve;
import ru.sber.transport.limits.providers.*;

import java.util.UUID;

import static ru.sber.transport.limits.model.ReserveStatus.CANCELED;
import static ru.sber.transport.limits.model.ReserveStatus.SPENT;

@Slf4j
@Transactional
@RequiredArgsConstructor
public class ReservesImpl implements Reserves {

    private final Spendings spendings;

    private final Employees employees;

    private final Services services;

    private final Types types;

    private final PeriodSharings periodSharings;

    @Override
    public void reserve(Reserve data) throws ReserveNotSufficientException, TypeNotAvailableException, ServiceNotAvailableException {
        final var consumerId = data.consumerId();
        final var actionCost = data.cost();
        final var actionId = data.id();
        final var serviceName = data.service();
        final var typeName = data.type();

        log.debug("Reserving {} for {} for action {}", actionCost, consumerId, actionId);
        final var employee = employees.get(consumerId);
        final var service = services.get(serviceName).orElseThrow(() -> new ServiceNotAvailableException(serviceName));
        final var type = types.get(typeName).orElseThrow(() -> new TypeNotAvailableException(typeName));
        final var periodSharing = periodSharings.get(service, type, data.date(), employee, true)
                .or(() -> periodSharings.get(service, type, data.date(), employee, false))
                .orElseThrow(ReserveNotSufficientException::new);
        var available = periodSharing.remains();
        final var reserve = spendings.get(actionId);
        if (reserve.isPresent()) {
            available = available.add(reserve.get().reserved());
        }
        if (available.compareTo(actionCost) < 0) {
            log.warn("Not enough money. Available: {}, Needed: {}", available, actionCost);
            throw new ReserveNotSufficientException();
        }
        periodSharings.update(periodSharing, available.subtract(actionCost));
        spendings.create(periodSharing, data);
        log.debug("Reserving {} for {} for action {} succeeded", actionCost, consumerId, actionId);
    }

    @Override
    public void cancel(UUID uuid) {
        spendings.get(uuid)
                .ifPresentOrElse(it -> {
                    final var periodSharing = periodSharings.get(it.periodSharing()).orElseThrow();
                    periodSharings.update(periodSharing, periodSharing.remains().add(it.reserved()));
                    spendings.update(it, CANCELED);
                }, ReserveNotFoundException::new);
    }

    @Override
    public void confirm(UUID uuid) throws ReserveNotFoundException {
        spendings.update(spendings.get(uuid).orElseThrow(ReserveNotFoundException::new), SPENT);
    }
}
