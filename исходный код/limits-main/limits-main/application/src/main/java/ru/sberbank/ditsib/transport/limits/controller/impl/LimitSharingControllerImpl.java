package ru.sberbank.ditsib.transport.limits.controller.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.limits.controller.LimitSharingController;
import ru.sberbank.ditsib.transport.limits.mapper.LimitSharingMapper;
import ru.sberbank.ditsib.transport.limits.mapper.VersionConverter;
import ru.sberbank.ditsib.transport.limits.model.GetLimitSharingDTO;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.service.LimitService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of limit controller service.
 */
@RestController
@Transactional
@Scope("request")
class LimitSharingControllerImpl extends BaseController implements LimitSharingController {
    
    private final LimitSharingService limitSharingService;
    
    private final LimitService limitService;

    private final ru.sberbank.ditsib.transport.limits.controller.v2.LimitSharingController controller;

    private final VersionConverter versionConverter;

    public LimitSharingControllerImpl(LimitSharingMapper limitSharingMapper, LimitSharingService limitSharingService, LimitService limitService, ru.sberbank.ditsib.transport.limits.controller.v2.LimitSharingController controller, VersionConverter versionConverter) {
        super(limitSharingMapper);
        this.limitSharingService = limitSharingService;
        this.limitService = limitService;
        this.controller = controller;
        this.versionConverter = versionConverter;
    }

    @Override
    public GetLimitSharingDTO get(UUID limitSharingId) {
        return versionConverter.toV1(controller.get(limitSharingId));
    }
    
    @Override
    public List<GetLimitSharingDTO> getAll() {
        return controller.getAll(0 ,Integer.MAX_VALUE, Sort.Direction.ASC, null)
                .map(versionConverter::toV1)
                .getContent();
    }
    
    @Override
    public List<GetLimitSharingDTO> getAllFull() {
        return controller.getAll(0 ,Integer.MAX_VALUE, Sort.Direction.ASC, null)
                .map(versionConverter::toV1)
                .getContent();
    }
    
    @Override
    public List<GetLimitSharingDTO> getByLimit(UUID limitId) {
        return controller.getAll(0 ,Integer.MAX_VALUE, Sort.Direction.ASC, limitId)
                .map(versionConverter::toV1)
                .getContent();
    }
    
    @Override
    public List<GetLimitSharingDTO> getByLimitFull(UUID limitId) {
        return controller.getAll(0 ,Integer.MAX_VALUE, Sort.Direction.ASC, limitId)
                .map(versionConverter::toV1)
                .getContent();
    }
    
    @Override
    public List<Long> getSharingImitation(UUID limitId, Long totalSum) {
        var limit = limitService.get(limitId).orElseThrow(() -> new EntityNotFoundException(Limit.class, limitId));
        return limitSharingService.distributeSharingPerPeriodImitation(limit, BigDecimal.valueOf(totalSum).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN))
                .entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().ordinal()))
                .map(Map.Entry::getValue)
                .map(it -> it.multiply(BigDecimal.valueOf(100)).longValue())
                .toList();
    }
    
}
