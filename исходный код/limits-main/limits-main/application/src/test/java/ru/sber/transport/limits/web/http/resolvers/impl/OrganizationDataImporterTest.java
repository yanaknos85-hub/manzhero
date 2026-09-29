package ru.sber.transport.limits.web.http.resolvers.impl;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.dao.*;
import ru.sberbank.ditsib.transport.limits.dto.file.LimitDataFileDto;
import ru.sberbank.ditsib.transport.limits.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.transport.limits.model.basic.Department;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.basic.Organization;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.EmpLimit;
import ru.sberbank.ditsib.transport.limits.model.limit.Limit;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitSharing;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationDataImporterTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private LimitRepository<Limit> limitRepository;
    @Mock
    private LimitSharingRepository limitSharingRepository;
    @Mock
    private LimitSharingPerPeriodRepository limitSharingPerPeriodRepository;
    @Mock
    private SQGenerator sqGenerator;

    @InjectMocks
    private OrganizationDataImporter organizationDataImporter;

    @Captor
    private ArgumentCaptor<Limit> limitArgumentCaptor;

    @Test
    @SneakyThrows
    void add() {
        var source = Instancio.of(LimitDataFileDto.class)
                .set(field(LimitDataFileDto::getLimitType), "EMP")
                .create();
        var employee = Instancio.of(Employee.class).create();
        var department = Instancio.of(Department.class).create();
        var limit = Instancio.of(EmpLimit.class).create();

        var field = organizationDataImporter.getClass().getDeclaredField("departmentUuids");
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        var departmentMap = (Map<UUID, Department>) field.get(organizationDataImporter);
        departmentMap.put(employee.getDepartmentId(), department);

        field = organizationDataImporter.getClass().getDeclaredField("organizationId");
        field.setAccessible(true);
        field.set(organizationDataImporter, employee.getOrganizationId());

        field = organizationDataImporter.getClass().getDeclaredField("depLimits");
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        var depLimits = (Map<String, Limit>) field.get(organizationDataImporter);
        depLimits.put(department.getCode(), limit);

        doReturn(List.of(employee)).when(employeeRepository).findByPersonnelNumber(source.getDepartmentCode());


        var actual = organizationDataImporter.add(source);
        assertThat(actual).isTrue();
    }

    @Test
    void persist_file_is_empty() {
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> organizationDataImporter.persist())
                .withMessage("Файл импорта пуст");
    }

    @Test
    @SneakyThrows
    void persist() {
        var department = Instancio.of(Department.class).create();
        var limitLevel1_0 = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getSum), BigDecimal.ZERO)
                .create();
        var limitLevel2_0 = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getParent), limitLevel1_0)
                .set(field(DepLimit::getSum), BigDecimal.ZERO)
                .create();
        var limitLevel2_1 = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getParent), limitLevel1_0)
                .set(field(DepLimit::getSum), BigDecimal.ZERO)
                .create();
        var limitLevel3_0 = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getParent), limitLevel2_0)
                .set(field(DepLimit::getSharings), List.of(
                        Instancio.of(LimitSharing.class)
                                .set(field(LimitSharing::getTransportType), TransportTypeEnum.TAXI)
                                .set(field(LimitSharing::getSum), BigDecimal.valueOf(1000))
                                .create()
                ))
                .create();
        var limitLevel3_1 = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getParent), limitLevel2_1)
                .set(field(DepLimit::getSharings), List.of(
                        Instancio.of(LimitSharing.class)
                                .set(field(LimitSharing::getTransportType), TransportTypeEnum.TAXI)
                                .set(field(LimitSharing::getSum), BigDecimal.valueOf(1000))
                                .create()
                ))
                .create();
        var limitLevel3_2 = Instancio.of(DepLimit.class)
                .set(field(DepLimit::getParent), limitLevel2_1)
                .set(field(DepLimit::getSharings), List.of(
                        Instancio.of(LimitSharing.class)
                                .set(field(LimitSharing::getTransportType), TransportTypeEnum.TAXI)
                                .set(field(LimitSharing::getSum), BigDecimal.valueOf(1000))
                                .create()
                ))
                .create();

        var field = organizationDataImporter.getClass().getDeclaredField("depLimits");
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        var depLimits = (Map<String, Limit>) field.get(organizationDataImporter);
        depLimits.put(department.getCode(), limitLevel1_0);

        field = organizationDataImporter.getClass().getDeclaredField("parents");
        field.setAccessible(true);

        @SuppressWarnings("unchecked")
        var parents = (Map<String, Set<Limit>>) field.get(organizationDataImporter);
        parents.put(null, Set.of(limitLevel1_0));
        parents.put(limitLevel1_0.getDepartment().getCode(), Set.of(limitLevel2_0, limitLevel2_1));
        parents.put(limitLevel2_0.getDepartment().getCode(), Set.of(limitLevel3_0));
        parents.put(limitLevel2_1.getDepartment().getCode(), Set.of(limitLevel3_1, limitLevel3_2));

        var organization = Instancio.of(Organization.class).create();
        field = organizationDataImporter.getClass().getDeclaredField("organizationId");
        field.setAccessible(true);
        field.set(organizationDataImporter, organization.getId());
        var employee = Instancio.of(Employee.class).create();

        doReturn(organization).when(organizationRepository).getReferenceById(organization.getId());
        doReturn(employee).when(employeeRepository).getReferenceById(any(UUID.class));
        doReturn(department).when(departmentRepository).getReferenceById(any(UUID.class));
        doReturn("%s-%04d-%08d".formatted("LD", organization.getDigitId(), 0))
                .when(sqGenerator).getNextId(any(Prefix.class), any(Long.class));
        doAnswer(arg -> arg.getArguments()[0]).when(limitRepository).save(any(DepLimit.class));
        doAnswer(arg -> arg.getArguments()[0]).when(limitSharingRepository).save(any(LimitSharing.class));
        doAnswer(arg -> arg.getArguments()[0]).when(limitSharingPerPeriodRepository).saveAll(anyList());

        assertThatCode(() -> organizationDataImporter.persist())
                .doesNotThrowAnyException();

        verify(limitRepository, times(11)).save(limitArgumentCaptor.capture());
        var capturedLimits = limitArgumentCaptor.getAllValues();

        capturedLimits.forEach(limit -> {
            if (limit.getHash().equals(limitLevel3_2.getHash()) || limit.getHash().equals(limitLevel3_1.getHash()) || limit.getHash().equals(limitLevel3_0.getHash())) {
                assertThat(limit.getSum()).isEqualTo(BigDecimal.valueOf(1000));
            }
            if (limit.getHash().equals(limitLevel2_1.getHash())) {
                assertThat(limit.getSum()).isEqualTo(BigDecimal.valueOf(2000));
            }
            if (limit.getHash().equals(limitLevel2_0.getHash())) {
                assertThat(limit.getSum()).isEqualTo(BigDecimal.valueOf(1000));
            }
            if (limit.getHash().equals(limitLevel1_0.getHash())) {
                assertThat(limit.getSum()).isEqualTo(BigDecimal.valueOf(3000));
            }
        });
    }
}