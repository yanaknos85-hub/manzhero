package ru.sberbank.ditsib.corpclient.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.corpclient.exceptions.DataConflictException;
import ru.sberbank.ditsib.corpclient.exceptions.OrganizationNotFoundException;
import ru.sberbank.ditsib.corpclient.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.corpclient.mapper.DepartmentMapper;
import ru.sberbank.ditsib.corpclient.mapper.EmployeeMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.DepartmentSender;
import ru.sberbank.ditsib.corpclient.service.DepartmentService;
import ru.sberbank.ditsib.corpclient.util.SortUtils;
import ru.sber.transport.humanreadableid.service.SQGenerator;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация сервиса по работе с подразделениями.
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
class DepartmentServiceImpl implements DepartmentService {

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final DepartmentRepository departmentRepository;
    private final OrganizationRepository organizationRepository;
    private final EmployeeRepository employeeRepository;
    @Qualifier("sQGenerator")
    private final SQGenerator sqGenerator;
    private final DepartmentSender departmentSender;

    @Override
    public DepartmentDTO saveDepartment(UUID organizationId, @NotNull NewDepartmentDTO source) {
        var newDepartment = departmentMapper.newDTOToDepartment(source);
        checkNonUniqueData(newDepartment, organizationId);
        Optional.ofNullable(source)
                .map(NewDepartmentDTO::getDepartmentHead)
                .map(DepartmentHeadDTO::getId)
                .flatMap(employeeRepository::findById)
                .ifPresent(newDepartment::setHead);
        Optional.ofNullable(source)
                .map(NewDepartmentDTO::getParent)
                .map(DepartmentParentDTO::getId)
                .flatMap(departmentRepository::findById)
                .ifPresent(newDepartment::setParent);
        newDepartment.setOrganization(organizationRepository.getReferenceById(organizationId));
        newDepartment.setHumanReadableId(getHumanReadable(newDepartment));
        newDepartment.setUpdateTime(OffsetDateTime.now());
        var saved = departmentRepository.save(newDepartment);
        departmentSender.send(saved);
        return departmentMapper.departmentToDTO(saved);
    }

    @Override
    public List<Department> getUpperLevelDepartments(UUID organizationId) {
        return departmentRepository.findByOrganizationIdAndParentIsNull(organizationId);
    }

    @Override
    public List<Department> findAllByOrganizationId(UUID organizationId) {
        return departmentRepository.findAllByOrganizationId(organizationId);
    }

    @Override
    public Collection<Department> getUpperLevelActiveDepartments(UUID organizationId) {
        return departmentRepository.findAllByOrganizationIdAndActiveStatusAndParentIsNull(organizationId, ActiveStatus.ACTIVE);
    }

    @Override
    public List<DepartmentDTO> getUpperLevelActiveDepartmentsDto(UUID organizationId) {
        return departmentRepository.findAllByOrganizationIdAndActiveStatusAndParentIsNull
                        (organizationId, ActiveStatus.ACTIVE)
                .stream().map(departmentMapper::departmentToDTO).toList();
    }

