package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.database.dao.OrganizationRepository;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.dto.GetAllActiveOrganizationNamesDto;
import ru.sber.transport.tariff_fleet.dto.GetDepartmentsInfo;
import ru.sber.transport.tariff_fleet.dto.OrganizationsDepartmentsSearchDto;
import ru.sber.transport.tariff_fleet.exception.DigitIdNotFoundException;
import ru.sber.transport.tariff_fleet.mapper.OrganizationMapper;
import ru.sber.transport.tariff_fleet.service.OrganizationService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.groupingBy;

/**
 * Implementation of service for working with organization.
 */
@RequiredArgsConstructor
@Service
public class OrganizationServiceImpl implements OrganizationService {
    
    private final OrganizationRepository repository;
    private final OrganizationMapper mapper;
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> get(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    @Transactional
    public void delete(Organization entity) {
        repository.save(entity.setActive(false));
    }
    
    @Override
    @Transactional
    public void saveOrUpdate(Organization entity) {
        var dbEntityOptional = repository.findById(entity.getId());
        if (dbEntityOptional.isPresent()) {
            repository.save(dbEntityOptional.get()
                                            .setActive(true)
                                            .setOfficialName(entity.getOfficialName())
                                            .setDigitId(entity.getDigitId()));
        } else {
            repository.save(entity);
        }
    }
    
    @Override
    public List<GetAllActiveOrganizationNamesDto> getAllActiveNames() {
        return repository.findAllByActiveTrueOrderByOfficialName().stream()
                         .map(mapper::organizationToGetAllActiveOrganizationNamesDto)
                         .toList();
    }
    
    @Override
    public List<GetDepartmentsInfo> getDepartmentsInfo(OrganizationsDepartmentsSearchDto dto) {
        return repository.findByIdsWithActiveDepartments(dto.organizationId()).stream()
                         .collect(groupingBy(value -> Map.entry(value.organizationId(), value.organizationName())))
                         .entrySet()
                         .stream()
                         .map(entry -> new GetDepartmentsInfo(entry.getKey().getValue(),
                                                              entry.getValue().stream()
                                                                   .map(mapper::organizationWithDepartmentIntoDto)
                                                                   .toList()))
                         .toList();
    }

    @Override
    @Transactional
    public Long getDigitIdByUserId(UUID userId) {
        return repository.findDigitIdByUserId(userId).orElseThrow(() -> new DigitIdNotFoundException(userId));
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }
}
