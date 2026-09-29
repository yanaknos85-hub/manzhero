package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.constant.ServiceType;
import ru.sber.transport.tariff_fleet.database.dao.ContractorRepository;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.dto.ContractorDto;
import ru.sber.transport.tariff_fleet.exception.ContractorNotFoundException;
import ru.sber.transport.tariff_fleet.mapper.ContractorMapper;
import ru.sber.transport.tariff_fleet.model.ContractorFilter;
import ru.sber.transport.tariff_fleet.service.ContractorService;
import ru.sber.transport.tariff_fleet.service.EmployeeService;
import ru.sber.transport.tariff_fleet.service.validation.ContractValidationService;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContractorServiceImpl implements ContractorService {

    private final ContractorRepository repository;
    private final ContractorMapper mapper;
    private final EmployeeService employeeService;
    private final ContractValidationService contractValidationService;

    @Override
    public Optional<Contractor> get(UUID id) {
        return repository.findById(id);
    }

    @Override
    public Contractor getById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ContractorNotFoundException(id));
    }

    @Override
    @Transactional
    public void delete(Contractor entity) {
        repository.save(entity.setActive(false));
    }

    @Override
    @Transactional
    public void save(Contractor entity) {
        repository.save(entity);
    }

    @Override
    public List<ContractorDto> getAllActive() {
        return repository.findAllByServiceTypeAndActiveTrueOrderByNameAsc(ServiceType.AUTOSERVICE).stream()
                .map(mapper::contractorToContractorDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractorDto> findAllOrganizationsByActiveContractsAndServiceTypeAndDocumentType(
            ServiceType serviceType, DocumentType documentType) {
        contractValidationService.validateFindByDocumentType(documentType);
        return findAllByFilter(ContractorFilter.builder()
                .active(true)
                .serviceType(serviceType)
                .documentType(documentType)
                .selfOnly(false)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractorDto> findSelfOrganizationsByActiveContractsAndServiceTypeAndDocumentType(
            ServiceType serviceType, DocumentType documentType, UUID userId) {
        contractValidationService.validateFindByDocumentType(documentType);
        var employee = employeeService.getByUserId(userId);
        return findAllByFilter(ContractorFilter.builder()
                .active(true)
                .serviceType(serviceType)
                .documentType(documentType)
                .selfOnly(true)
                .organizationId(employee.getOrganization().getId())
                .build());

    }

    private List<ContractorDto> findAllByFilter(ContractorFilter filter) {
        return repository.findContractorsByFilter(filter).stream()
                .map(mapper::contractorToContractorDto)
                .sorted(Comparator.comparing(ContractorDto::name))
                .toList();
    }

}