    @Override
    public Collection<DepartmentDTO> getLevelDepartments(UUID organizationId) {
        Collection<Department> upperLevelDepartments = getUpperLevelDepartments(organizationId);
        if (upperLevelDepartments == null) {
            throw new EntityNotFoundException(Department.class, organizationId);
        }
        Collection<Department> secondLevelDepartments = new ArrayList<>();
        Collection<Department> thirdLevelDepartments = new ArrayList<>();
        Collection<Department> fourthLevelDepartments = new ArrayList<>();
        Collection<Department> fifthLevelDepartments = new ArrayList<>();
        if (!upperLevelDepartments.isEmpty()) {
            upperLevelDepartments.forEach((upperLevelDepartment -> upperLevelDepartment.setLevelCode(1)));
            upperLevelDepartments.forEach(upperLevelDepartment ->
                    secondLevelDepartments.addAll(departmentRepository.findByOrganizationIdAndParentId(organizationId, upperLevelDepartment.getId())));
        }
        if (!secondLevelDepartments.isEmpty()) {
            secondLevelDepartments.forEach((secondLevelDepartment -> secondLevelDepartment.setLevelCode(2)));
            secondLevelDepartments.forEach(secondLevelDepartment ->
                    thirdLevelDepartments.addAll(departmentRepository.findByOrganizationIdAndParentId(organizationId, secondLevelDepartment.getId())));
        }
        if (!thirdLevelDepartments.isEmpty()) {
            thirdLevelDepartments.forEach((thirdLevelDepartment -> thirdLevelDepartment.setLevelCode(3)));
            thirdLevelDepartments.forEach(thirdLevelDepartment ->
                    fourthLevelDepartments.addAll(departmentRepository.findByOrganizationIdAndParentId(organizationId, thirdLevelDepartment.getId())));
        }
        if (!fourthLevelDepartments.isEmpty()) {
            fourthLevelDepartments.forEach((fourthLevelDepartment -> fourthLevelDepartment.setLevelCode(4)));
            fourthLevelDepartments.forEach(fourthLevelDepartment ->
                    fifthLevelDepartments.addAll(departmentRepository.findByOrganizationIdAndParentId(organizationId, fourthLevelDepartment.getId())));
        }
        if (!fifthLevelDepartments.isEmpty()) {
            fifthLevelDepartments.forEach((fifthLevelDepartment -> fifthLevelDepartment.setLevelCode(5)));
        }
        upperLevelDepartments.addAll(secondLevelDepartments);
        upperLevelDepartments.addAll(thirdLevelDepartments);
        upperLevelDepartments.addAll(fourthLevelDepartments);
        upperLevelDepartments.addAll(fifthLevelDepartments);
        Collection<DepartmentDTO> levelDepartmentsDto = new ArrayList<>();
        upperLevelDepartments.forEach(upperLevelDepartment ->
                levelDepartmentsDto.add(departmentMapper.departmentToDTO(upperLevelDepartment)));
        return levelDepartmentsDto;
    }

