package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.limits.constants.LimitTransferHistoryType;
import ru.sberbank.ditsib.transport.limits.controller.v2.TransportTypeLimitController;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitReSharingByTransportTypeV2DTO;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.model.LimitData;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.Period;
import ru.sberbank.ditsib.transport.limits.service.DepLimitService;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;
import ru.sberbank.ditsib.transport.limits.service.LimitService;

import java.math.BigDecimal;
import java.util.Calendar;

@RequiredArgsConstructor
@RestController
@Scope("request")
class TransportTypeLimitControllerImpl extends BaseControllerImpl implements TransportTypeLimitController {

    private static final String CANNOT_SHARE_PREV_YEAR_ERROR = "Лимит за прошедшие годы не может быть перераспределен";

    private static final String CANNOT_USE_MY_LIMIT_ERROR =
            "Ошибка: галочку 'Использовать лимит моего подразделения' установить нельзя: есть дочерние лимиты";

    private final EmployeeService employeeService;

    private final DepLimitService depLimitService;

    private final LimitService limitService;

    @Override
    public void reShareLimitBetweenTransportTypes(LimitReSharingByTransportTypeV2DTO dto, JwtAuthenticationToken authentication) {
        var author = getEmployee(employeeService, authentication);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        var limitId = dto.limitId();
        var depLimit = depLimitService.get(limitId).orElseThrow(
                () -> new EntityNotFoundException(DepLimit.class, limitId));
        if (depLimit.getYear() < currentYear) {
            throw new LimitLogicException(CANNOT_SHARE_PREV_YEAR_ERROR);
        }
        // make transfer
        final var sum = dto.sum();
        if (sum.compareTo(BigDecimal.ZERO) < 0) {
            throw new LimitLogicException("Сумма не может быть меньше нуля!");
        }
        if (sum.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        var fromPeriod = dto.fromPeriod();
        var toPeriod = dto.toPeriod();
        var source = new LimitData(depLimit, dto.sourceTransportType(), Period.create(fromPeriod.name()));
        var target = new LimitData(depLimit, dto.targetTransportType(), Period.create(toPeriod.name()));
        limitService.transferSum(source, target, sum, author.getId(), LimitTransferHistoryType.GENERAL,
                false);
    }
}
