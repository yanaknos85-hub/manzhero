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
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.database.model.RepairContract;
import ru.sber.transport.tariff_fleet.dto.repair.RepairContractGetDto;
import ru.sber.transport.tariff_fleet.mapper.RepairContractMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.sber.transport.tariff_fleet.dto.repair.RepairContractSortSetting.RepairContractSortOption.CONTRACTOR_NAME;
import static ru.sber.transport.tariff_fleet.dto.repair.RepairContractSortSetting.RepairContractSortOption.NUMBER;


@EmbeddedPostgres
@SpringBootTest
@Sql({ "/scripts/db_cleanup.sql",
       "/scripts/basic_corp_structure.sql",
       "/scripts/contractor.sql",
       "/scripts/repair_contract.sql" })
class RepairContractRepositoryTest {
    
    @Autowired
    private RepairContractRepository repairContractRepository;
    @Autowired
    private RepairContractMapper repairContractMapper;
    
    
    @Test
    void findWithContractByContractId() {
        var contractId = UUID.fromString("999d5145-73e0-4518-aba2-bf9b3ea81088");
        var withContractByContractIdOpt = repairContractRepository.findWithContractByContractId(contractId);
        assertTrue(withContractByContractIdOpt.isPresent());
        assertThat(withContractByContractIdOpt.get())
                .extracting(RepairContract::getContractId)
                .isEqualTo(contractId);
    }
    
    @MethodSource
    @ParameterizedTest
    void search(
            UUID contractorId,
            UUID organizationId,
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
            List<RepairContractGetDto> expected
               ) {
        var actual = repairContractRepository.searchRepairContracts(number, contractorId, organizationId, start, end, active, pageable);
        assertThat(actual.getTotalElements()).isEqualTo(totalElements);
        assertThat(actual.getSize()).isEqualTo(size);
        assertThat(actual.getTotalPages()).isEqualTo(totalPages);
        assertThat(actual.getNumber()).isEqualTo(pageNumber);
        assertThat(actual.getNumberOfElements()).isEqualTo(numberOfElements);
        assertThat(actual.getSort()).isEqualTo(sort);
        assertThat(actual.getPageable()).isEqualTo(pageable);
        
        assertThat(actual.getContent().stream().map(i -> repairContractMapper.getRepairContractProjectionToRepairGetContractDto(i)).toList())
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
    
    
    static Stream<Arguments> search() {
        var contractorId1 = UUID.fromString("a033de41-2228-40ba-bc13-e97849625484");
        var contractorId2 = UUID.fromString("b9ec53c0-be74-4aac-b295-873083c7ce4d");
        var organizationId1 = UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6");
        var contractDto1 = new RepairContractGetDto(
                UUID.fromString("999d5145-73e0-4518-aba2-bf9b3ea81088"),
                "11111",
                LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(3),
                true,
                UUID.fromString("a033de41-2228-40ba-bc13-e97849625484"),
                "ContractorName2",
                "1",
                33333L
        );
        var contractDto2 = new RepairContractGetDto(
                UUID.fromString("5805f22f-30e3-440f-91b4-4b5fe2c28405"),
                "99999",
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(9),
                false,
                UUID.fromString("a033de41-2228-40ba-bc13-e97849625484"),
                "ContractorName2",
                "22",
                99999L);
        var contractDto3 = new RepairContractGetDto(
                UUID.fromString("5c7b86b9-667f-4fb9-8d2e-ab3e871751eb"),
                "9999",
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(10),
                true,
                UUID.fromString("b9ec53c0-be74-4aac-b295-873083c7ce4d"),
                "ContractorName3",
                "333",
                999999L);
        
        return Stream.of(
                Arguments.of(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 20, Sort.by(NUMBER.getFieldName()).ascending()),
                        3,
                        20,
                        1,
                        0,
                        3,
                        Sort.by(NUMBER.getFieldName()).ascending(),
                        List.of(contractDto1, contractDto3, contractDto2)),
                Arguments.of(
                        contractorId1,
                        organizationId1,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 20, Sort.by(NUMBER.getFieldName()).ascending()),
                        2,
                        20,
                        1,
                        0,
                        2,
                        Sort.by(NUMBER.getFieldName()).ascending(),
                        List.of(contractDto1, contractDto2)),
                Arguments.of(
                        null,
                        null,
                        "11111",
                        null,
                        null,
                        null,
                        PageRequest.of(0, 20, Sort.by(NUMBER.getFieldName()).ascending()),
                        1,
                        20,
                        1,
                        0,
                        1,
                        Sort.by(NUMBER.getFieldName()).ascending(),
                        List.of(contractDto1)),
                Arguments.of(
                        null,
                        null,
                        null,
                        LocalDate.now().minusDays(3),
                        LocalDate.now().plusDays(3),
                        null,
                        PageRequest.of(0, 20, Sort.by(NUMBER.getFieldName()).ascending()),
                        1,
                        20,
                        1,
                        0,
                        1,
                        Sort.by(NUMBER.getFieldName()).ascending(),
                        List.of(contractDto1)),
                Arguments.of(
                        null,
                        null,
                        null,
                        null,
                        null,
                        true,
                        PageRequest.of(0, 20, Sort.by(NUMBER.getFieldName()).ascending()),
                        2,
                        20,
                        1,
                        0,
                        2,
                        Sort.by(NUMBER.getFieldName()).ascending(),
                        List.of(contractDto1, contractDto3)),
                Arguments.of(
                        null,
                        null,
                        null,
                        LocalDate.now().minusDays(10),
                        LocalDate.now().plusDays(15),
                        null,
                        PageRequest.of(0, 20, Sort.by(NUMBER.getFieldName()).ascending()),
                        3,
                        20,
                        1,
                        0,
                        3,
                        Sort.by(NUMBER.getFieldName()).ascending(),
                        List.of(contractDto1, contractDto3, contractDto2)),
                Arguments.of(
                        null,
                        null,
                        null,
                        LocalDate.now().minusDays(15),
                        LocalDate.now().plusDays(5),
                        null,
                        PageRequest.of(0, 20, Sort.by(NUMBER.getFieldName()).ascending()),
                        2,
                        20,
                        1,
                        0,
                        2,
                        Sort.by(NUMBER.getFieldName()).ascending(),
                        List.of(contractDto1, contractDto2)),
                Arguments.of(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(1, 1, Sort.by(NUMBER.getFieldName()).ascending()),
                        3,
                        1,
                        3,
                        1,
                        1,
                        Sort.by(NUMBER.getFieldName()).ascending(),
                        List.of(contractDto3)),
                Arguments.of(
                        contractorId2,
                        null,
                        "9999",
                        LocalDate.now().minusDays(11),
                        LocalDate.now().plusDays(10),
                        null,
                        PageRequest.of(0, 20, Sort.by(NUMBER.getFieldName()).ascending()),
                        1,
                        20,
                        1,
                        0,
                        1,
                        Sort.by(NUMBER.getFieldName()).ascending(),
                        List.of(contractDto3)),
                Arguments.of(
                        null,
                        null,
                        "9999",
                        null,
                        null,
                        null,
                        PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, CONTRACTOR_NAME.getFieldName())),
                        2,
                        20,
                        1,
                        0,
                        2,
                        Sort.by(Sort.Direction.DESC, CONTRACTOR_NAME.getFieldName()),
                        List.of(contractDto3, contractDto2)));
    }
    
}