    @Override
    public Department getDepartment(@NotNull UUID id) {
        return departmentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Department.class, id));

    }

    @Override
    public DepartmentDTO getDepartmentDto(@NotNull UUID id) {
        return departmentMapper.departmentToDTO(getDepartment(id));
    }

    @Override
    public DepartmentDTO validateDepartmentByIdAndOrgId(UUID id, UUID orgId) {
        return departmentMapper.departmentToDTO(
                departmentRepository.getByIdAndOrganizationId(id, orgId)
                        .orElseThrow(() -> new EntityNotFoundException(Department.class, id)));
    }

    @Override
    public void deleteDepartment(@NotNull UUID id) {
        var department = departmentRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(Department.class, id));
        department.setActiveStatus(ActiveStatus.INACTIVE);
        departmentRepository.save(department);
    }

    @Override
    public DepartmentDTO updateDepartment(@NotNull UUID organizationId, @NotNull UUID departmentId, @NotNull NewDepartmentDTO source) {

        var newDepartment = departmentRepository.getReferenceById(departmentId);

        if (existsCode(organizationId, source.getCode(), departmentId)) {
            throw new DuplicateDataException(Department.class, Map.of("code", source.getCode(), "organizationId", organizationId));
        }
        if (existsCode(organizationId, source.getName(), departmentId)) {
            throw new DuplicateDataException(Department.class, Map.of("name", source.getName(), "organizationId", organizationId));
        }

        newDepartment.setUpdateTime(OffsetDateTime.now());
        String location = source.getLocation();
        if (location != null) {
            newDepartment.setLocation(location);
        }
        String levelName = source.getLevelName();
        if (levelName != null) {
            newDepartment.setLevelName(levelName);
        }
        var status = source.getStatus();
        if (status != null) {
            newDepartment.setActiveStatus(departmentMapper.toModel(status));
        }
        UUID departmentHeadId = source.getDepartmentHead() != null ? source.getDepartmentHead().getId() : null;
        if (departmentHeadId != null) {
            var employeeRepositoryOne = employeeRepository
                    .findById(departmentHeadId).orElse(employeeMapper.employeeFromId(departmentHeadId));
            newDepartment.setHead(employeeRepositoryOne);
            newDepartment.setHandmade(true);
        }
        UUID geozoneId = source.getGeozoneId();
        if (geozoneId != null) {
            newDepartment.setGeozone(geozoneId);
        }
        UUID parentId = source.getParent() != null ? source.getParent().getId() : null;
        if (parentId != null) {
            var departmentRepositoryOne = departmentRepository
                    .findById(parentId).orElse(departmentMapper.departmentFromId(parentId));
            newDepartment.setParent(departmentRepositoryOne);
        }
        var levelCode = source.getLevelCode();
        if (levelCode > 0) {
            newDepartment.setLevelCode(levelCode);
        }
        if (source.getFilialFlag() != null) {
            newDepartment.setFilialFlag(source.getFilialFlag());
        }
        // Поля notNull
        newDepartment.setCode(source.getCode());
        newDepartment.setName(source.getName());
        checkNonUniqueData(newDepartment, organizationId);
        var saved = departmentRepository.save(newDepartment);
        departmentSender.send(saved);
        return departmentMapper.departmentToDTO(saved);
    }

    @SuppressWarnings("java:S3958")
    @Override
    public Iterable<DepartmentSelectDTO> getActiveDepartmentsDto(
            Set<UUID> departmentSet, @NotNull UUID orgId, DepartmentParameters parameters, DepartmentProjection projection
    ) {
        if (projection == null) {
            projection = DepartmentProjection.FULL;
        }
        var page = parameters.getPage();
        var size = parameters.getSize();
        var direction = parameters.getDirection();
        var field = parameters.getField();
        var filter = parameters.getFilter();

        if (projection == DepartmentProjection.SELECT) {
            var departments = departmentRepository.findAll(PageRequest.of(page, size, SortUtils.createSort(direction, field)));
            return departments.stream().map(departmentMapper::departmentToSelectDTO).toList();
        } else if (projection == DepartmentProjection.FULL) {
            DepartmentSpecification spec;
            if (filter.get(DepartmentField.STATUS) == null || filter.get(DepartmentField.STATUS).equals(ActiveStatus.ACTIVE.name())) {
                spec = new DepartmentSpecification(orgId, departmentSet, filter, ActiveStatus.ACTIVE, null);
            } else {
                spec = new DepartmentSpecification(orgId, departmentSet, filter, null, null);
            }
            return departmentRepository.findAll(spec, PageRequest.of(page, size, SortUtils.createSort(direction, field))).map(this::setFullStructurePath);
        }
        return Collections.emptyList();
    }

    @Override
    public Iterable<DepartmentSelectDTO> getActiveDepartmentsDto(
            @NotNull UUID orgId, DepartmentParameters parameters, DepartmentProjection projection
    ) {
        return getActiveDepartmentsDto(null, orgId, parameters, projection);
    }

    @Override
    public Iterable<DepartmentSelectDTO> getDepartmentsDto(Set<UUID> orgIds,
                                                           Set<UUID> departmentIds,
                                                           DepartmentParameters parameters,
                                                           @NotNull DepartmentProjection projection) {

        var page = parameters.getPage();
        var size = parameters.getSize();
        var direction = parameters.getDirection();
        var field = parameters.getField();
        var filter = parameters.getFilter();
        var status = Optional.ofNullable(filter)
                .map(map ->map.get(DepartmentField.STATUS))
                .map(String.class::cast)
                .map(val->Arrays.stream(ActiveStatus.values())
                        .filter(e -> e.name().equalsIgnoreCase(val))
                        .findAny())
                .orElse(Optional.empty()).orElse(null);

        filter.remove(DepartmentField.STATUS);

        if (departmentIds != null && !departmentIds.isEmpty()) {
            return departmentRepository.findAllById(departmentIds).stream()
                    .map(departmentMapper::departmentToSelectDTO)
                    .toList();
        }

        if ((orgIds == null || orgIds.isEmpty())
                || projection == null
                || !Set.of(DepartmentProjection.SELECT,
                        DepartmentProjection.FULL,
                        DepartmentProjection.MIN).contains(projection)) {
            return Collections.emptyList();
        }
        var spec = new DepartmentSpecification(null, null, filter, status, orgIds);

        var departments = departmentRepository.findAll(spec, PageRequest.of(page, size, SortUtils.createSort(direction, field)));
        if (projection == DepartmentProjection.FULL) {
            return departments.map(this::setFullStructurePath);
        }
        return departments.stream().map(departmentMapper::departmentToSelectDTO).toList();
    }

    @Override
    public Iterable<DepartmentDTO> getAllDepartmentsDto(
            @NotNull UUID orgId, DepartmentParameters parameters
    ) {
        var page = parameters.getPage();
        var size = parameters.getSize();
        var direction = parameters.getDirection();
        var field = parameters.getField();
        var filter = parameters.getFilter();
        var spec = new DepartmentSpecification(orgId, null, filter, null, null);
        return departmentRepository.findAll(spec, PageRequest.of(page, size, SortUtils.createSort(direction, field)))
                .map(departmentMapper::departmentToDTO);
    }

    @Override
    public Iterable<Department> getDepartments(
            @NotNull UUID orgId, DepartmentParameters parameters
    ) {
        var page = parameters.getPage();
        var size = parameters.getSize();
        var direction = parameters.getDirection();
        var field = parameters.getField();
        return departmentRepository.getByOrganizationIdAndActiveStatus(orgId, ActiveStatus.ACTIVE,
                PageRequest.of(page, size, SortUtils.createSort(direction, field)));
    }

    @Override
    public boolean existsById(@NotNull UUID id) {
        return departmentRepository.existsById(id);
    }


    @Override
    public Iterable<DepartmentDTO> getChildrenDepartments(
            UUID departmentId, DepartmentParameters parameters
    ) {
        var page = parameters.getPage();
        var size = parameters.getSize();
        var direction = parameters.getDirection();
        var field = parameters.getField();
        return departmentRepository.findDistinctByParentId(departmentId,
                        PageRequest.of(page, size, SortUtils.createSort(direction, field)))
                .map(departmentMapper::departmentToDTO);
    }

    @Override
    public Optional<Department> getDepartment(UUID organizationId, String code) {
        return departmentRepository.findByOrganizationIdAndCode(organizationId, code);
    }

    @Override
    public Optional<Department> getDepartmentByName(UUID organizationId, String name) {
        return departmentRepository.findByOrganizationIdAndName(organizationId, name);
    }

    @Override
    public void save(Department department) {
        department.setUpdateTime(OffsetDateTime.now());
        var saved = departmentRepository.save(department);
        departmentSender.send(saved);
    }

    @Override
    public Iterable<Department> getAll(DepartmentParameters parameters) {
        var page = parameters.getPage();
        var size = parameters.getSize();
        var direction = parameters.getDirection();
        var field = parameters.getField();
        return departmentRepository.findAll(PageRequest.of(page, size, SortUtils.createSort(direction, field)));
    }

    @Override
    public Iterable<Department> getAll() {
        return departmentRepository.findAll();
    }

    @Override
    public Optional<UUID> detectLooping(UUID departmentId, UUID parentId) {
        if (parentId == null) {
            return Optional.empty();
        }
        if (departmentId.equals(parentId)) {
            return Optional.of(departmentId);
        }
        var parent = departmentRepository.findById(parentId);
        if (parent.isEmpty()) {
            return Optional.empty();
        }
        return detectLooping(parent.get().getId(),
                parent.map(Department::getParent).map(Department::getId).orElse(null));
    }


    /**
     * throws UniqueDataResponseException if new data is not unique
     *
     * @param newData data to check
     */
    protected void checkNonUniqueData(@NotNull Department newData, UUID organizationId) {
        if (newData.getId() == null) {
            departmentRepository.findByCodeAndActiveStatusAndOrganizationId(newData.getCode(), ActiveStatus.ACTIVE,
                            organizationId)
                    .ifPresent(this::throwCodeDuplicate);
        } else {
            departmentRepository
                    .findByCodeAndIdNotAndActiveStatusAndOrganizationId(newData.getCode(), newData.getId(),
                            ActiveStatus.ACTIVE, organizationId)
                    .ifPresent(this::throwCodeDuplicate);
        }
    }

    private void throwCodeDuplicate(Department department) {
        throw new DuplicateDataException(department.getClass(), "code", department.getCode());
    }

    private String getHumanReadable(Department department) {
        if (department.getOrganization() == null) {
            throw new DataConflictException("Отсутствует организация в подразделении");
        }
        var organization =
                organizationRepository.findById(department.getOrganization().getId()).orElseThrow(
                        () -> new OrganizationNotFoundException(department.getOrganization().getId()));
        return sqGenerator.getNextId(Prefix.DT, organization.getDigitId());
    }

    private DepartmentDTO setFullStructurePath(Department department) {
        var departmentDTO = departmentMapper.departmentToDTO(department);
        var departmentForCycle = department;
        while (departmentForCycle.getParent() != null) {
            departmentForCycle = departmentForCycle.getParent();
            departmentDTO.setFullStructurePath(departmentForCycle.getName() + "/" + departmentDTO.getFullStructurePath());
        }
        return departmentDTO;
    }

    /**
     * Спецификация фильтрации подразделений.
     */
    @RequiredArgsConstructor
    static class DepartmentSpecification implements Specification<Department> {

        private final UUID orgId;
        private final Set<UUID> departmentSet;
        private final Map<DepartmentField, Serializable> filter;
        private final ActiveStatus status;
        private final Set<UUID> orgIds;

        @Override
        public Predicate toPredicate(
                Root<Department> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder
        ) {
            var predicate = orgIds != null && !orgIds.isEmpty()
                    ? root.get(Department_.ORGANIZATION).get(Organization_.ID).in(orgIds)
                    : criteriaBuilder.equal(root.get(Department_.ORGANIZATION).get(Organization_.ID), orgId);

            if (status != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(Department_.ACTIVE_STATUS), status));
            }
            if (departmentSet != null && !departmentSet.isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get(Department_.ID).in(departmentSet));
            }

            if (filter != null) {
                for (var entry : filter.entrySet()) {
                    var key = entry.getKey();
                    var value = entry.getValue();
                    if (value == null) {
                        continue;
                    }
                    predicate =
                            switch (key) {
                                case NAME -> criteriaBuilder.and(predicate, criteriaBuilder.like(criteriaBuilder.lower(root.get(Department_.NAME)),
                                        value.toString().toLowerCase(Locale.ROOT) + "%"));
                                case STATUS -> criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(Department_.ACTIVE_STATUS),
                                        ActiveStatus.valueOf((String) value)));
                                case ID -> criteriaBuilder.and(predicate, criteriaBuilder.like(root.get(Department_.HUMAN_READABLE_ID),
                                        "%" + value + "%"));
                                case CODE -> criteriaBuilder.and(predicate, criteriaBuilder.like(root.get(Department_.CODE),
                                        value + "%"));
                                case LOCATION -> criteriaBuilder.and(predicate, criteriaBuilder.like(root.get(Department_.LOCATION),
                                        "%" + value + "%"));
                            };
                }
            }
            return predicate;
        }
    }

    @Override
    public Iterable<DepartmentTreeNodeDTO> getAllDepartmentsAsTree(UUID organizationId) {
        TreeNode<DepartmentLevelDTO> treeRoot = constructTree(organizationId);
        LinkedList<DepartmentTreeNodeDTO> allDepartmentLinkedList = new LinkedList<>();
        traversePreOrder2(treeRoot, allDepartmentLinkedList);
        return allDepartmentLinkedList;
    }

    private void traversePreOrder2(TreeNode<DepartmentLevelDTO> treeNode, LinkedList<DepartmentTreeNodeDTO> limitLinkedList) {
        if (treeNode != null) {
            DepartmentTreeNodeDTO departmentTreeNodeDTO = new DepartmentTreeNodeDTO();
            departmentTreeNodeDTO.setId(treeNode.data.getDepartment().getId());
            departmentTreeNodeDTO.setName(treeNode.data.getDepartment().getName());
            departmentTreeNodeDTO.setFilialFlag(treeNode.data.getDepartment().getFilialFlag());
            Set<UUID> childrenIds = treeNode.data.getDepartment().getChildren().stream().map(Department::getId).collect(Collectors.toSet());
            departmentTreeNodeDTO.setChildren(childrenIds);
            limitLinkedList.add(departmentTreeNodeDTO);
            for (TreeNode<DepartmentLevelDTO> treeNode1 : treeNode.getChildren()) {
                traversePreOrder2(treeNode1, limitLinkedList);
            }
        }
    }

    @Override
    public CheckFilialFlagResutlDTO checkFilialFlagAll(UUID organizationId) {
        TreeNode<DepartmentLevelDTO> treeRoot = constructTree(organizationId);
        int numOfFilialFlags = 0;
        int numOfDeletedFlags = 0;
        LinkedList<DepartmentLevelDTO> allDepartmentLinkedList = new LinkedList<>();
        traversePreOrder(treeRoot, allDepartmentLinkedList);
        int maxLevel = getMaxLevel(allDepartmentLinkedList) + 1;
        for (int i = 0; i < maxLevel; i++) { // loop levels
            for (DepartmentLevelDTO departmentLevelDTO : allDepartmentLinkedList) {
                if (departmentLevelDTO.getLevel() == i
                        && Boolean.TRUE.equals(departmentLevelDTO.getDepartment().getFilialFlag())) {
                    int num = checkAndRemoveFililaFlags(treeRoot, departmentLevelDTO.getDepartment());
                    numOfFilialFlags++;
                    numOfDeletedFlags += num;
                }
            }
        }
        return new CheckFilialFlagResutlDTO(numOfFilialFlags + numOfDeletedFlags, numOfDeletedFlags);
    }

    @Override
    public boolean existsName(UUID id, String name, UUID exclusion) {
        if (exclusion == null) {
            return departmentRepository.existsByOrganizationIdAndName(id, name);
        } else {
            return departmentRepository.existsByOrganizationIdAndNameAndIdNotIn(id, name, Set.of(exclusion));
        }
    }

    @Override
    public boolean existsCode(UUID id, String code, UUID exclusion) {
        if (exclusion == null) {
            return departmentRepository.existsByOrganizationIdAndCode(id, code);
        } else {
            return departmentRepository.existsByOrganizationIdAndCodeAndIdNotIn(id, code, Set.of(exclusion));
        }
    }

    private int checkAndRemoveFililaFlags(TreeNode<DepartmentLevelDTO> treeRoot, Department department) {
        TreeNode<DepartmentLevelDTO> treeNode = searchNode(treeRoot, department);
        LinkedList<DepartmentLevelDTO> departmentLinkedList = new LinkedList<>();
        traversePreOrder(treeNode, departmentLinkedList);
        List<Department> departmentList = new ArrayList<>();
        for (int i = 1; i < departmentLinkedList.size(); i++) {
            DepartmentLevelDTO departmentLevelDTO = departmentLinkedList.get(i);
            if (Boolean.TRUE.equals(departmentLevelDTO.getDepartment().getFilialFlag())) {
                departmentLevelDTO.getDepartment().setFilialFlag(false);
                departmentList.add(departmentLevelDTO.getDepartment());
            }
        }
        departmentRepository.saveAll(departmentList);
        return departmentList.size();
    }

    private TreeNode<DepartmentLevelDTO> searchNode(TreeNode<DepartmentLevelDTO> treeNode, Department department) {
        if (treeNode.data.getDepartment().getId().equals(department.getId())) {
            return treeNode;
        } else {
            TreeNode<DepartmentLevelDTO> found;
            for (TreeNode<DepartmentLevelDTO> treeNode1 : treeNode.getChildren()) {
                found = searchNode(treeNode1, department);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private int getMaxLevel(List<DepartmentLevelDTO> allDepartmentLinkedList) {
        int maxLevel = 0;
        for (DepartmentLevelDTO departmentLevelDTO : allDepartmentLinkedList) {
            if (departmentLevelDTO.getLevel() > maxLevel) {
                maxLevel = departmentLevelDTO.getLevel();
            }
        }
        return maxLevel;
    }

    @Override
    public boolean checkFilialFlag(Department department) {
        if (Boolean.TRUE.equals(department.getFilialFlag())) {
            return true;
        }
        if (!ActiveStatus.ACTIVE.equals(department.getActiveStatus())) {
            return false;
        }
        // check if parents has flag
        Department department1 = checkParentFilialFlag(department.getParent());
        if (department1 != null) {
            return true;
        }
        TreeNode<DepartmentLevelDTO> treeRoot = constructTree(department);
        LinkedList<DepartmentLevelDTO> departmentLinkedList = new LinkedList<>();
        traversePreOrder(treeRoot, departmentLinkedList);
        // chek if subtree has flag
        return departmentLinkedList.stream().anyMatch(e -> Boolean.TRUE.equals(e.getDepartment().getFilialFlag()));
    }

    private void traversePreOrder(TreeNode<DepartmentLevelDTO> treeNode, LinkedList<DepartmentLevelDTO> limitLinkedList) {
        if (treeNode != null) {
            limitLinkedList.add(treeNode.data);
            for (TreeNode<DepartmentLevelDTO> treeNode1 : treeNode.getChildren()) {
                traversePreOrder(treeNode1, limitLinkedList);
            }
        }
    }

    private Department checkParentFilialFlag(Department department) {
        if (department == null) {
            return null;
        }
        if (Boolean.TRUE.equals(department.getFilialFlag())) {
            return department;
        } else {
            return checkParentFilialFlag(department.getParent());
        }
    }

    private TreeNode<DepartmentLevelDTO> constructTree(@lombok.NonNull Department department) {
        TreeNode<DepartmentLevelDTO> treeRoot = new TreeNode<>(new DepartmentLevelDTO(department, 0));
        fillTree(treeRoot, 0);
        return treeRoot;
    }

    private TreeNode<DepartmentLevelDTO> constructTree(UUID organizationId) {
        var departmentList = getUpperLevelActiveDepartments(organizationId);
        var upperDepartment = departmentList.stream().findFirst().orElse(null);
        if (upperDepartment == null) {
            throw new DataConflictException("No uppper level status department found!");
        }
        TreeNode<DepartmentLevelDTO> treeRoot = new TreeNode<>(new DepartmentLevelDTO(upperDepartment, 0));
        fillTree(treeRoot, 0);
        return treeRoot;
    }

    private void fillTree(TreeNode<DepartmentLevelDTO> treeNode, int level) {
        level++;
        Department nodeDepartment = treeNode.data.getDepartment();
        for (Department department : nodeDepartment.getChildren()) {
            if (ActiveStatus.ACTIVE.equals(department.getActiveStatus())) {
                TreeNode<DepartmentLevelDTO> treeNode1 = treeNode.addChild(new DepartmentLevelDTO(department, level));
                fillTree(treeNode1, level);
            }
        }
    }

    static class TreeNode<T> {
        T data;

        TreeNode<T> parent = null;

        @Getter
        List<TreeNode<T>> children;

        public TreeNode(T data) {
            this.data = data;
            this.children = new LinkedList<>();
        }

        public TreeNode<T> addChild(T child) {
            TreeNode<T> childNode = new TreeNode<>(child);
            childNode.parent = this;
            this.children.add(childNode);
            return childNode;
        }
    }
}
