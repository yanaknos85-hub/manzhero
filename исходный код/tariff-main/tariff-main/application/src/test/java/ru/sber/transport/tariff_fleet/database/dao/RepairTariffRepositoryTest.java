package ru.sber.transport.tariff_fleet.database.dao;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.model.TariffFilter;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@DisplayName("Тесты репозитория repair_tariff")
@SpringBootTest(
        properties = {
                "spring.main.cloud-platform=none",
                "logger.level.root=debug"
        }
)
@EmbeddedPostgres
class RepairTariffRepositoryTest {

    @Autowired
    private RepairTariffRepository repairTariffRepository;

    @Test
    @Sql(scripts = {
            "/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/scripts/db_cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Поиск тарифов автосервиса с пагинацией и подсчетом общего числа")
    void testSearchByFilterWithPagination() {
        var filter = Instancio.ofBlank(TariffFilter.class)
                .set(field(TariffFilter::contractorId), UUID.fromString("b1a8d2ce-c3fd-43dd-a15b-d94cfda21a3e"))
                .create();
        var paging = PageRequest.of(0, 10);

        var actual = repairTariffRepository.searchByFilter(filter, paging);

        assertThat(actual.getTotalElements()).isEqualTo(2);
        assertThat(actual.getSize()).isEqualTo(10);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getNumber()).isZero();
        assertThat(actual.getNumberOfElements()).isEqualTo(2);
        assertThat(actual.getSort()).isEqualTo(Sort.unsorted());
        assertThat(actual.getPageable()).isEqualTo(paging);
        assertThat(actual.getTotalElements()).isEqualTo(2);
    }

    @Test
    @Sql(scripts = {
            "/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/scripts/db_cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Поиск тарифов автосервиса по organizationId")
    void testSearchByFilterByOrganizationId() {
        var filter = Instancio.ofBlank(TariffFilter.class)
                .set(field(TariffFilter::organizationId), UUID.fromString("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6"))
                .create();
        var paging = PageRequest.of(0, 10);

        var actual = repairTariffRepository.searchByFilter(filter, paging);

        assertThat(actual.getTotalElements()).isEqualTo(2);
        assertThat(actual.getSize()).isEqualTo(10);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getNumber()).isZero();
        assertThat(actual.getNumberOfElements()).isEqualTo(2);
        assertThat(actual.getSort()).isEqualTo(Sort.unsorted());
        assertThat(actual.getPageable()).isEqualTo(paging);
        assertThat(actual.getTotalElements()).isEqualTo(2);
    }

    @Test
    @Sql(scripts = {
            "/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/scripts/db_cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Поиск тарифов автосервиса по humanReadableId")
    void testSearchByFilterByHumanReadableId() {
        var filter = Instancio.ofBlank(TariffFilter.class)
                .set(field(TariffFilter::humanReadableId), "TARIFF_002")
                .create();
        var paging = PageRequest.of(0, 10);

        var actual = repairTariffRepository.searchByFilter(filter, paging);

        assertThat(actual.getTotalElements()).isEqualTo(1);
        assertThat(actual.getSize()).isEqualTo(10);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getNumber()).isZero();
        assertThat(actual.getNumberOfElements()).isEqualTo(1);
        assertThat(actual.getSort()).isEqualTo(Sort.unsorted());
        assertThat(actual.getPageable()).isEqualTo(paging);
        assertThat(actual.getTotalElements()).isEqualTo(1);
        assertThat(actual.getContent().getFirst().getHumanReadableId()).isEqualTo("TARIFF_002");
    }

    @Test
    @Sql(scripts = {
            "/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/scripts/db_cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Поиск тарифов автосервиса без фильтров (все тарифы)")
    void testSearchByFilterWithoutFilters() {
        var filter = Instancio.createBlank(TariffFilter.class);
        var paging = PageRequest.of(0, 10);

        var actual = repairTariffRepository.searchByFilter(filter, paging);

        assertThat(actual.getTotalElements()).isEqualTo(2);
        assertThat(actual.getSize()).isEqualTo(10);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getNumber()).isZero();
        assertThat(actual.getNumberOfElements()).isEqualTo(2);
        assertThat(actual.getSort()).isEqualTo(Sort.unsorted());
        assertThat(actual.getPageable()).isEqualTo(paging);
        assertThat(actual.getTotalElements()).isEqualTo(2);
    }

    @Test
    @Sql(scripts = {
            "/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/tariff_fuel_and_repair.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/scripts/db_cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("Поиск тарифов автосервиса по active статусу")
    void testSearchByFilterByActive() {
        var filter = Instancio.ofBlank(TariffFilter.class)
                .set(field(TariffFilter::active), true)
                .create();
        var paging = PageRequest.of(0, 10);

        var actual = repairTariffRepository.searchByFilter(filter, paging);

        assertThat(actual.getTotalElements()).isEqualTo(2);
        assertThat(actual.getSize()).isEqualTo(10);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getNumber()).isZero();
        assertThat(actual.getNumberOfElements()).isEqualTo(2);
        assertThat(actual.getSort()).isEqualTo(Sort.unsorted());
        assertThat(actual.getPageable()).isEqualTo(paging);
        assertThat(actual.getTotalElements()).isEqualTo(2);
    }
}
