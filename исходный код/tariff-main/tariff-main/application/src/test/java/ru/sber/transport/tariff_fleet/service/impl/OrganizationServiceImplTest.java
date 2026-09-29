package ru.sber.transport.tariff_fleet.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.database.dao.OrganizationRepository;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.database.model.OrganizationNameWithDepartmentInfo;
import ru.sber.transport.tariff_fleet.dto.GetAllActiveOrganizationNamesDto;
import ru.sber.transport.tariff_fleet.dto.OrganizationsDepartmentsSearchDto;
import ru.sber.transport.tariff_fleet.exception.DigitIdNotFoundException;
import ru.sber.transport.tariff_fleet.mapper.OrganizationMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.sber.transport.tariff_fleet.TestData.createOrganization2;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса по работе с организациями")
class OrganizationServiceImplTest {

    @InjectMocks
    private OrganizationServiceImpl service;
    @Mock
    private OrganizationRepository repository;
    @Mock
    private OrganizationMapper mapper;
    private Organization organization;

    @BeforeEach
    void setup() {
        organization = Instancio.create(Organization.class);
    }

    @Test
    void get() {
        when(repository.findById(organization.getId())).thenReturn(Optional.of(organization));
        assertEquals(service.get(organization.getId()), Optional.of(organization));
        assertEquals(service.get(randomUUID()), Optional.empty());
    }

    @Test
    void delete() {
        when(repository.save(any())).thenReturn(organization);
        service.delete(organization);
        verify(repository).save(any());
    }

    @Test
    void saveOrUpdate() {
        var updateOrganization1 = Organization.builder()
                .id(organization.getId())
                .officialName("officialName1Updated")
                .digitId(3L)
                .build();
        var organization2 = createOrganization2();
        when(repository.findById(organization.getId())).thenReturn(Optional.of(organization));
        when(repository.findById(organization2.getId())).thenReturn(Optional.empty());
        when(repository.save(updateOrganization1)).thenReturn(updateOrganization1);
        when(repository.save(organization2)).thenReturn(organization2);
        service.saveOrUpdate(updateOrganization1);
        service.saveOrUpdate(organization2);
        verify(repository).findById(organization2.getId());
        verify(repository).findById(organization2.getId());
        verify(repository).save(updateOrganization1);
        verify(repository).save(organization2);
    }

    @Test
    void getAllActiveNames() {
        when(repository.findAllByActiveTrueOrderByOfficialName()).thenReturn(List.of(organization));
        when(mapper.organizationToGetAllActiveOrganizationNamesDto(organization))
                .thenReturn(new GetAllActiveOrganizationNamesDto(organization.getId(), organization.getOfficialName()));
        var allActiveNames = service.getAllActiveNames();
        verify(repository).findAllByActiveTrueOrderByOfficialName();
        assertTrue(allActiveNames.stream().anyMatch(o -> o.id().equals(organization.getId())
                && o.name().equals(organization.getOfficialName())));
    }

    @Test
    void getDepartmentsInfoEmpty() {
        when(repository.findByIdsWithActiveDepartments(any())).thenReturn(List.of());
        var emptyList = service.getDepartmentsInfo(new OrganizationsDepartmentsSearchDto(List.of(randomUUID())));
        assertTrue(emptyList.isEmpty());
    }

    @Test
    void getDepartmentsInfoSingle() {
        var organization3 = Instancio.create(Organization.class);
        var singleItem = List.of(generateDepartment(organization3, "Department A"));
        when(repository.findByIdsWithActiveDepartments(any())).thenReturn(singleItem);

        var oneRowList = service.getDepartmentsInfo(new OrganizationsDepartmentsSearchDto(List.of(organization3.getId())));

        assertEquals(1, oneRowList.size());
        assertEquals(organization3.getOfficialName(), oneRowList.get(0).organizationName());
        assertEquals(1, oneRowList.get(0).departmentList().size());
    }

    @Test
    void getDepartmentsInfo() {
        var organization1 = Instancio.create(Organization.class);
        var organization2 = Instancio.create(Organization.class);

        var depB = generateDepartment(organization1, "Department B");
        var depC = generateDepartment(organization1, "Department C");
        var depD = generateDepartment(organization2, "Department D");
        when(repository.findByIdsWithActiveDepartments(any())).thenReturn(List.of(depB, depC, depD));

        var result = service.getDepartmentsInfo(new OrganizationsDepartmentsSearchDto(List.of(organization1.getId())));

        assertEquals(2, result.size());
        var resultOrg1 = result.stream()
                .filter(o -> o.organizationName().equals(organization1.getOfficialName()))
                .findFirst();
        assertTrue(resultOrg1.isPresent());
        assertEquals(organization1.getOfficialName(), resultOrg1.get().organizationName());
        assertEquals(2, resultOrg1.get().departmentList().size());
        var resultOrg2 = result.stream()
                .filter(o -> o.organizationName().equals(organization2.getOfficialName()))
                .findFirst();
        assertTrue(resultOrg2.isPresent());
        assertEquals(organization2.getOfficialName(), resultOrg2.get().organizationName());
        assertEquals(1, resultOrg2.get().departmentList().size());
    }

    @Test
    void getDigitIdByUserId() {
        var userId = UUID.randomUUID();
        var wrongUserId = UUID.randomUUID();
        doReturn(Optional.of(8L)).when(repository).findDigitIdByUserId(userId);
        doReturn(Optional.empty()).when(repository).findDigitIdByUserId(wrongUserId);
        assertThat(service.getDigitIdByUserId(userId)).isEqualTo(8L);
        assertThatThrownBy(() -> service.getDigitIdByUserId(wrongUserId))
                .isInstanceOf(DigitIdNotFoundException.class)
                .hasMessage(String.format("Не найден уникальный числовой идентификатор организации сотрудника, userId:%s", wrongUserId));
    }

    @Test
    void existsById() {
        var id = UUID.randomUUID();
        var wrongId = UUID.randomUUID();
        doReturn(true).when(repository).existsById(id);
        doReturn(false).when(repository).existsById(wrongId);
        assertThat(service.existsById(id)).isTrue();
        assertThat(service.existsById(wrongId)).isFalse();
    }

    private static OrganizationNameWithDepartmentInfo generateDepartment(Organization organization, String departmentName) {
        return new OrganizationNameWithDepartmentInfo(
                organization.getId(),
                organization.getOfficialName(),
                randomUUID(),
                departmentName,
                null
        );
    }

}