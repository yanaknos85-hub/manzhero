package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.limits.controller.v2.LimitSharingController;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitSharingV2DTO;
import ru.sberbank.ditsib.transport.limits.mapper.LimitSharingMapper;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController("LimitSharingControllerV2Impl")
@Scope("request")
@Transactional
class LimitSharingControllerImpl implements LimitSharingController {

    private final LimitSharingService limitSharingService;

    private final LimitSharingMapper limitSharingMapper;

    @Override
    public GetLimitSharingV2DTO get(UUID limitSharingId) {
        var limitSharing = limitSharingService.get(limitSharingId).orElseThrow(() -> new EntityNotFoundException(LimitSharing.class, limitSharingId));
        return limitSharingMapper.toV2Dto(limitSharing);
    }

    @Override
    public Page<GetLimitSharingV2DTO> getAll(Integer page, Integer size, Sort.Direction direction, UUID limitId) {
        var list = limitSharingService.getAll(page, size, direction, limitId);
        return list.map(limitSharingMapper::toV2Dto);
    }
}
