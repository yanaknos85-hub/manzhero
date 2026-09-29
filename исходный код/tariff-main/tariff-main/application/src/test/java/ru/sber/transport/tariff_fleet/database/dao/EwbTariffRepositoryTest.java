package ru.sber.transport.tariff_fleet.database.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.constant.InspectionType;
import ru.sber.transport.tariff_fleet.database.model.Tariff_;
import ru.sber.transport.tariff_fleet.dto.ewb.EwbTariffGetDto;
import ru.sber.transport.tariff_fleet.mapper.EwbTariffMapper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.sber.transport.tariff_fleet.constant.InspectionType.MEDIC;

@EmbeddedPostgres
@SpringBootTest
@Sql({ "/scripts/db_cleanup.sql",
       "/scripts/basic_corp_structure.sql",
       "/scripts/edf_operator.sql",
       "/scripts/ewb_contract.sql",
       "/scripts/fleet_owner_organization.sql",
       "/scripts/ewb_tariff.sql" })
class EwbTariffRepositoryTest {
    @Autowired
    private EwbTariffRepository ewbTariffRepository;
    @Autowired
    private EwbTariffMapper ewbTariffMapper;
    
    @Test
    void existsByDepartmentIdAndTariff_ContractIdAndTariff_ActiveIsTrue() {
        assertThat(ewbTariffRepository.existsByDepartmentIdAndTariff_ContractIdAndTariff_ActiveIsTrue(UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4"),
                                                                                                      UUID.fromString("5627f0b0-cac0-49e2-be7f-db026fa42435"))).isTrue();
        assertThat(ewbTariffRepository.existsByDepartmentIdAndTariff_ContractIdAndTariff_ActiveIsTrue(UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4"),
                                                                                                      UUID.fromString("e921aa12-0668-4bac-856c-c3d851882911"))).isFalse();
        assertThat(ewbTariffRepository.existsByDepartmentIdAndTariff_ContractIdAndTariff_ActiveIsTrue(UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4"),
                                                                                                      UUID.fromString("20bfb1f4-6099-45e8-8d81-c42df5070763"))).isFalse();
    }
    
