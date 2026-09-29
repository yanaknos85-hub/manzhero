package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.PositionRepository;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.dto.NewPositionDTO;
import ru.sberbank.ditsib.corpclient.dto.PositionDTO;
import ru.sberbank.ditsib.corpclient.dto.PositionSearchDTO;
import ru.sberbank.ditsib.corpclient.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.corpclient.mapper.PositionMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.PositionSender;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;
import ru.sberbank.ditsib.corpclient.service.PositionService;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sber.transport.humanreadableid.service.SQGenerator;

import jakarta.validation.constraints.NotNull;

import java.util.*;

/**
 * Implementation of PositionService
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {
    private final PositionMapper mapper;
    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;
    @Qualifier("sQGenerator")
    private final SQGenerator sqGenerator;

    private final OrganizationService organizationService;

    private final PositionSender positionSender;

    @Override
    public PositionDTO savePosition(@NotNull NewPositionDTO source) {
        final var newEntity = mapper.newDTOToPosition(source);
        final var organization = organizationService.get(source.getOrganizationId());
        checkNonUniqueData(newEntity);
        newEntity.setHumanReadableId(sqGenerator.getNextId(Prefix.PS, organization.getDigitId()));
        newEntity.setOrganization(organization);
        final var save = save(newEntity);
        return mapper.positionToDTO(save);
    }

    @Override
    public Position save(Position source) {
        final var saved = positionRepository.saveAndFlush(source);
        positionSender.send(saved);
        return saved;
    }

    @Override
    public PositionDTO getPosition(@NotNull UUID id) {
        return mapper.positionToDTO(positionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Position.class, id)));
    }

    @Override
    public Optional<Position> getPosition(UUID organizationId, String name) {
        return positionRepository.findByOrganizationIdAndName(organizationId, name);
    }

    @Override
    public void deletePosition(@NotNull UUID id) {
        final var position = positionRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(Position.class, id));
        if (employeeRepository.findAllByPositionIdAndActiveStatus(id, ActiveStatus.ACTIVE, Pageable.unpaged()).isEmpty()) {
            position.setActiveStatus(ActiveStatus.INACTIVE);
            positionRepository.save(position);
        } else {
            throw new IllegalStateResponseException("Запрещено удалять должность, назначенную сотрудникам. " +
                    "Переназначьте должности, затем повторите попытку.");
        }
    }

    @Override
    public void restorePosition(@NotNull UUID id) {
        final var position = positionRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        Position.class, id));
        if (ActiveStatus.ACTIVE.equals(position.getActiveStatus())) {
            throw new IllegalStateResponseException("Невозможно активировать неудаленную должность");
        }
        position.setActiveStatus(ActiveStatus.ACTIVE);
        positionRepository.save(position);
    }

    @Override
    public PositionDTO updatePosition(@NotNull PositionDTO source) {
        final var toUpdate = positionRepository.findById(source.getId()).orElseThrow(() -> new EntityNotFoundException(Position.class, source.getId()));
        toUpdate.setName(source.getName());
        toUpdate.setAvailableClasses(source.getAvailableClasses());
        toUpdate.setSelfApproved(source.isSelfApproved());
        toUpdate.setActiveStatus(source.isActive() ? ActiveStatus.ACTIVE : ActiveStatus.INACTIVE);
        return mapper.positionToDTO(positionRepository.save(toUpdate));
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<PositionDTO> getPositions(@NotNull UUID orgId) {
        return positionRepository.findByOrganizationId(orgId).stream().map(mapper::positionToDTO).toList();
    }

    @Override
    public List<Position> getPositions() {
        return positionRepository.findAll();
    }

    @Override
    public PositionDTO validatePositionByIdAndOrgId(@NotNull UUID id, @NotNull UUID orgId) {
        return mapper.positionToDTO(
                positionRepository.findByOrganizationIdAndId(orgId, id)
                        .orElseThrow(() -> new EntityNotFoundException(
                                Position.class, id)));

    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<PositionDTO> search(PositionSearchDTO positionSearchDTO) {
        final var spec = getSpec(positionSearchDTO);
        return positionRepository.findAll(spec).stream().map(mapper::positionToDTO).toList();
    }

    @Override
    public List<Position> findAllByOrganizationId(UUID organizationId) {
        return positionRepository.findAllByOrganizationId(organizationId);
    }

    private Specification<Position> getSpec(PositionSearchDTO positionSearchDTO) {
        return (root, query, builder) -> {
            assert query != null;
            query.distinct(true);

            var total = builder.notEqual(root.get("id"), new UUID(0, 0));

            final var positionName = positionSearchDTO.getPositionName();
            if (!StringUtils.isEmpty(positionName)) {
                total = builder.and(total, builder.like(builder.lower(root.get(Position_.name)),
                    "%" + positionName.toLowerCase(Locale.ROOT) + "%"));
            }

            final var humanReadableId = positionSearchDTO.getHumanReadableId();
            if (!StringUtils.isEmpty(humanReadableId)) {
                total = builder.and(total, builder.like(builder.lower(root.get(Position_.humanReadableId)),
                    "%" + humanReadableId.toLowerCase(Locale.ROOT) + "%"));
            }
            final var organizationId = positionSearchDTO.getOrganizationId();
            if (organizationId != null) {
                var organizationJoin = root.join(Position_.organization);
                total = builder.and(total, builder.equal(organizationJoin.get(Organization_.id), organizationId));
            }

            total = builder.and(total, builder.equal(root.get(Position_.activeStatus),
                    positionSearchDTO.isActive() ? ActiveStatus.ACTIVE : ActiveStatus.INACTIVE));
            final var selfApproved = positionSearchDTO.getSelfApproved();
            if (selfApproved != null) {
                total = builder.and(total, builder.equal(root.get(Position_.selfApproved),
                    selfApproved));
            }

            final var availableClasses = positionSearchDTO.getAvailableClasses();
            if (availableClasses != null && !availableClasses.isEmpty()) {
                var joinedClasses = root.join(Position_.availableClasses);
                total = builder.and(total, joinedClasses.in(availableClasses));
            }

            return total;
        };
    }

    /**
     * throws UniqueDataResponseException if new data is not unique
     *
     * @param newData data to check
     */
    protected void checkNonUniqueData(@NotNull Position newData) {
        if (newData.getId() == null) {
            positionRepository.findByPositionNameAndOrganizationId(newData.getName(),
                    newData.getOrganization().getId()).ifPresent(this::throwDuplicateName);
        } else {
            positionRepository.findByPositionNameAndOrganizationIdAndIdNot(newData.getName(),
                    newData.getOrganization().getId(),
                    newData.getId()).ifPresent(this::throwDuplicateName);
        }
    }

    /**
     * Бросить исключение о дубликате имен.
     *
     * @param position существующие данные.
     */
    private void throwDuplicateName(Position position) {
        throw new DuplicateDataException(position.getClass(), "positionName", position.getName());
    }
}
