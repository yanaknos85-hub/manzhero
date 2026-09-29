package ru.sber.transport.tariff_fleet.database.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.constant.InspectionType;
import ru.sber.transport.tariff_fleet.database.model.Contract_;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbContractGetDto;
import ru.sber.transport.tariff_fleet.mapper.EwbContractMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres
@SpringBootTest
@Sql({ "/scripts/db_cleanup.sql",
       "/scripts/basic_corp_structure.sql",
       "/scripts/edf_operator.sql",
       "/scripts/ewb_contract.sql" })
class EwbContractRepositoryTest {
    
    @Autowired
    private EwbContractRepository ewbContractRepository;
    @Autowired
    private EwbContractMapper ewbContractMapper;
    
    @Test
    void existsByOrganizationIdAndInspectionTypeAndContract_Number() {
        assertThat(ewbContractRepository.existsByOrganizationIdAndInspectionTypeAndContract_NumberAndContract_ActiveTrue(
                UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"),
                InspectionType.MEDIC,
                "11111")).isTrue();
        assertThat(ewbContractRepository.existsByOrganizationIdAndInspectionTypeAndContract_NumberAndContract_ActiveTrue(
                UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"),
                InspectionType.MEDIC,
                "11111")).isFalse();
        assertThat(ewbContractRepository.existsByOrganizationIdAndInspectionTypeAndContract_NumberAndContract_ActiveTrue(
                UUID.fromString("20bfb1f4-6099-45e8-8d81-c42df5070763"),
                InspectionType.MEDIC,
                "99999")).isFalse();
    }

