package ru.sber.transport.tariff_fleet.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.constant.ServiceType;
import ru.sber.transport.tariff_fleet.database.dao.ContractorRepository;
import ru.sber.transport.tariff_fleet.database.model.Contractor;
import ru.sber.transport.tariff_fleet.database.model.Employee;
import ru.sber.transport.tariff_fleet.database.model.Organization;
import ru.sber.transport.tariff_fleet.dto.ContractorDto;
import ru.sber.transport.tariff_fleet.exception.ContractorNotFoundException;
import ru.sber.transport.tariff_fleet.mapper.ContractorMapper;
import ru.sber.transport.tariff_fleet.service.EmployeeService;
import ru.sber.transport.tariff_fleet.service.validation.ContractValidationService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.sber.transport.tariff_fleet.constant.DocumentType.REPAIR_AND_MAINTENANCE;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса по работе с контрагентами")
class ContractorServiceImplTest {

    @InjectMocks
    private ContractorServiceImpl service;
    @Mock
    private ContractValidationService contractValidationService;
    @Mock
    private ContractorRepository repository;
    @Mock
    private ContractorMapper contractorMapper;
    @Mock
    private EmployeeService employeeService;

    @Test
    void get() {
        var id = UUID.randomUUID();
        service.get(id);
        verify(repository).findById(id);
    }

    @Test
    void getById() {
        var id = UUID.randomUUID();
        doReturn(Optional.empty()).when(repository).findById(id);
        assertThrows(ContractorNotFoundException.class, () -> service.getById(id));
    }


    @Test
    void delete() {
        var entity = Instancio.create(Contractor.class);
        service.delete(entity);
        verify(repository).save(entity.setActive(false));
    }

    @Test
    void save() {
        var entity = Instancio.create(Contractor.class);
        service.save(entity);
        verify(repository).save(entity);
    }

    @Test
    void getAllActive() {
        var contractor = Instancio.create(Contractor.class);
        doReturn(List.of(contractor))
                .when(repository).findAllByServiceTypeAndActiveTrueOrderByNameAsc(ServiceType.AUTOSERVICE);
        var contractorDto = Instancio.create(ContractorDto.class);
        doReturn(contractorDto)
                .when(contractorMapper).contractorToContractorDto(contractor);
        var actual = service.getAllActive();
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(List.of(contractorDto));
    }

    @Test
    void testFindAllOrganizationsByActiveContracts() {
        doNothing().when(contractValidationService).validateFindByDocumentType(REPAIR_AND_MAINTENANCE);
        when(repository.findContractorsByFilter(any())).thenReturn(List.of(new Contractor()));
        var result = service.findAllOrganizationsByActiveContractsAndServiceTypeAndDocumentType(
                ServiceType.AUTOSERVICE, REPAIR_AND_MAINTENANCE);

        assertThat(result).hasSize(1);
    }

    @Test
    void testFindSelfOrganizationsByActiveContracts() {
        var userId = UUID.randomUUID();
        doNothing().when(contractValidationService).validateFindByDocumentType(REPAIR_AND_MAINTENANCE);
        when(repository.findContractorsByFilter(any())).thenReturn(List.of(new Contractor()));
        when(employeeService.getByUserId(userId)).thenReturn(new Employee(
                userId, null, null, null, null, null, null, null, null, null, true, new Organization(UUID.randomUUID(), null, null, true), null
        ));
        var result = service.findSelfOrganizationsByActiveContractsAndServiceTypeAndDocumentType(
                ServiceType.AUTOSERVICE, REPAIR_AND_MAINTENANCE, userId);

        assertThat(result).hasSize(1);
    }
}
