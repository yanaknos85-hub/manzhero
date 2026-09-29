package ru.sberbank.ditsib.corpclient.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.corpclient.database.dao.*;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.TripPurpose;
import ru.sberbank.ditsib.corpclient.dto.mapper.PurposeMapper;
import ru.sberbank.ditsib.corpclient.dto.purpose.TripPurposeDTO;
import ru.sberbank.ditsib.corpclient.messaging.sender.PurposeSender;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class TripPurposeServiceImplTest {
    @InjectMocks
    private TripPurposeServiceImpl tripPurposeService;

    @Mock
    private TripPurposeRepository tripPurposeRepository;
    @Mock
    private TripPurposeAttributeRepository tripPurposeAttributeRepository;
    @Mock
    private TripPurposeDepartmentRepository tripPurposeDepartmentRepository;
    @Mock
    private TripPurposeDateRepository tripPurposeDateRepository;
    @Mock
    private TripPurposeTimeRepository tripPurposeTimeRepository;
    @Mock
    private TripPurposeWeekdayRepository tripPurposeWeekdayRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private AttributeRepository attributeRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private PurposeSender purposeSender;
    @Mock
    private PurposeMapper purposeMapper;
    @Mock
    private EmployeeRepository employeeRepository;

    @Test
    void findAllByAttributes() {
        var organizationId1 = UUID.randomUUID();
        var organizationId2 = UUID.randomUUID();
        var organizationId3 = UUID.randomUUID();
        var userId1 = UUID.randomUUID();
        var userId2 = UUID.randomUUID();
        var userId3 = UUID.randomUUID();
        var userId4 = UUID.randomUUID();
        var employee1 = Instancio.create(Employee.class);
        var employee2 = Instancio.of(Employee.class)
                .ignore(field(Employee::getAttributes))
                .create();
        var employee3 = Instancio.of(Employee.class)
                .set(field(Employee::getAttributes), null)
                .create();
        var tripPurposeList1 = new LinkedList<>(Instancio.ofList(TripPurpose.class)
                .size(3)
                .create());
        var tripPurposeList2 = Instancio.ofList(TripPurpose.class)
                .size(3)
                .create();
        var tripPurposeDTO1 = Instancio.create(TripPurposeDTO.class);
        var tripPurposeDTO2 = Instancio.create(TripPurposeDTO.class);
        var tripPurposeDTO3 = Instancio.create(TripPurposeDTO.class);
        var tripPurposeDTO4 = Instancio.create(TripPurposeDTO.class);
        var tripPurposeDTO5 = Instancio.create(TripPurposeDTO.class);
        var tripPurposeDTO6 = Instancio.create(TripPurposeDTO.class);
        doReturn(false).when(organizationRepository).existsById(organizationId1);
        doReturn(true).when(organizationRepository).existsById(organizationId2);
        doReturn(true).when(organizationRepository).existsById(organizationId3);
        doReturn(Optional.empty()).when(employeeRepository).findByUserId(userId4);
        doReturn(Optional.of(employee1)).when(employeeRepository).findByUserId(userId1);
        doReturn(Optional.of(employee2)).when(employeeRepository).findByUserId(userId2);
        doReturn(Optional.of(employee3)).when(employeeRepository).findByUserId(userId3);
        doReturn(tripPurposeList1).when(tripPurposeRepository).findAllByAttributesAndStatistic(anySet(),
                any(),
                any());
        doReturn(tripPurposeList2).when(tripPurposeRepository).findAllByEmptyAttributes(organizationId2);
        doReturn(tripPurposeList2).when(tripPurposeRepository).findAllByEmptyAttributes(organizationId3);
        doReturn(tripPurposeDTO1).when(purposeMapper).tripPurposeToDTO(tripPurposeList1.get(0));
        doReturn(tripPurposeDTO2).when(purposeMapper).tripPurposeToDTO(tripPurposeList1.get(1));
        doReturn(tripPurposeDTO3).when(purposeMapper).tripPurposeToDTO(tripPurposeList1.get(2));
        doReturn(tripPurposeDTO4).when(purposeMapper).tripPurposeToDTO(tripPurposeList2.get(0));
        doReturn(tripPurposeDTO5).when(purposeMapper).tripPurposeToDTO(tripPurposeList2.get(1));
        doReturn(tripPurposeDTO6).when(purposeMapper).tripPurposeToDTO(tripPurposeList2.get(2));
        assertThatThrownBy(() -> tripPurposeService.findAllByAttributes(userId1, organizationId1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Data not found: Entity: Organization, ID: %s", organizationId1);
        assertThatThrownBy(() -> tripPurposeService.findAllByAttributes(userId4, organizationId2))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Data not found: Entity: Employee, ID: {userId=%s}", userId4);
        assertThat(tripPurposeService.findAllByAttributes(userId1, organizationId2))
                .usingRecursiveComparison()
                .isEqualTo(List.of(tripPurposeDTO1, tripPurposeDTO2, tripPurposeDTO3));
        assertThat(tripPurposeService.findAllByAttributes(userId2, organizationId2))
                .usingRecursiveComparison()
                .isEqualTo(List.of(tripPurposeDTO4, tripPurposeDTO5, tripPurposeDTO6));
        assertThat(tripPurposeService.findAllByAttributes(userId3, organizationId3))
                .usingRecursiveComparison()
                .isEqualTo(List.of(tripPurposeDTO4, tripPurposeDTO5, tripPurposeDTO6));
    }
}