package ru.sberbank.ditsib.corpclient.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.sberbank.ditsib.corpclient.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.corpclient.database.model.ActiveStatus;
import ru.sberbank.ditsib.corpclient.database.model.Department;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.dto.DepartmentParameters;
import ru.sberbank.ditsib.corpclient.dto.DepartmentProjection;
import ru.sberbank.ditsib.corpclient.mapper.ActiveStatusMapperImpl;
import ru.sberbank.ditsib.corpclient.mapper.DepartmentMapper;
import ru.sberbank.ditsib.corpclient.mapper.DepartmentMapperImpl;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса департаментов метода getDepartmentsDto")
class DepartmentServiceImplGetDepartmentsDtoTest {
    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper = new DepartmentMapperImpl(new ActiveStatusMapperImpl());

    @Test
    @DisplayName("Проверка поиска департаментов")
    void test_getDepartmentsDto() {
        var orgId = UUID.randomUUID();
        var departmentName = "depName";
        var humanReadableId = "DT-S-001";

        var organization = new Organization();
        organization.setAddress("address" + 1);
        organization.setOfficialName("name" + 1);
        organization.setMsrn("msrn");
        organization.setTid("tid");
        organization.setId(orgId);

        Organization finalOrganization = organization;
        List<Department> departmentList = IntStream.range(0, 30).mapToObj(i -> {
            int smallIndex = i / 10;
            var department = new Department();
            department.setOrganization(finalOrganization);
            department.setName(departmentName + "%s_%s".formatted(smallIndex, i));
            department.setCode("Co%sde%s".formatted(smallIndex, i));
            department.setHumanReadableId(humanReadableId + "%s-%s".formatted(smallIndex, i));
            department.setLocation("Loca%stion%s".formatted(smallIndex, i));
            department.setUpdateTime(OffsetDateTime.now());
            department.setActiveStatus(i < 20 ? ActiveStatus.ACTIVE : ActiveStatus.INACTIVE);
            return department;
        }).toList();

        var parameters = new DepartmentParameters();

        assertThat(departmentService.getDepartmentsDto(
                Set.of(organization.getId()), null,
                parameters, null)).isEmpty();

        verify(departmentRepository, times(0))
                .findAll((Specification<Department>) any(), (Pageable) any());

        when(departmentRepository.findAll((Specification) any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(departmentList, Pageable.unpaged(), departmentList.size()));
        assertThat(departmentService.getDepartmentsDto(Set.of(organization.getId()), null, parameters, DepartmentProjection.MIN))
                .hasSize(departmentList.size());
        verify(departmentRepository, times(1))
                .findAll((Specification<Department>) any(), (Pageable) any());
        verify(departmentMapper, times(30)).departmentToSelectDTO(any(Department.class));

    }
}
