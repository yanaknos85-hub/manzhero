package ru.sberbank.ditsib.corpclient.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.dto.OrganizationField;
import ru.sberbank.ditsib.corpclient.dto.OrganizationProjection;
import ru.sberbank.ditsib.corpclient.dto.OrganizationSelectDTO;
import ru.sberbank.ditsib.corpclient.mapper.OrganizationMapper;
import ru.sberbank.ditsib.corpclient.service.OrganizationControllerService;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;
import ru.sberbank.ditsib.corpclient.util.SortUtils;
import ru.sberbank.ditsib.request.Direction;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Реализация контроллера сервиса организаций.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class OrganizationControllerServiceImpl implements OrganizationControllerService {
    
    private final OrganizationService service;
    
    private final OrganizationMapper mapper;
    
    @Override
    @Transactional
    public void edit(UUID id, Organization newData) {
        service.edit(id, newData);
    }
    
    @Override
    public void delete(UUID orgId) {
        service.delete(orgId);
    }
    
    @Override
    @Transactional
    public Organization get(UUID organizationId) {
        return service.get(organizationId);
    }
    
    @Override
    @Transactional
    public Iterable<OrganizationSelectDTO> get(int page, int size, Direction direction,
                                               OrganizationField field, Map<OrganizationField, Serializable> filter, OrganizationProjection projection) {
        if (projection == null) {
            projection = OrganizationProjection.FULL;
        }
        return switch (projection) {
            case FULL -> service.get(page, size, SortUtils.createSort(direction, field), filter).map(mapper::toDto);
            case SELECT -> service.get(SortUtils.createSort(direction, field), filter).stream().map(mapper::toSelectDto).collect(Collectors.toList());
        };
    }
    
    @Override
    public boolean validateOrganizationId(@NotNull UUID organizationId) {
        return service.validateOrganizationId(organizationId);
    }
}

