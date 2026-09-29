package ru.sberbank.ditsib.corpclient.service.impl;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.dto.mapper.PurposeMapper;
import ru.sberbank.ditsib.corpclient.dto.purpose.NewTripPurposeDTO;
import ru.sberbank.ditsib.corpclient.dto.purpose.TripPurposeDTO;
import ru.sberbank.ditsib.corpclient.messaging.sender.PurposeSender;
import ru.sberbank.ditsib.corpclient.service.TripPurposeService;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of trip purpose service
 */
@Service
@Transactional
@RequiredArgsConstructor
class TripPurposeServiceImpl implements TripPurposeService {
    private final TripPurposeRepository repository;
    private final TripPurposeAttributeRepository tripPurposeAttributeRepository;
    private final TripPurposeDepartmentRepository tripPurposeDepartmentRepository;
    private final TripPurposeDateRepository tripPurposeDateRepository;
    private final TripPurposeTimeRepository tripPurposeTimeRepository;
    private final TripPurposeWeekdayRepository tripPurposeWeekdayRepository;

    private final OrganizationRepository organizationRepository;
    private final AttributeRepository attributeRepository;
    private final DepartmentRepository departmentRepository;

    private final PurposeSender purposeSender;

    private final PurposeMapper mapper;

    private final EmployeeRepository employeeRepository;
    
    @Override
    public TripPurposeDTO get(UUID uuid) {
        return mapper.tripPurposeToDTO(repository.findById(uuid).orElseThrow(
                () -> new EntityNotFoundException(TripPurpose.class, uuid)));
    }
    
    @Override
    public Optional<TripPurpose> get(String name, UUID id) {
        return repository.findByLabelAndOrganization(name, id);
    }

    @Override
    public TripPurposeDTO updatePurpose(UUID organizationId, UUID purposeId, @NotNull NewTripPurposeDTO source) {
        var tripPurpose = mapper.newDTOToTripPurpose(source, organizationId);

        if (tripPurpose.getTripPurposeAttributes() != null) {
            updateAttributes(tripPurpose);
        }

        if (tripPurpose.getTripPurposeDepartments() != null) {
            updatePurposeDepartments(tripPurpose);
        }

        checkInterval(tripPurpose);

        tripPurpose.setPurposeParentLabel(UUID.randomUUID().toString());
        TripPurpose purposeUpdate = null;
        if (purposeId != null) {
            purposeUpdate = repository.findById(purposeId).
                    orElseThrow(() -> new EntityNotFoundException(TripPurpose.class, purposeId));

            purposeUpdate.setActive(false);
            repository.save(purposeUpdate);

            tripPurpose.setPurposeParentLabel(purposeUpdate.getPurposeParentLabel());
        }
        tripPurpose.setIcon(source.getIcon());

        Optional<Organization> optionalOrganization = organizationRepository.findById(organizationId);
        if(optionalOrganization.isEmpty()) {
            throw new EntityNotFoundException(Organization.class, source.getOrganization());
        }

        Optional<TripPurpose> presentPurpose = repository.findByLabelAndOrganization(source.getLabel(), source.getOrganization());
        if (presentPurpose.isPresent()) {
            throw new DuplicateDataException(TripPurpose.class, "label", source.getLabel());
        }

        repository.save(tripPurpose);

        if (tripPurpose.getTripPurposeAttributes() != null) {
            tripPurpose.getTripPurposeAttributes().forEach(attribute -> attribute.setTripPurpose(tripPurpose));
            tripPurposeAttributeRepository.saveAll(tripPurpose.getTripPurposeAttributes());
        }

        if (tripPurpose.getTripPurposeDepartments() != null) {
            tripPurpose.getTripPurposeDepartments().forEach(attribute -> attribute.setTripPurpose(tripPurpose));
            tripPurposeDepartmentRepository.saveAll(tripPurpose.getTripPurposeDepartments());
        }

        if (tripPurpose.getTripPurposeDates() != null) {
            tripPurpose.getTripPurposeDates().forEach(attribute -> attribute.setTripPurpose(tripPurpose));
            tripPurposeDateRepository.saveAll(tripPurpose.getTripPurposeDates());
        }

        if (tripPurpose.getTripPurposeTimes() != null) {
            tripPurpose.getTripPurposeTimes().forEach(attribute -> attribute.setTripPurpose(tripPurpose));
            tripPurposeTimeRepository.saveAll(tripPurpose.getTripPurposeTimes());
        }

        if (tripPurpose.getTripPurposeWeekdays() != null) {
            tripPurpose.getTripPurposeWeekdays().forEach(attribute -> attribute.setTripPurpose(tripPurpose));
            tripPurposeWeekdayRepository.saveAll(tripPurpose.getTripPurposeWeekdays());
        }

        Optional.ofNullable(purposeUpdate).ifPresent(purposeSender::send);
        purposeSender.send(tripPurpose);

        return mapper.tripPurposeToDTO(tripPurpose);
    }

