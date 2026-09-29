package ru.sber.transport.tariff_fleet.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.database.dao.EdfOperatorRepository;
import ru.sber.transport.tariff_fleet.database.model.EdfOperator;
import ru.sber.transport.tariff_fleet.dto.EdfOperatorDto;
import ru.sber.transport.tariff_fleet.mapper.EdfOperatorMapper;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class EdfOperatorServiceImplTest {
    @InjectMocks
    private EdfOperatorServiceImpl edfOperatorService;
    @Mock
    private EdfOperatorRepository edfOperatorRepository;
    @Mock
    private EdfOperatorMapper edfOperatorMapper;
    
    @Test
    void getById() {
        var edfOperator = Instancio.create(EdfOperator.class);
        doReturn(Optional.of(edfOperator)).when(edfOperatorRepository).findById(anyString());
        var actual = edfOperatorService.getById(UUID.randomUUID().toString());
        assertThat(actual).isEqualTo(Optional.of(edfOperator));
    }
    
    @Test
    void getAllActive() {
        var edfOperator1 = Instancio.create(EdfOperator.class);
        var edfOperator2 = Instancio.create(EdfOperator.class);
        var edfOperator3 = Instancio.create(EdfOperator.class);
        var edfOperators = Set.of(edfOperator1, edfOperator2, edfOperator3);
        var edfOperatorDto1 = Instancio.create(EdfOperatorDto.class);
        var edfOperatorDto2 = Instancio.create(EdfOperatorDto.class);
        var edfOperatorDto3 = Instancio.create(EdfOperatorDto.class);
        var expected = Set.of(edfOperatorDto1, edfOperatorDto2, edfOperatorDto3);
        doReturn(edfOperators).when(edfOperatorRepository).findAllByActiveIsTrue();
        doReturn(expected).when(edfOperatorMapper).setEdfOperatorToSetEdfOperatorDto(edfOperators);
        var actual = edfOperatorService.getAllActive();
        assertThat(actual).usingRecursiveComparison()
                          .isEqualTo(expected);
    }
}