    @MethodSource
    @ParameterizedTest
    void searchEwbTariffs(
            UUID organizationId,
            UUID departmentId,
            UUID contractOrganizationId,
            String inspectionType,
            String humanReadableId,
            Boolean active,
            PageRequest pageRequest,
            int totalElements,
            int size,
            int totalPages,
            int pageNumber,
            int numberOfElements,
            Sort sort,
            List<EwbTariffGetDto> expected
                         ) {
        var actual = ewbTariffRepository.searchEwbTariffs(organizationId,
                                                          departmentId,
                                                          contractOrganizationId,
                                                          inspectionType,
                                                          humanReadableId,
                                                          active,
                                                          pageRequest);
        assertThat(actual.getTotalElements()).isEqualTo(totalElements);
        assertThat(actual.getSize()).isEqualTo(size);
        assertThat(actual.getTotalPages()).isEqualTo(totalPages);
        assertThat(actual.getNumber()).isEqualTo(pageNumber);
        assertThat(actual.getNumberOfElements()).isEqualTo(numberOfElements);
        assertThat(actual.getSort()).isEqualTo(sort);
        assertThat(actual.getPageable()).isEqualTo(pageRequest);
        assertThat(ewbTariffMapper.listGetEwbTariffProjectionToListEwbGetTariffDto(actual.getContent()))
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
    
    @Test
    void findByTariffId() {
        var tariffId = UUID.fromString("3c2d9e7a-cc85-7b05-8692-263cb759408d");
        var actualOptional = ewbTariffRepository.findByTariffId(tariffId);
        assertTrue(actualOptional.isPresent());
        var actual = actualOptional.get();
        assertThat(actual)
                .satisfies(a -> {
                    assertEquals(UUID.fromString("3c2d9e7a-cc85-7b05-8692-263cb759408d"), a.getId());
                    assertEquals(MEDIC.toString(), a.getInspectionType());
                    assertEquals("Тест2", a.getFleetOwnerName());
                    assertEquals("Test2", a.getFleetOwnerDepartmentName());
                    assertEquals("ЦА", a.getContractorName());
                    assertEquals("11111", a.getContractNumber());
                    assertEquals(3333, a.getAmount());
                });
    }
    
    static Stream<Arguments> searchEwbTariffs() {
        var tariffDto1 = new EwbTariffGetDto(UUID.fromString("3c2d9e7a-cc85-7b05-8692-263cb759408d"),
                                             "11111",
                                             "TF-0008-00000001",
                                             true,
                                             MEDIC.name(),
                                             "Тест2",
                                             "Test2",
                                             "ЦА");
        var tariffDto2 = new EwbTariffGetDto(UUID.fromString("fbd0afa4-0c43-165f-404a-55f14ed78306"),
                                             "99999",
                                             "TF-0008-00000002",
                                             false,
                                             MEDIC.name(),
                                             "Тест2",
                                             "Test2",
                                             "ЦА");
        var tariffDto3 = new EwbTariffGetDto(UUID.fromString("6f3350fd-1c55-19ab-e861-09ccfd6f34f7"),
                                             "9999",
                                             "TF-0008-00000003",
                                             true,
                                             InspectionType.TECHNIC.name(),
                                             "ЦА",
                                             "ПАО «Сбербанк России» (ЦА)",
                                             "Тест2");
        return Stream.of(
                Arguments.of(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 10, Sort.by(Tariff_.HUMAN_READABLE_ID).ascending()),
                        3,
                        10,
                        1,
                        0,
                        3,
                        Sort.by(Tariff_.HUMAN_READABLE_ID).ascending(),
                        List.of(tariffDto1, tariffDto2, tariffDto3)
                            ),
                Arguments.of(
                        UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 10, Sort.by(Tariff_.HUMAN_READABLE_ID).ascending()),
                        2,
                        10,
                        1,
                        0,
                        2,
                        Sort.by(Tariff_.HUMAN_READABLE_ID).ascending(),
                        List.of(tariffDto1, tariffDto2)
                            ),
                Arguments.of(
                        null,
                        UUID.fromString("482e6dcb-03a9-4927-90b4-c7081114a9d8"),
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 10, Sort.by(Tariff_.HUMAN_READABLE_ID).ascending()),
                        1,
                        10,
                        1,
                        0,
                        1,
                        Sort.by(Tariff_.HUMAN_READABLE_ID).ascending(),
                        List.of(tariffDto3)
                            ),
                Arguments.of(
                        null,
                        null,
                        UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"),
                        null,
                        null,
                        null,
                        PageRequest.of(0, 10, Sort.by(Tariff_.HUMAN_READABLE_ID).ascending()),
                        2,
                        10,
                        1,
                        0,
                        2,
                        Sort.by(Tariff_.HUMAN_READABLE_ID).ascending(),
                        List.of(tariffDto1, tariffDto2)
                            ),
                Arguments.of(
                        null,
                        null,
                        null,
                        InspectionType.TECHNIC.name(),
                        null,
                        null,
                        PageRequest.of(0, 10, Sort.by(Tariff_.HUMAN_READABLE_ID).ascending()),
                        1,
                        10,
                        1,
                        0,
                        1,
                        Sort.by(Tariff_.HUMAN_READABLE_ID).ascending(),
                        List.of(tariffDto3)
                            ),
                Arguments.of(
                        null,
                        null,
                        null,
                        null,
                        "TF-0008",
                        null,
                        PageRequest.of(0, 10, Sort.by(Tariff_.HUMAN_READABLE_ID).ascending()),
                        3,
                        10,
                        1,
                        0,
                        3,
                        Sort.by(Tariff_.HUMAN_READABLE_ID).ascending(),
                        List.of(tariffDto1, tariffDto2, tariffDto3)
                            ),
                Arguments.of(
                        null,
                        null,
                        null,
                        null,
                        null,
                        Boolean.FALSE,
                        PageRequest.of(0, 10, Sort.by(Tariff_.HUMAN_READABLE_ID).ascending()),
                        1,
                        10,
                        1,
                        0,
                        1,
                        Sort.by(Tariff_.HUMAN_READABLE_ID).ascending(),
                        List.of(tariffDto2)
                            ),
                Arguments.of(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(1, 1, Sort.by(Tariff_.HUMAN_READABLE_ID).ascending()),
                        3,
                        1,
                        3,
                        1,
                        1,
                        Sort.by(Tariff_.HUMAN_READABLE_ID).ascending(),
                        List.of(tariffDto2)
                            ),
                Arguments.of(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 10, Sort.by(Tariff_.HUMAN_READABLE_ID).descending()),
                        3,
                        10,
                        1,
                        0,
                        3,
                        Sort.by(Tariff_.HUMAN_READABLE_ID).descending(),
                        List.of(tariffDto3, tariffDto2, tariffDto1)
                            ),
                Arguments.of(
                        UUID.fromString("621c288d-e348-46e5-a319-cbf61ef1e396"),
                        UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4"),
                        UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"),
                        MEDIC.name(),
                        "TF-0008-00000001",
                        Boolean.TRUE,
                        PageRequest.of(0, 10, Sort.by(Tariff_.HUMAN_READABLE_ID).ascending()),
                        1,
                        10,
                        1,
                        0,
                        1,
                        Sort.by(Tariff_.HUMAN_READABLE_ID).ascending(),
                        List.of(tariffDto1)
                            )
                        );
    }
    
    @Test
    void findActiveTariffDepartmentIds() {
        assertThat(ewbTariffRepository.findAllActiveTariffDepartmentIdsByContractId(UUID.fromString("5627f0b0-cac0-49e2-be7f-db026fa42435")))
                .isEqualTo(Collections.singletonList(UUID.fromString("489A0090-1819-4C60-A611-572EA115C6A4")));
        assertThat(ewbTariffRepository.findAllActiveTariffDepartmentIdsByContractId(UUID.randomUUID()))
                .isEqualTo(Collections.emptyList());
    }
    
    @Test
    void findByIdWithTariff() {
        assertThat(ewbTariffRepository.findByIdWithTariff(UUID.fromString("3c2d9e7a-cc85-7b05-8692-263cb759408d"))).isPresent();
        assertThat(ewbTariffRepository.findByIdWithTariff(UUID.randomUUID())).isNotPresent();
    }
    
    @Test
    void findNotActiveNotManualByContractIdWithTariff() {
        var actual = ewbTariffRepository.findNotActiveNotManualByContractIdWithTariff(UUID.fromString("20bfb1f4-6099-45e8-8d81-c42df5070763"));
        assertThat(actual)
                .hasSize(1)
                .satisfies(a -> assertEquals(a.get(0).getTariffId(), UUID.fromString("fbd0afa4-0c43-165f-404a-55f14ed78306")));
    }
    
    @Test
    void findActiveByContractIdWithTariff() {
        var actual = ewbTariffRepository.findActiveByContractIdWithTariff(UUID.fromString("5627f0b0-cac0-49e2-be7f-db026fa42435"));
        assertThat(actual)
                .hasSize(1)
                .satisfies(a -> assertEquals(a.get(0).getTariffId(), UUID.fromString("3c2d9e7a-cc85-7b05-8692-263cb759408d")));
    }
}