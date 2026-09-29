package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.controller.v2.LimitController;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitResharingV2DTO;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;
import ru.sberbank.ditsib.transport.limits.service.DepLimitService;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;
import ru.sberbank.ditsib.transport.limits.service.LimitService;

import java.math.BigDecimal;
import java.util.Calendar;

@RequiredArgsConstructor
@RestController
@Transactional
@Scope("request")
class LimitControllerV2Impl extends BaseControllerImpl implements LimitController {

    private static final String CANNOT_SHARE_PREV_YEAR_ERROR = "Лимит за прошедшие годы не может быть перераспределен";

    private final DepLimitService depLimitService;

    private final LimitService limitService;

    private final EmployeeService employeeService;

    @Override
    public void reShareLimit(LimitResharingV2DTO dto, JwtAuthenticationToken authentication, String sourceService) {
        final var author = getEmployee(employeeService, authentication);
        final int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        final var sourceDepLimit =
                depLimitService.get(dto.sourceLimitId()).orElseThrow(() -> new EntityNotFoundException(Limit.class, dto.sourceLimitId()));
        // get source limit by department
        var targetDepLimit =
                depLimitService.get(dto.targetLimitId()).orElseThrow(() -> new EntityNotFoundException(Limit.class, dto.targetLimitId()));

        if (sourceDepLimit.getYear() < currentYear) {
            throw new LimitLogicException(CANNOT_SHARE_PREV_YEAR_ERROR);
        }
        if (sourceDepLimit.getYear() != targetDepLimit.getYear()) {
            throw new LimitLogicException("Год лимитов должен совпадать");
        }

        // make transfer
        final var sum = dto.sum();
        if (sum.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        final var fromPeriod = dto.fromPeriod();
        final var toPeriod = dto.toPeriod();
        final var source = new LimitData(
                sourceDepLimit,
                dto.sourceTransportType(),
                Period.create(fromPeriod.name())
        );
        final var target = new LimitData(
                targetDepLimit,
                dto.targetTransportType(),
                Period.create(toPeriod.name())
        );
        limitService.transferSum(source, target,
                sum, author.getId(),
                LimitTransferHistoryType.GENERAL, false);
    }
}
