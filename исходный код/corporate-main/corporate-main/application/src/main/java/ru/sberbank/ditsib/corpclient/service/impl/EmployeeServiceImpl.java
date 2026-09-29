package ru.sberbank.ditsib.corpclient.service.impl;

import jakarta.persistence.criteria.*;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.sber.transport.authorization.exceptions.UnauthorizedException;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.scim.messages.AccountRoleLinkMessage;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.corpclient.database.dao.PositionRepository;
import ru.sberbank.ditsib.corpclient.database.model.*;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.corpclient.exceptions.UpdateForbiddenException;
import ru.sberbank.ditsib.corpclient.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.corpclient.mapper.EmployeeMapper;
import ru.sberbank.ditsib.corpclient.mapper.OrganizationMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.ConfirmationSender;
import ru.sberbank.ditsib.corpclient.messaging.sender.EmployeeSender;
import ru.sberbank.ditsib.corpclient.messaging.sender.UserSender;
import ru.sberbank.ditsib.corpclient.service.*;
import ru.sberbank.ditsib.corpclient.util.SortUtils;
import ru.sber.transport.humanreadableid.service.SQGenerator;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.corpclient.controller.impl.ExecutorGroupControllerImpl.ROLE_ADMIN_EXECUTOR_GROUP;

/**
 * Реализация сервиса сотрудников.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
class EmployeeServiceImpl implements EmployeeService {

    private static final String USER_ID_FIELD = "userId";

    private final EmployeeMapper employeeMapper;
    private final EmployeeRepository employeeRepository;
    @Qualifier("sQGenerator")
    private final SQGenerator sqGenerator;
    private final DepartmentRepository departmentRepository;
    private final AttributeService attributeService;
    private final OrganizationMapper organizationMapper;
    private final PersonalCarEnrichmentService personalCarEnrichmentService;
    private final EmployeeSender employeeSender;
    private final UserSender userSender;
    private final PositionRepository positionRepository;
    private final OrganizationRepository organizationRepository;
    private final ConfirmationSender confirmationSender;

    @Override
    public EmployeeDTO saveEmployee(UUID departmentId, @NotNull NewEmployeeDTO source) {
        final var newEmployee = employeeMapper.newDTOToEmployee(source);
        final var department = departmentRepository.getReferenceById(departmentId);
        final var organization = organizationRepository.getReferenceById(department.getOrganization().getId());
        newEmployee.setId(UUID.randomUUID());
        newEmployee.setNew(true);
        newEmployee.setDepartment(department);
        newEmployee.setOrgStructureType(OrgStructureType.EXTERNAL);
        newEmployee.setUserId(newEmployee.getId());
        newEmployee.setOrganization(organization);
        Optional.ofNullable(source.supervisorId()).ifPresent(supervisor -> newEmployee.setSupervisor(Employee.builder().id(supervisor).build()));

        checkPersonnelNumber(null, source.personnelNumber(), organization.getId());

        newEmployee.setHumanReadableId(getHumanReadable(newEmployee));

        final var employee = saveEmployee(newEmployee, source.roles());

        return employeeMapper.toDto(employee, employee.getDepartment().getOrganization().getId());
    }

    @Override
    public Employee saveEmployee(Employee source, Collection<String> roles) {
        final var saved = saveEmployee(source);
        userSender.send(saved, roles);
        return saved;
    }

    private Employee saveEmployee(Employee source) {
        if (source.getId() == null) {
            source.setId(UUID.randomUUID());
            source.setNew(true);
        }
        if (source.getUserId() == null) {
            source.setUserId(source.getId());
        }
        if (source.getActiveStatus() == null) {
            source.setActiveStatus(ActiveStatus.ACTIVE);
        }
        source.setOrganization(source.getDepartment().getOrganization());
        source.setUpdateTime(OffsetDateTime.now());
        final var saved = employeeRepository.save(source);

        final var personnelNumber = saved.getPersonnelNumber();
        if (personnelNumber != null) {
            final var employees = employeeRepository.findAllBySupervisorPersonnelNumberAndDepartmentOrganizationId(
                    personnelNumber, source.getDepartment().getOrganization().getId(), Pageable.unpaged());
            employees.forEach(employee -> employee.setSupervisor(saved));
            employeeRepository.saveAll(employees);

            final var departments = departmentRepository.findAllByDepartmentHeadPersonnelNumberAndOrganizationId(
                    personnelNumber, source.getDepartment().getOrganization().getId());
            departments.forEach(department -> department.setHead(saved));
            departmentRepository.saveAll(departments);
        }
        final var supervisorData = saved.getSupervisor();
        if (supervisorData != null && supervisorData.getPersonnelNumber() != null) {
            final var superPersonnelNumber = supervisorData.getPersonnelNumber();
            final var organizationId = saved.getOrganization().getId();
            final var supervisor = employeeRepository.findByPersonnelNumberAndOrganizationId(superPersonnelNumber, organizationId).orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of("personnelNumber", superPersonnelNumber, "organizationId", organizationId)));
            saved.setSupervisor(supervisor);
        }
        final var finallySaved = employeeRepository.save(saved);
        finallySaved.setUserId(finallySaved.getId());

        employeeSender.send(finallySaved);

        return finallySaved;
    }

    @Override
    public EmployeeDTO getEmployeeByOrganizationIdAndUserId(@NotNull UUID organizationId, @NotNull UUID id) {
        return employeeMapper.toDto(employeeRepository.findByDepartmentOrganizationIdAndUserId(organizationId, id)
                .orElseThrow(() -> new EntityNotFoundException(
                        Employee.class, Map.of(USER_ID_FIELD, id))), organizationId);
    }

    @Override
    public Employee getEmployeeByUserId(@NotNull UUID userId) {
        return employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of(USER_ID_FIELD, userId)));
    }

    @Override
    public Employee getEmployeeById(@NotNull UUID id) {
        return employeeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                Employee.class, id));
    }

    @Override
    public void deleteEmployee(@NotNull UUID id) {
        final var employee = getEmployeeById(id);
        employee.setActiveStatus(ActiveStatus.INACTIVE);
        saveEmployee(employee, List.of());
    }

    @Override
    public List<Employee> deleteEmployeeByDepartmentId(UUID departmentId) {
        final var toDelete =
                employeeRepository.findAllByDepartmentIdAndActiveStatus(departmentId, ActiveStatus.ACTIVE, Pageable.unpaged());
        toDelete.forEach(employee -> employee.setActiveStatus(ActiveStatus.INACTIVE));
        return employeeRepository.saveAll(toDelete);
    }

    @Override
    public Employee updateEmployee(@NotNull Employee source, NewEmployeeDTO newData) {
        validateUserExistence(source.getUserId());
        final var employee = employeeRepository.getReferenceById(Objects.requireNonNull(source.getId()));
        checkPersonnelNumber(source.getId(), newData.personnelNumber(), employee.getOrganization().getId());

        if (OrgStructureType.INTERNAL.equals(employee.getOrgStructureType())) {
            throw new UpdateForbiddenException();
        }
        employeeMapper.toModel(employee, newData);
        employee.setPosition(positionRepository.findById(newData.positionId()).orElseThrow(() -> new EntityNotFoundException(Position.class, newData.positionId())));
        employee.getPersonalCars().forEach(elt -> {
            elt.setEmployee(employee);
            personalCarEnrichmentService.addUserAcceptInfo(elt);
        });
        importAttributes(employee.getAttributes());

        return saveEmployee(employee, newData.roles());
    }

    @Override
    public void updateEmployee(@NonNull Employee target, Map<EmployeePatchField, Serializable> data) {
        target = employeeRepository.getReferenceById(Objects.requireNonNull(target.getId()));
        var phoneChanged = false;
        for (final var entry : data.entrySet()) {
            final var field = entry.getKey();
            switch (field) {
                case MOBILE_PHONE -> {
                    target.setMobilePhone(String.valueOf(entry.getValue()));
                    target.setPhoneConfirmed(false);
                    phoneChanged = true;
                }
                case FIRST_NAME -> target.setFirstName(String.valueOf(entry.getValue()));
                case LAST_NAME -> target.setLastName(String.valueOf(entry.getValue()));
                case PATRONYMIC -> target.setPatronymic(String.valueOf(entry.getValue()));
                default -> log.info("Receiving update unsupported field: {}", field);
            }
        }
        target = employeeRepository.save(target);
        if(phoneChanged){
            confirmationSender.send(target.getId(), target.getMobilePhone());
        }
        employeeSender.send(target);
        userSender.send(target, null);
    }

    @Override
    public void signPdn(@NonNull UUID userId) {
        final var employee = employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, userId));

        if (employee.getOrgStructureType().equals(OrgStructureType.INTERNAL)) {
            return;
        }

        employee.setConsent(true);

        employeeRepository.save(employee);
    }

    @Override
    public void validateUser(UUID userId) {
        validateUserExistence(userId);
    }

    @Override
    public Iterable<HasEmployeeData> getEmployeesByOrgId(
            UUID organizationId, EmployeeParameters parameters, EmployeeProjection projection
    ) {
        final var page = parameters.getPage();
        final var size = parameters.getSize();
        final var direction = parameters.getDirection();
        final var field = parameters.getField();
        final var sort = SortUtils.createSort(direction, field);
        final var spec = new EmployeeFilterSpecification(parameters.getFilter());
        spec.setOrganizationId(organizationId);
        if (projection == null) {
            projection = EmployeeProjection.FULL;
        }
        return switch (projection) {
            case FULL -> employeeRepository.findAll(spec, PageRequest.of(page, size, sort))
                    .map(e -> employeeMapper.toDto(e, e.getDepartment().getOrganization().getId()));
            case SELECT -> employeeRepository.findAll(spec, PageRequest.of(page, size, sort))
                    .map(employeeMapper::employeeToSelectDTO);
        };
    }

    @Override
    public List<Employee> findAllByOrganizationId(UUID organizationId) {
        return employeeRepository.findAllByOrganizationId(organizationId);
    }

    @Override
    public Iterable<CustomerDTO> findEmployeesByOrganisationsAndDepartments(List<UUID> organizations, List<UUID> departments, Jwt token) {
        if (checkRightsOnSearchByOrgsAndDeps(organizations, token)) {
            return employeeRepository.findAllByOrganizationIdInOrDepartmentIdIn(organizations, departments)
                    .stream().map(employeeMapper::employeeToCustomerDTO).collect(Collectors.toSet());
        }

        throw new UnauthorizedException(token.getId());
    }

    @Override
    public Iterable<HasEmployeeData> getEmployeesByDepartmentId(
            UUID departmentId, EmployeeParameters parameters, EmployeeProjection projection
    ) {
        final var page = parameters.getPage();
        final var size = parameters.getSize();
        final var direction = parameters.getDirection();
        final var field = parameters.getField();
        final var filter = parameters.getFilter();
        if (projection == null) {
            projection = EmployeeProjection.FULL;
        }
        final var spec = new EmployeeFilterSpecification(filter);
        spec.setDepartmentIds(Collections.singletonList(departmentId));
        return switch (projection) {
            case FULL -> employeeRepository.findAll(spec, PageRequest.of(page, size, SortUtils.createSort(direction, field)))
                    .map(e -> employeeMapper.toDto(e, e.getDepartment().getOrganization().getId()));
            case SELECT -> employeeRepository.findAll(spec, PageRequest.of(page, size, SortUtils.createSort(direction, field)))
                    .map(employeeMapper::employeeToSelectDTO);
        };
    }

    @Override
    public Page<Employee> getEmployeesByDepartmentIds(
            Collection<UUID> departmentIds, EmployeeParameters parameters,
            Collection<UUID> excludedEmployeeIds) {
        final var page = parameters.getPage();
        final var size = parameters.getSize();
        final var direction = parameters.getDirection();
        final var field = parameters.getField();
        final var filter = parameters.getFilter();
        final var spec = new EmployeeFilterSpecification(filter);
        spec.setDepartmentIds(departmentIds);
        spec.setExcludedEmployees(excludedEmployeeIds);
        return employeeRepository.findAll(spec, PageRequest.of(page, size, SortUtils.createSort(direction, field)));
    }

    @Override
    public Iterable<EmployeeDTO> getEmployeesByOrganizationIdAndPositionId(
            UUID orgId, UUID positionId, EmployeeParameters parameters
    ) {
        final var page = parameters.getPage();
        final var size = parameters.getSize();
        final var direction = parameters.getDirection();
        final var field = parameters.getField();
        return employeeRepository.findAllByPositionOrganizationIdAndPositionId(orgId, positionId,
                        PageRequest.of(page, size, SortUtils.createSort(direction, field)))
                .map(e -> employeeMapper.toDto(e, orgId));
    }

    @Override
    public EmployeeDTO validateAndGetEmployeeByIdAndOrgId(@NotNull UUID id, @NotNull UUID organizationId) {
        return employeeMapper.toDto(
                employeeRepository.findByIdAndOrganizationId(id, organizationId).orElseThrow(
                        () -> new EntityNotFoundException(Employee.class, id)), organizationId);
    }

    @Override
    public Iterable<EmployeeDTO> findByFIOLike(
            String searchString, UUID organizationId, EmployeeParameters parameters
    ) {
        final var page = parameters.getPage();
        final var size = parameters.getSize();
        final var direction = parameters.getDirection();
        final var field = parameters.getField();
        return employeeRepository.findByFIOLike(searchString, organizationId, PageRequest.of(page, size, SortUtils.createSort(direction, field)))
                .map(e -> employeeMapper.toDto(e, organizationId));
    }

    @Override
    public Iterable<EmployeeDTO> findByDepartmentAndByFIOLike(
            String searchString, UUID departmentId, EmployeeParameters parameters
    ) {
        final var page = parameters.getPage();
        final var size = parameters.getSize();
        final var direction = parameters.getDirection();
        final var field = parameters.getField();
        return employeeRepository.findByDepartmentAndByFIOLike(searchString, departmentId,
                        PageRequest.of(page, size, SortUtils.createSort(direction, field)))
                .map(e -> employeeMapper.toDto(e, e.getDepartment().getOrganization().getId()));
    }

    @Override
    public Iterable<EmployeeDTO> findByFIOLike(String searchString, EmployeeParameters parameters) {
        return employeeRepository.findByFIOLike(searchString, PageRequest.of(parameters.getPage(),
                        parameters.getSize(),
                        SortUtils.createSort(parameters.getDirection(),
                                parameters.getField())))
                .map(e -> employeeMapper.toDto(e, e.getDepartment().getOrganization().getId()));
    }



    @Override
    public Iterable<EmployeeDTO> findByFIOLike(String searchString, EmployeeParameters parameters, JwtAuthenticationToken authentication) {
        var employee = getAuthenticatedEmployee(authentication);
       return switch (employee.getOrgStructureType()) {
           case INTERNAL ->  employeeRepository.findByFIOLike(searchString,
                               employee.getOrgStructureType(),
                               PageRequest.of(parameters.getPage(),
                               parameters.getSize(),
                               SortUtils.createSort(parameters.getDirection(),
                                       parameters.getField())))
                       .map(e -> employeeMapper.toDto(e, e.getDepartment().getOrganization().getId()));

           case EXTERNAL ->employeeRepository.findByFIOLike(searchString,
                   employee.getOrgStructureType(),
                   employee.getOrganization().getId(),
                   PageRequest.of(parameters.getPage(),
                           parameters.getSize(),
                           SortUtils.createSort(parameters.getDirection(),
                                   parameters.getField())))
                   .map(e -> employeeMapper.toDto(e, e.getDepartment().getOrganization().getId()));
       };
    }
    @Override
    public Employee validateAndGetEmployeeByIdAndDepartmentId(@NotNull UUID id, @NotNull UUID departmentId) {
        return employeeRepository.findByIdAndDepartmentId(id, departmentId)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, id));
    }

    @Override
    public Iterable<Employee> getAllEmployees(EmployeeParameters parameters) {
        final var page = parameters.getPage();
        final var size = parameters.getSize();
        final var direction = parameters.getDirection();
        final var field = parameters.getField();
        return employeeRepository.findAll(PageRequest.of(page, size, SortUtils.createSort(direction, field)));
    }

    @Override
    public Iterable<Employee> getAllEmployees() {
        return employeeRepository.findAll(Pageable.unpaged());
    }

    @Override
    public void addApproval(UUID actionId, @NonNull UUID actorEmployeeId) {
        employeeRepository.getReferenceById(actorEmployeeId).getActions().add(actionId);
    }

    @Override
    public void deleteApproval(UUID actionId, UUID actorEmployeeId) {
        employeeRepository.findAllByActionsContains(actionId, Pageable.unpaged()).stream().map(Employee::getActions)
                .forEach(actions -> actions.remove(actionId));
    }

    @Override
    public boolean existsById(@NotNull UUID id) {
        return employeeRepository.existsById(id);
    }

    @Override
    public boolean existsByPersonnelNumberAndOrganizationId(String personnelNumber, UUID organizationId) {
        final var employeeOptional = employeeRepository.findByPersonnelNumberAndOrganizationId(personnelNumber, organizationId);
        return employeeOptional.isPresent();
    }

    /**
     * Проверка уникальности ТН. throws UniqueDataResponseException данные не уникальны.
     *
     * @param id              идентификатор.
     * @param personnelNumber ТН для проверки.
     */
    protected void checkPersonnelNumber(UUID id, @NonNull String personnelNumber, UUID organizationId) {
        if (id == null) {
            employeeRepository.findByPersonnelNumberAndOrganizationId(personnelNumber, organizationId)
                    .ifPresent(this::throwPersonnelNumberDuplicate);
        } else {
            employeeRepository.findByPersonnelNumberAndOrganizationIdAndIdNot(personnelNumber, organizationId, id)
                    .ifPresent(this::throwPersonnelNumberDuplicate);
        }
    }

    /**
     * Бросить исключение о дубликате ТН.
     *
     * @param employee существующие данные.
     */
    private void throwPersonnelNumberDuplicate(Employee employee) {
        throw new DuplicateDataException(employee.getClass(), "personnelNumber", employee.getPersonnelNumber());
    }

    protected void validateUserExistence(@NotNull UUID userId) {
        if (!employeeRepository.existsByUserId(userId)) {
            throw new EntityNotFoundException(Employee.class, Map.of(USER_ID_FIELD, userId));
        }
    }

    private String getHumanReadable(Employee employee) {
        var foundDepartment = employee.getDepartment();
        return sqGenerator.getNextId(Prefix.US, foundDepartment.getOrganization().getDigitId());
    }

    private void importAttributes(Set<Attribute> attributeSet) {
        if (!CollectionUtils.isEmpty(attributeSet)) {
            attributeService.mergeAll(attributeSet);
        }
    }

    @Override
    public Optional<Employee> getEmployeeByPersonalNumberAndOrganizationId(String personalNumber, UUID organizationId) {
        return employeeRepository.findByPersonnelNumberAndOrganizationId(personalNumber, organizationId);
    }

    @Override
    public List<Employee> getEmployeesWIthAttributes() {
        return employeeRepository.findAllWithAttributes();
    }

    @Override
    public OrganizationDTO getOrganizationByUserId(UUID userId) {
        return Optional.ofNullable(getEmployeeByUserId(userId).getDepartment())
                .map(Department::getOrganization).map(organizationMapper::toDto)
                .orElse(null);
    }


    @Override
    public Employee getAuthenticatedEmployee(JwtAuthenticationToken authentication) {
        var userId = UUID.fromString(authentication.getToken().getId());
        return employeeRepository.findByUserId(userId).orElseThrow(() -> new EntityNotFoundException(Employee.class, Map.of(USER_ID_FIELD, userId)));
    }

    @Override
    public void update(UUID id, AccountRoleLinkMessage message) {
        employeeRepository.findByUserId(id).ifPresent(emp -> {
            emp.setActiveStatus(message.isActive() ? ActiveStatus.ACTIVE : ActiveStatus.INACTIVE);
            saveEmployee(emp, message.getRoles());
        });
    }

    private boolean checkRightsOnSearchByOrgsAndDeps(List<UUID> organizations, Jwt token) {
        final var currentEmployee = employeeRepository.findById(UUID.fromString(token.getId()))
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, token.getId()));

        if (organizations.size() < 2 && currentEmployee.getOrganization().getId().equals(organizations.getFirst())) {
            return true;
        }

        return token.getClaimAsBoolean(ROLE_ADMIN_EXECUTOR_GROUP) != null
                ? token.getClaimAsBoolean(ROLE_ADMIN_EXECUTOR_GROUP) : false;
    }

    /**
     * Спецификация для фильтрации сотрудников.
     */
    @RequiredArgsConstructor
    private static class EmployeeFilterSpecification implements Specification<Employee> {

        @Setter
        private Collection<UUID> departmentIds;

        @Setter
        private UUID organizationId;

        @Setter
        private Collection<UUID> excludedEmployees;

        private final Map<EmployeeField, Serializable> filter;

        @Override
        public Predicate toPredicate(
                @NonNull Root<Employee> root,
                CriteriaQuery<?> query,
                @NonNull CriteriaBuilder criteriaBuilder
        ) {
            var predicate = criteriaBuilder.isNotNull(root.get(Employee_.ID));
            if (organizationId != null) {
                var department = root.join(Employee_.DEPARTMENT);
                var organization = department.join(Department_.ORGANIZATION);
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(organization.get(Organization_.ID), organizationId));
            }
            if (departmentIds != null && !departmentIds.isEmpty()) {
                var department = root.join(Employee_.DEPARTMENT);
                predicate = criteriaBuilder.and(predicate, department.get(Department_.ID).in(departmentIds));
            }
            if (excludedEmployees != null && !excludedEmployees.isEmpty()) {
                predicate = criteriaBuilder.and(predicate, root.get(Employee_.ID).in(excludedEmployees).not());
            }
            if (filter != null) {
                for (var field : filter.entrySet()) {
                    var value = field.getValue();
                    if (value == null) {
                        continue;
                    }
                    predicate = appendPredicate(root, criteriaBuilder, predicate, field.getKey(), String.valueOf(value));
                }
            }
            return predicate;
        }

        private Predicate appendPredicate(Root<Employee> root, CriteriaBuilder criteriaBuilder, Predicate predicate, EmployeeField field, String value) {
            return switch (field) {
                case PERSONNEL_NUMBER -> criteriaBuilder.and(predicate,
                        criteriaBuilder.like(root.get(Employee_.PERSONNEL_NUMBER), value + "%"));
                case FULL_NAME -> appendFullNameFilter(predicate, criteriaBuilder, root, value + "%");
                case ID -> criteriaBuilder.and(predicate, criteriaBuilder.like(root.get(Employee_.HUMAN_READABLE_ID), value + "%"));
                case STATUS -> criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get(Employee_.activeStatus), ActiveStatus.valueOf(value)));
                case MOBILE -> criteriaBuilder.and(predicate, criteriaBuilder.like(root.get(Employee_.MOBILE_PHONE), value + "%"));
                case EMAIL -> criteriaBuilder.and(predicate, criteriaBuilder.like(root.get(Employee_.EMAIL), value + "%"));
            };
        }

        /**
         * Добавляет к предикату поиск по всем возможным комбинациям имени.
         *
         * @param predicate       исходный предикат.
         * @param criteriaBuilder построитель запросов.
         * @param root            корневой элемент запроса.
         * @param value           фильтруемое значение.
         * @return предикат.
         */
        private Predicate appendFullNameFilter(Predicate predicate, CriteriaBuilder criteriaBuilder, Root<Employee> root, String value) {
            Expression<String> lastNameExtractor = root.get(Employee_.LAST_NAME);
            Expression<String> firstNameExtractor = root.get(Employee_.FIRST_NAME);
            Expression<String> patronymicExtractor = root.get(Employee_.PATRONYMIC);

            var fullNameLikePredicate = switch (value.split(" ").length) {
                case 1 -> simpleNamePredicate(criteriaBuilder, value, lastNameExtractor, firstNameExtractor, patronymicExtractor);
                case 2 -> twoNamePredicate(criteriaBuilder, value, lastNameExtractor, firstNameExtractor, patronymicExtractor);
                default -> fullNamePredicate(criteriaBuilder, value, lastNameExtractor, firstNameExtractor, patronymicExtractor);
            };

            return criteriaBuilder.and(predicate, fullNameLikePredicate);
        }

        /**
         * Добавляет к предикату поиск по ФИО или ИФО.
         *
         * @param criteriaBuilder     построитель запросов.
         * @param value               фильтруемое значение.
         * @param firstNameExtractor  объект с данными имени.
         * @param lastNameExtractor   объект с данными фамилии.
         * @param patronymicExtractor объект с данными отчества.
         * @return предикат.
         */
        private Predicate fullNamePredicate(
                CriteriaBuilder criteriaBuilder, String value, Expression<String> lastNameExtractor, Expression<String> firstNameExtractor,
                Expression<String> patronymicExtractor
        ) {
            var lastFirstPatronymic = criteriaBuilder.concat(criteriaBuilder.concat(lastNameExtractor, firstNameExtractor), patronymicExtractor);
            var firstLastPatronymic = criteriaBuilder.concat(criteriaBuilder.concat(lastNameExtractor, firstNameExtractor), patronymicExtractor);
            return criteriaBuilder.or(criteriaBuilder.like(lastFirstPatronymic, value),
                    criteriaBuilder.like(firstLastPatronymic, value));
        }

        /**
         * Добавляет к предикату поиск по ИО, ИФ или ФИ.
         *
         * @param criteriaBuilder     построитель запросов.
         * @param value               фильтруемое значение.
         * @param firstNameExtractor  объект с данными имени.
         * @param lastNameExtractor   объект с данными фамилии.
         * @param patronymicExtractor объект с данными отчества.
         * @return предикат.
         */
        private Predicate twoNamePredicate(
                CriteriaBuilder criteriaBuilder, String value, Expression<String> lastNameExtractor, Expression<String> firstNameExtractor,
                Expression<String> patronymicExtractor
        ) {
            var lastFirstName = criteriaBuilder.concat(lastNameExtractor, firstNameExtractor);
            var firstLastName = criteriaBuilder.concat(firstNameExtractor, lastNameExtractor);
            var firstPatronymicName = criteriaBuilder.concat(firstNameExtractor, patronymicExtractor);
            return criteriaBuilder.or(criteriaBuilder.like(lastFirstName, value),
                    criteriaBuilder.like(firstLastName, value),
                    criteriaBuilder.like(firstPatronymicName, value));
        }

        /**
         * Добавляет к предикату поиск по И, Ф или О.
         *
         * @param criteriaBuilder     построитель запросов.
         * @param value               фильтруемое значение.
         * @param firstNameExtractor  объект с данными имени.
         * @param lastNameExtractor   объект с данными фамилии.
         * @param patronymicExtractor объект с данными отчества.
         * @return предикат.
         */
        private Predicate simpleNamePredicate(
                CriteriaBuilder criteriaBuilder, String value, Expression<String> lastNameExtractor, Expression<String> firstNameExtractor,
                Expression<String> patronymicExtractor
        ) {
            return criteriaBuilder.or(criteriaBuilder.like(lastNameExtractor, value),
                    criteriaBuilder.like(firstNameExtractor, value),
                    criteriaBuilder.like(patronymicExtractor, value));
        }
    }
}
