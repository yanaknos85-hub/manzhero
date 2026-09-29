package ru.sberbank.ditsib.corpclient.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationGroupRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.dao.TransportOrgRepository;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.database.model.OrganizationGroup;
import ru.sberbank.ditsib.corpclient.database.model.OrganizationGroup_;
import ru.sberbank.ditsib.corpclient.database.model.TransportOrg;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.corpclient.exceptions.DataConflictException;
import ru.sberbank.ditsib.corpclient.messaging.sender.OrganizationSender;
import ru.sberbank.ditsib.corpclient.service.OrganizationGroupService;
import ru.sberbank.utils.reflection.ReflectionUtils;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationGroupServiceImpl implements OrganizationGroupService {

    private final OrganizationGroupRepository organizationGroupRepository;

    private final OrganizationRepository organizationRepository;

    private final OrganizationSender organizationSender;

    private final TransportOrgRepository transportOrgRepository;

    @Override
    public OrganizationGroupResponseDTO add(OrganizationGroupDTO organizationGroupDTO) throws JsonProcessingException {
        var organizationIds = new ArrayList<>(organizationGroupDTO.organizationIds());
        var organizations = organizationRepository.findAllByIdIn(organizationIds);
        organizationIds.removeAll(organizations.stream()
                .filter(organization -> organization.getOrganizationGroup()==null)
                .map(Organization::getId).toList());
        if(!organizationIds.isEmpty()) {
            throwException(organizationIds, organizations);
        }
        var organizationGroup = OrganizationGroup.builder()
                .name(organizationGroupDTO.name()).internal(organizationGroupDTO.internal()).build();
        var existingOrganizationGroup = organizationGroupRepository.findByName(organizationGroup.getName());
        if(existingOrganizationGroup.isPresent()){
            throw new DataConflictException(OrganizationGroup.class, DataConflictException.ConflictType.ORGANIZATION_GROUP_WITH_CURRENT_NAME_ALREADY_EXISTS,
                    existingOrganizationGroup.get().getId(), "name", organizationGroup.getName());
        }
        var organizationGroupFinal = organizationGroupRepository.save(organizationGroup);

        final var classes = transportOrgRepository.getAllByOrganizationIdIn(organizations.parallelStream().map(Organization::getId).toList())
                .stream()
                .collect(Collectors.toMap(TransportOrg::getOrganizationId, List::of, (l, r) -> Stream.concat(l.stream(), r.stream()).toList()));

        organizations.forEach(organization -> {
            organization.setOrganizationGroup(organizationGroupFinal);
            organization.setAvailableClasses(classes.getOrDefault(organization.getId(), List.of()).stream().map(TransportOrg::getTransportType).toList());
        });

        organizationRepository.saveAll(organizations);
        organizations.forEach(organizationSender::send);
        return new OrganizationGroupResponseDTO(organizationGroupFinal.getId(),
                organizationGroupFinal.getName(),
                organizationGroupFinal.isInternal());
    }

    @Override
    public Page<OrganizationGroupResponseDTO> getAll(OrganizationGroupSearchDTO organizationGroupSearchDTO) {
        var spec = getSpec(organizationGroupSearchDTO);
        var pageRequest = PageRequest.of(organizationGroupSearchDTO.getPage(), organizationGroupSearchDTO.getSize());
        return organizationGroupRepository.findAll(spec, pageRequest).map(this::toResponseDto);
    }

    @Override
    public void update(UUID organizationGroupId, OrganizationGroupDTO organizationGroupDTO) {
        var organizationGroup = organizationGroupRepository.findById(organizationGroupId)
                .orElseThrow(() -> new EntityNotFoundException(OrganizationGroup.class, organizationGroupId));
        changeName(organizationGroup, organizationGroupDTO.name());
        changeOrganizations(organizationGroup, null, organizationGroupDTO.organizationIds());

    }

    @Override
    public void patch(UUID organizationGroupId, List<PatchData<OrganizationGroupPatchFields>> fields) {
        var organizationGroup = organizationGroupRepository.findById(organizationGroupId)
                .orElseThrow(() -> new EntityNotFoundException(OrganizationGroup.class, organizationGroupId));
        fields.forEach(entry -> {
            var field = entry.field();
            if(OrganizationGroupPatchFields.NAME.equals(field)){
                changeName(organizationGroup, String.valueOf(entry.value()));
            }
            if(OrganizationGroupPatchFields.ORGANIZATION_IDS.equals(field)){
                changeOrganizations(organizationGroup, entry, null);
            }
        });
    }

    @Override
    public void delete(UUID organizationGroupId) {
        var organizations = organizationRepository.findAllByOrganizationGroupId(organizationGroupId);
        organizations.forEach(organization -> organization.setOrganizationGroup(null));
        organizationRepository.saveAll(organizations);
        organizations.forEach(organizationSender::send);
        organizationGroupRepository.deleteById(organizationGroupId);
    }

    private Specification<OrganizationGroup> getSpec(OrganizationGroupSearchDTO organizationGroupSearchDTO){
        return (root, q, cb) -> {
            Predicate predicate = cb.equal(cb.literal(1), 1);
            for (var entry : organizationGroupSearchDTO.getFilter().entrySet()) {
                var value = entry.getValue();
                if (value != null) {
                    predicate = switch (entry.getKey()) {
                        case NAME -> cb.and(predicate, cb.like(cb.lower(root.get(OrganizationGroup_.name)),entry.getValue().toString().toLowerCase(Locale.ROOT) + "%"));
                        case INTERNAL -> cb.and(predicate, cb.equal(root.get(OrganizationGroup_.internal), entry.getValue()));
                    };
                }
            }
            return predicate;
        };
    }

    private OrganizationGroupResponseDTO toResponseDto(OrganizationGroup organizationGroup){
        return new OrganizationGroupResponseDTO(organizationGroup.getId(), organizationGroup.getName(),
                organizationGroup.isInternal());
    }

    private void throwException(List<UUID> organizationIds, List<Organization> organizations){
        var conflictMap = new HashMap<UUID, String>();
        organizations.stream()
                .filter(organization -> organizationIds.contains(organization.getId()))
                .filter(organization -> organization.getOrganizationGroup()!=null)
                .collect(Collectors.toList()) //NOSONAR
                .forEach(organization -> conflictMap.put(organization.getId(), organization.getOrganizationGroup().getName())); //NOSONAR
        throw new DataConflictException(Organization.class,
                DataConflictException.ConflictType.ORGANIZATION_ALREADY_IN_GROUP,
                ReflectionUtils.cast(organizationIds), "organizationGroup", ReflectionUtils.cast(conflictMap));
    }

    private void changeName(OrganizationGroup organizationGroup, String name){
        organizationGroup.setName(name);
        var existingOrganizationGroup = organizationGroupRepository.findByName(organizationGroup.getName());
        if(existingOrganizationGroup.isPresent() && existingOrganizationGroup.get().getId()!=organizationGroup.getId()){
            throw new DataConflictException(OrganizationGroup.class, DataConflictException.ConflictType.ORGANIZATION_GROUP_WITH_CURRENT_NAME_ALREADY_EXISTS,
                    existingOrganizationGroup.get().getId(), "name", organizationGroup.getName());
        }
        organizationGroupRepository.save(organizationGroup);

    }

    private void changeOrganizations(OrganizationGroup organizationGroup, PatchData<OrganizationGroupPatchFields> entry, List<UUID> newOrganizationIds){
        var objectMapper = new ObjectMapper();
        var organizationIds = new ArrayList<UUID>();
        if(newOrganizationIds==null) {
            organizationIds.addAll(objectMapper
                    .convertValue(entry.value(), new TypeReference<ArrayList<UUID>>(){}));
        } else {
            organizationIds.addAll(newOrganizationIds);
        }
        var previousOrganizations = organizationRepository.findAllByOrganizationGroupId(organizationGroup.getId());

        final var previousClasses = transportOrgRepository.getAllByOrganizationIdIn(previousOrganizations.parallelStream().map(Organization::getId).toList())
                .stream()
                .collect(Collectors.toMap(TransportOrg::getOrganizationId, List::of, (l, r) -> Stream.concat(l.stream(), r.stream()).toList()));

        previousOrganizations.forEach(organization -> {
            organization.setOrganizationGroup(null);
            organization.setAvailableClasses(previousClasses.getOrDefault(organization.getId(), List.of()).stream().map(TransportOrg::getTransportType).toList());
        });

        organizationRepository.saveAll(previousOrganizations);
        previousOrganizations.forEach(organizationSender::send);
        var newOrganizations = organizationRepository.findAllByIdIn(organizationIds);

        final var newClasses = transportOrgRepository.getAllByOrganizationIdIn(newOrganizations.parallelStream().map(Organization::getId).toList())
                .stream()
                .collect(Collectors.toMap(TransportOrg::getOrganizationId, List::of, (l, r) -> Stream.concat(l.stream(), r.stream()).toList()));

        newOrganizations.parallelStream().forEach(organization -> {
            if(organization.getOrganizationGroup()!=null
                    && !organization.getOrganizationGroup().getId().equals(organizationGroup.getId())){
                throwException(List.of(organization.getId()), List.of(organization));
            } else {
                organization.setOrganizationGroup(organizationGroup);
            }
            organization.setAvailableClasses(newClasses.getOrDefault(organization.getId(), List.of()).stream().map(TransportOrg::getTransportType).toList());
        });
        organizationRepository.saveAll(newOrganizations);
        newOrganizations.forEach(organizationSender::send);
    }
}