    @MethodSource("searchEwbContracts")
    @ParameterizedTest
    void searchEwbContracts(
            UUID organizationId,
            String inspectionType,
            String number,
            LocalDate start,
            LocalDate end,
            Boolean active,
            Pageable pageable,
            int totalElements,
            int size,
            int totalPages,
            int pageNumber,
            int numberOfElements,
            Sort sort,
            List<EwbContractGetDto> expected
                           ) {
        var actual = ewbContractRepository.searchEwbContracts(organizationId,
                                                              inspectionType,
                                                              number,
                                                              start,
                                                              end,
                                                              active,
                                                              pageable);
        assertThat(actual.getTotalElements()).isEqualTo(totalElements);
        assertThat(actual.getSize()).isEqualTo(size);
        assertThat(actual.getTotalPages()).isEqualTo(totalPages);
        assertThat(actual.getNumber()).isEqualTo(pageNumber);
        assertThat(actual.getNumberOfElements()).isEqualTo(numberOfElements);
        assertThat(actual.getSort()).isEqualTo(sort);
        assertThat(actual.getPageable()).isEqualTo(pageable);
        assertThat(ewbContractMapper.listGetEwbContractProjectionToListEwbContractGetDto(actual.getContent()))
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
    
    static Stream<Arguments> searchEwbContracts() {
        var organizationId1 = UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6");
        var organizationId2 = UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396");
        var contractDto1 = new EwbContractGetDto(UUID.fromString("5627f0b0-cac0-49e2-be7f-db026fa42435"),
                                                 "11111",
                                                 LocalDate.of(2025, 3, 15),
                                                 LocalDate.now().plusYears(1),
                                                 true,
                                                 "ЦА",
                                                 33333L,
                                                 InspectionType.MEDIC.name());
        var contractDto2 = new EwbContractGetDto(UUID.fromString("20bfb1f4-6099-45e8-8d81-c42df5070763"),
                                                 "99999",
                                                 LocalDate.of(2024, 4, 15),
                                                 LocalDate.of(2024, 5, 15),
                                                 false,
                                                 "ЦА",
                                                 99999L,
                                                 InspectionType.MEDIC.name());
        var contractDto3 = new EwbContractGetDto(UUID.fromString("e921aa12-0668-4bac-856c-c3d851882911"),
                                                 "9999",
                                                 LocalDate.of(2024, 4, 15),
                                                 LocalDate.now().plusYears(2),
                                                 true,
                                                 "Тест2",
                                                 999999L,
                                                 InspectionType.TECHNIC.name());
        var contractDto4 = new EwbContractGetDto(UUID.fromString("80cea1a8-b869-49d0-b0dd-de0079fbb134"),
                                                 "9999",
                                                 LocalDate.now().minusDays(1),
                                                 LocalDate.now().plusDays(1),
                                                 false,
                                                 "ЦА",
                                                 99999L,
                                                 InspectionType.MEDIC.name());
        var contractDto5 = new EwbContractGetDto(UUID.fromString("be8ba3fc-5064-4ad9-9972-7aa33079516d"),
                                                 "9999",
                                                 LocalDate.now().minusDays(2),
                                                 LocalDate.now().minusDays(1),
                                                 true,
                                                 "Тест2",
                                                 999999L,
                                                 InspectionType.TECHNIC.name());
        return Stream.of(
                Arguments.of(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 20, Sort.by(Contract_.NUMBER).ascending()),
                        5,
                        20,
                        1,
                        0,
                        5,
                        Sort.by(Contract_.NUMBER).ascending(),
                        List.of(contractDto1, contractDto3, contractDto4, contractDto5, contractDto2)),
                Arguments.of(
                        organizationId1,
                        null,
                        null,
                        null,
                        null,
                        true,
                        PageRequest.of(0, 20, Sort.by(Contract_.NUMBER).ascending()),
                        1,
                        20,
                        1,
                        0,
                        1,
                        Sort.by(Contract_.NUMBER).ascending(),
                        List.of(contractDto1)),
                Arguments.of(
                        null,
                        InspectionType.TECHNIC.name(),
                        null,
                        null,
                        null,
                        true,
                        PageRequest.of(0, 20, Sort.by(Contract_.NUMBER).ascending()),
                        2,
                        20,
                        1,
                        0,
                        2,
                        Sort.by(Contract_.NUMBER).ascending(),
                        List.of(contractDto3, contractDto5)),
                Arguments.of(
                        null,
                        null,
                        "11111",
                        null,
                        null,
                        true,
                        PageRequest.of(0, 20, Sort.by(Contract_.NUMBER).ascending()),
                        1,
                        20,
                        1,
                        0,
                        1,
                        Sort.by(Contract_.NUMBER).ascending(),
                        List.of(contractDto1)),
                Arguments.of(
                        null,
                        null,
                        null,
                        LocalDate.of(2025, 3, 15),
                        LocalDate.now().plusYears(1),
                        true,
                        PageRequest.of(0, 20, Sort.by(Contract_.NUMBER).ascending()),
                        2,
                        20,
                        1,
                        0,
                        2,
                        Sort.by(Contract_.NUMBER).ascending(),
                        List.of(contractDto1, contractDto5)),
                Arguments.of(
                        null,
                        null,
                        null,
                        LocalDate.of(2024, 4, 15),
                        LocalDate.of(2029, 6, 15),
                        true,
                        PageRequest.of(0, 20, Sort.by(Contract_.NUMBER).ascending()),
                        3,
                        20,
                        1,
                        0,
                        3,
                        Sort.by(Contract_.NUMBER).ascending(),
                        List.of(contractDto1, contractDto3, contractDto5)),
                Arguments.of(
                        null,
                        null,
                        null,
                        LocalDate.of(2023, 3, 15),
                        LocalDate.now().plusYears(1),
                        true,
                        PageRequest.of(0, 20, Sort.by(Contract_.NUMBER).ascending()),
                        2,
                        20,
                        1,
                        0,
                        2,
                        Sort.by(Contract_.NUMBER).ascending(),
                        List.of(contractDto1, contractDto5)),
                Arguments.of(
                        null,
                        null,
                        null,
                        null,
                        null,
                        false,
                        PageRequest.of(0, 20, Sort.by(Contract_.NUMBER).ascending()),
                        2,
                        20,
                        1,
                        0,
                        2,
                        Sort.by(Contract_.NUMBER).ascending(),
                        List.of(contractDto4, contractDto2)),
                Arguments.of(
                        null,
                        null,
                        null,
                        null,
                        null,
                        true,
                        PageRequest.of(1, 1, Sort.by(Contract_.NUMBER).ascending()),
                        3,
                        1,
                        3,
                        1,
                        1,
                        Sort.by(Contract_.NUMBER).ascending(),
                        List.of(contractDto3)),
                Arguments.of(
                        organizationId2,
                        InspectionType.TECHNIC.name(),
                        "9999",
                        LocalDate.of(2024, 3, 15),
                        LocalDate.now().plusYears(2),
                        true,
                        PageRequest.of(0, 20, Sort.by(Contract_.NUMBER).ascending()),
                        2,
                        20,
                        1,
                        0,
                        2,
                        Sort.by(Contract_.NUMBER).ascending(),
                        List.of(contractDto3, contractDto5)),
                Arguments.of(
                        null,
                        null,
                        "9999",
                        null,
                        null,
                        true,
                        PageRequest.of(0, 20, JpaSort.unsafe(Sort.Direction.ASC, "(organizationName)")),
                        2,
                        20,
                        1,
                        0,
                        2,
                        JpaSort.unsafe(Sort.Direction.ASC, "(organizationName)"),
                        List.of(contractDto3, contractDto5))
                        );
    }
    
    @Test
    void findByIdWithContract() {
        assertThat(ewbContractRepository.findByIdWithContract(UUID.fromString("5627f0b0-cac0-49e2-be7f-db026fa42435"))).isPresent();
        assertThat(ewbContractRepository.findByIdWithContract(UUID.randomUUID())).isNotPresent();
    }
    
    @Test
    void findByIdWithMedicalLicense() {
        assertThat(ewbContractRepository.findByIdWithMedicalLicense(UUID.fromString("5627f0b0-cac0-49e2-be7f-db026fa42435"))).isPresent();
        assertThat(ewbContractRepository.findByIdWithMedicalLicense(UUID.randomUUID())).isNotPresent();
    }
}