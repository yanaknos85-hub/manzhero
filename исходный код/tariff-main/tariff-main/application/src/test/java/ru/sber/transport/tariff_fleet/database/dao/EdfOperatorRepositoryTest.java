package ru.sber.transport.tariff_fleet.database.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.database.model.EdfOperator;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedPostgres
@SpringBootTest
@Transactional
class EdfOperatorRepositoryTest {
    @Autowired
    private EdfOperatorRepository edfOperatorRepository;
    
    @Test
    @Sql("/scripts/edf_operator.sql")
    void findAllByActiveIsTrue() {
        var edfOperator1 = new EdfOperator("2BM", "ПФ СКБ Контур", "2BM - «ПФ СКБ Контур»", true);
        var edfOperator2 = new EdfOperator("2AL", "Такском ЭДО", "2AL - «Такском ЭДО»", true);
        var actual = edfOperatorRepository.findAllByActiveIsTrue();
        assertThat(actual).isEqualTo(Set.of(edfOperator1, edfOperator2));
    }
}