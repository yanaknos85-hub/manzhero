package ru.sber.transport.tariff_fleet.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.tariff_fleet.database.model.EdfOperator;
import ru.sber.transport.tariff_fleet.dto.EdfOperatorDto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EdfOperatorMapperTest {
    
    private final EdfOperatorMapper edfOperatorMapper = Mappers.getMapper(EdfOperatorMapper.class);
    
    @Test
    void edfOperatorToEdfOperatorDto() {
        var edfOperator = Instancio.create(EdfOperator.class);
        var expected = new EdfOperatorDto(edfOperator.getId(), edfOperator.getTitle());
        var actual = edfOperatorMapper.edfOperatorToEdfOperatorDto(edfOperator);
        assertThat(actual).isEqualTo(expected);
        assertThat(edfOperatorMapper.edfOperatorToEdfOperatorDto(null)).isNull();
    }
    
    @Test
    void setEdfOperatorToSetEdfOperatorDto() {
        var edfOperator1 = Instancio.create(EdfOperator.class);
        var edfOperator2 = Instancio.create(EdfOperator.class);
        var edfOperator3 = Instancio.create(EdfOperator.class);
        var source = Set.of(edfOperator1, edfOperator2, edfOperator3);
        var expected1 = new EdfOperatorDto(edfOperator1.getId(), edfOperator1.getTitle());
        var expected2 = new EdfOperatorDto(edfOperator2.getId(), edfOperator2.getTitle());
        var expected3 = new EdfOperatorDto(edfOperator3.getId(), edfOperator3.getTitle());
        var actual = edfOperatorMapper.setEdfOperatorToSetEdfOperatorDto(source);
        assertThat(actual).usingRecursiveComparison()
                          .isEqualTo(Set.of(expected1, expected2, expected3));
        assertThat(edfOperatorMapper.setEdfOperatorToSetEdfOperatorDto(null)).isNull();
    }
}