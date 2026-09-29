package ru.sberbank.ditsib.transport.limits.controller.v2.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.limits.controller.v2.LimitSharingPercentsController;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitSharingPercentsV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitSharingPercentsV2DTO;
import ru.sberbank.ditsib.transport.limits.exceptions.LimitLogicException;
import ru.sberbank.ditsib.transport.limits.mapper.LimitSharingPercentsMapper;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharingPercents;
import ru.sberbank.ditsib.transport.limits.service.DepLimitService;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;
import ru.sberbank.ditsib.transport.limits.service.LimitSharingPercentService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController("LimitSharingPercentsControllerV2Impl")
@Scope("request")
class LimitSharingPercentsControllerImpl implements LimitSharingPercentsController {

    private final LimitSharingPercentService limitSharingPercentService;

    private final DepLimitService depLimitService;

    private final EmployeeService employeeService;

    private final LimitSharingPercentsMapper limitSharingPercentsMapper;

    @Override
    public GetLimitSharingPercentsV2DTO add(LimitSharingPercentsV2DTO limitSharingPercentsDTO, JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        var author = employeeService.getByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of("userId", userId)));

        var limitId = limitSharingPercentsDTO.limitId();
        var depLimit = depLimitService.get(limitId).orElseThrow(() -> new EntityNotFoundException(Limit.class, limitId));

        if (depLimit.getParent() != null) {
            throw new LimitLogicException("Процентное распределение может быть прикреплено только к головному лимиту");
        }

        var oldPercents = limitSharingPercentService.getByLimit(depLimit);
        if (oldPercents.isPresent()) {
            throw new LimitLogicException("Процентное распределение для данного лимита уже существует");
        }

        LimitSharingPercents limitSharingPercents = transformDTOToEntity(limitSharingPercentsDTO);
        limitSharingPercents.setAuthor(author);
        limitSharingPercents.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
        limitSharingPercents.setLimit(depLimit);
        limitSharingPercents = limitSharingPercentService.add(limitSharingPercents);

        return limitSharingPercentsMapper.toV2Dto(limitSharingPercents);
    }

    @Override
    public void edit(UUID limitSharingPercentsId, LimitSharingPercentsV2DTO limitSharingPercentsDTO, JwtAuthenticationToken authentication) {
        var limitSharingPercents = limitSharingPercentService.get(limitSharingPercentsId)
                .orElseThrow(() -> new EntityNotFoundException(LimitSharingPercents.class, limitSharingPercentsId));

        limitSharingPercentsMapper.update(limitSharingPercents, limitSharingPercentsDTO);
        limitSharingPercentService.save(limitSharingPercents);
    }

    @Override
    public void delete(UUID limitSharingPercentsId, JwtAuthenticationToken authentication) {
        var limitSharingPercents = limitSharingPercentService.get(limitSharingPercentsId).orElseThrow(
                () -> new EntityNotFoundException(LimitSharingPercents.class, limitSharingPercentsId));
        limitSharingPercentService.delete(limitSharingPercents);
    }

    @Override
    public GetLimitSharingPercentsV2DTO get(UUID limitSharingPercentsId) {
        var limitSharingPercents = limitSharingPercentService.get(limitSharingPercentsId).orElseThrow(
                () -> new EntityNotFoundException(LimitSharingPercents.class, limitSharingPercentsId));
        return limitSharingPercentsMapper.toV2Dto(limitSharingPercents);
    }

    @Override
    public Page<GetLimitSharingPercentsV2DTO> getAll(Integer page, Integer size, Sort.Direction direction, UUID limitId) {
        Page<LimitSharingPercents> list = limitSharingPercentService.getAll(page, size, direction, limitId);
        return list.map(limitSharingPercentsMapper::toV2Dto);
    }

    private LimitSharingPercents transformDTOToEntity(LimitSharingPercentsV2DTO limitSharingPercentsDTO) {
        if (limitSharingPercentsDTO == null) {
            return null;
        }

        var limitId = limitSharingPercentsDTO.limitId();
        var depLimit = depLimitService.get(limitId)
                .orElseThrow(() -> new EntityNotFoundException(Limit.class, limitId));

        var limitSharingPercents = new LimitSharingPercents();
        limitSharingPercentsMapper.update(limitSharingPercents, limitSharingPercentsDTO);
        limitSharingPercents.setLimit(depLimit);
        return limitSharingPercents;
    }
}