    private void checkInterval(TripPurpose tripPurpose) {
        if (tripPurpose.getTripPurposeDates() != null && !tripPurpose.getTripPurposeDates().isEmpty()
                && tripPurpose.getTripPurposeDates().stream().
                noneMatch(purposeDates ->
                        purposeDates.getStartDate().compareTo(purposeDates.getEndDate()) <= 0)) {
            throw new IllegalStateResponseException("End date cannot be less than start date");
        }
    }

    private void updatePurposeDepartments(TripPurpose tripPurpose) {
        for (var tripPurposeDepartment : tripPurpose.getTripPurposeDepartments()) {
            if (tripPurposeDepartment.getDepartment() == null) {
                continue;
            }
            var id = tripPurposeDepartment.getDepartment().getId();
            if (id == null) {
                throw new IllegalStateResponseException("Department id cannot be null or empty");
            }
            if (!departmentRepository.existsById(id)) {
                throw new EntityNotFoundException(Department.class, id);
            }
        }
    }

    private void updateAttributes(TripPurpose tripPurpose) {
        for (var tripPurposeAttribute : tripPurpose.getTripPurposeAttributes()) {
            if (tripPurposeAttribute.getAttribute() == null) {
                continue;
            }

            var id = tripPurposeAttribute.getAttribute().getId();
            if (id == null) {
                throw new IllegalStateResponseException("Attribute id cannot be null or empty");
            }
            var attributeById = attributeRepository.existsById(id);
            if (!attributeById) {
                throw new EntityNotFoundException(Attribute.class, id);
            }
        }
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<TripPurposeDTO> getActivePurpose(UUID organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new EntityNotFoundException(Organization.class, organizationId);
        }
        return repository.findAllByOrganizationAndActive(organizationId).stream().map(mapper::tripPurposeToDTO).toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<TripPurposeDTO> getAllPurposes(UUID organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new EntityNotFoundException(Organization.class, organizationId);
        }
        return repository.findAllByOrganization(organizationId).stream().map(mapper::tripPurposeToDTO).toList();
    }

    @Override
    public TripPurposeDTO getTripPurpose(@NotNull UUID uuid, @NotNull UUID organization) throws EntityNotFoundException{
        if (!organizationRepository.existsById(organization)) {
            throw new EntityNotFoundException(Organization.class, organization);
        }

        var tripPurpose = repository.findByIdAndOrganization(uuid, organization).
                orElseThrow(() -> new EntityNotFoundException(TripPurpose.class, uuid));

        return mapper.tripPurposeToDTO(tripPurpose);
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<TripPurposeDTO> findAllByPurposeLike(String searchString, @NotNull UUID organization) {
        if (!organizationRepository.existsById(organization)) {
            throw new EntityNotFoundException(Organization.class, organization);
        }

        return repository.findAllByLabelLike(searchString, organization).stream().map(mapper::tripPurposeToDTO)
                         .toList();
    }

    @Override
    public List<TripPurposeDTO> findAllByAttributes(UUID userId, @NotNull UUID organizationId) {
        if (!organizationRepository.existsById(organizationId)) {
            throw new EntityNotFoundException(Organization.class, organizationId);
        }
        var employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of("userId", userId)));
        var attributes = employee.getAttributes();
        if (attributes == null || attributes.isEmpty()) {
            return repository.findAllByEmptyAttributes(organizationId).stream()
                    .map(mapper::tripPurposeToDTO)
                    .toList();
        } else {
            return repository.findAllByAttributesAndStatistic(attributes.stream()
                                    .map(Attribute::getId)
                                    .collect(Collectors.toSet()),
                            organizationId,
                            employee.getId()).stream()
                    .map(mapper::tripPurposeToDTO)
                    .collect(Collectors.toCollection(LinkedList::new));
        }
    }

    @Override
    public void deletePurpose(@NotNull UUID purposeId, @NotNull UUID organization) {
        if (!organizationRepository.existsById(organization)) {
            throw new EntityNotFoundException(Organization.class, organization);
        }

        var tripPurpose = repository.findByIdAndOrganization(purposeId, organization).
                orElseThrow(() -> new EntityNotFoundException(TripPurpose.class, purposeId));

        tripPurpose.setActive(false);
        repository.save(tripPurpose);

        purposeSender.send(tripPurpose);
    }
    
    @Override
    public TripPurpose save(TripPurpose purpose) {
        return repository.save(purpose);
    }
    
    @Override
    public List<TripPurpose> getAll() {
        return repository.findAllWithOrganization();
    }
}
