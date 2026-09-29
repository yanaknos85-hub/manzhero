package ru.sberbank.ditsib.transport.limits.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.limits.controller.LimitHistoryController;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitHistoryDTO;
import ru.sberbank.ditsib.transport.limits.mapper.VersionConverter;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of limit controller service.
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@Scope("request")
class LimitHistoryControllerImpl implements LimitHistoryController {

    private final ru.sberbank.ditsib.transport.limits.controller.v2.LimitHistoryController controller;
    
    private final VersionConverter versionConverter;
    
    @Override
    public List<GetLimitHistoryDTO> getAll() {
        return controller.getAll(null, 0, Integer.MAX_VALUE, Sort.Direction.ASC).map(versionConverter::toV1)
                .getContent();
    }
    
    @Override
    public List<GetLimitHistoryDTO> getByLimit(UUID limitId, Integer maxRecords) {
        return controller.getAll(limitId, 0, maxRecords, Sort.Direction.ASC).map(versionConverter::toV1)
                .getContent();
    }
}