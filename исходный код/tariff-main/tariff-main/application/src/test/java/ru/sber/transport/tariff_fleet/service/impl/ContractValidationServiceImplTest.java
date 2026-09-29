package ru.sber.transport.tariff_fleet.service.impl;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.dao.ContractRepository;
import ru.sber.transport.tariff_fleet.dto.DateRange;
import ru.sber.transport.tariff_fleet.exception.ContractAlreadyExistsException;
import ru.sber.transport.tariff_fleet.exception.DateRangeValidationException;
import ru.sber.transport.tariff_fleet.exception.UnexpectedDocumentTypeValidationException;
import ru.sber.transport.tariff_fleet.service.validation.impl.ContractValidationServiceImpl;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ContractValidationServiceImplTest {

    @Mock
    private ContractRepository contractRepository;
    @Mock
    private Clock clock;
    private final Clock fixedClock = Clock.fixed(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).plusMinutes(20)
            .toInstant(ZoneOffset.UTC), ZoneId.of(ZoneOffset.UTC.getId()));
    @InjectMocks
    private ContractValidationServiceImpl contractValidationService;

    @Test
    void validateUvhdUnique() {
        var uvhd = "123456789";
        doReturn(Boolean.TRUE).when(contractRepository).existsByUvhdAndActiveTrue(anyString());
        var exception = assertThrows(ContractAlreadyExistsException.class,
                () -> contractValidationService.validateUvhdUnique(uvhd));
        assertEquals(ContractAlreadyExistsException.MSG_FORMAT_2.formatted(uvhd),
                exception.getMessage());
        doReturn(Boolean.FALSE).when(contractRepository).existsByUvhdAndActiveTrue(anyString());
        assertDoesNotThrow(() -> contractValidationService.validateUvhdUnique(uvhd));
    }

    @Test
    void validateDateRange() {
        var now = LocalDate.now();
        var beforeNow = LocalDate.now().minusDays(1);
        var afterNow = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> contractValidationService.validateDateRange(now, beforeNow))
                .isInstanceOf(DateRangeValidationException.class)
                .hasMessage("Не верно задан период, дата и время начала больше, чем дата и время окончания");
        assertDoesNotThrow(() -> contractValidationService.validateDateRange(now, afterNow));
    }

    @Test
    void validateStartAndGet() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        var expected1 = LocalDate.now(fixedClock);
        var expected2 = LocalDate.now(fixedClock).plusDays(1);
        var expected3 = LocalDate.now(fixedClock).minusDays(1);
        var dateRange1 = new DateRange(LocalDate.now(fixedClock), LocalDate.now(fixedClock).plusDays(7));
        var dateRange2 = new DateRange(LocalDate.now(fixedClock).plusDays(1), LocalDate.now(fixedClock).plusDays(7));
        var dateRange3 = new DateRange(LocalDate.now(fixedClock).minusDays(1), LocalDate.now(fixedClock).minusDays(7));
        assertThat(contractValidationService.validateStartAndGet(null, null)).isNull();
        assertThat(contractValidationService.validateStartAndGet(null, true)).isNull();
        assertThat(contractValidationService.validateStartAndGet(null, false)).isNull();
        assertThat(contractValidationService.validateStartAndGet(dateRange1, null)).isEqualTo(expected1);
        assertThat(contractValidationService.validateStartAndGet(dateRange2, null)).isEqualTo(expected2);
        assertThat(contractValidationService.validateStartAndGet(dateRange3, null)).isEqualTo(expected3);
        assertThat(contractValidationService.validateStartAndGet(dateRange1, true)).isEqualTo(expected1);
        assertThatThrownBy(() -> contractValidationService.validateStartAndGet(dateRange2, true))
                .isInstanceOf(DateRangeValidationException.class)
                .hasMessage("У активного договора не может быть Дата начала действия договора меньше текущего дня");
        assertThat(contractValidationService.validateStartAndGet(dateRange3, true)).isEqualTo(expected3);
        assertThat(contractValidationService.validateStartAndGet(dateRange1, false)).isEqualTo(expected1);
        assertThatThrownBy(() -> contractValidationService.validateStartAndGet(dateRange2, false))
                .isInstanceOf(DateRangeValidationException.class)
                .hasMessage("У не активного договора не может быть Дата начала действия договора больше или равна текущему дню");
        assertThat(contractValidationService.validateStartAndGet(dateRange3, false)).isEqualTo(expected3);
    }

    @Test
    void validateEndAndGet() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        var expected1 = LocalDate.now(fixedClock);
        var expected2 = LocalDate.now(fixedClock).plusDays(1);
        var expected3 = LocalDate.now(fixedClock).minusDays(1);
        var dateRange1 = new DateRange(LocalDate.now(fixedClock).minusDays(7), LocalDate.now(fixedClock));
        var dateRange2 = new DateRange(LocalDate.now(fixedClock), LocalDate.now(fixedClock).plusDays(1));
        var dateRange3 = new DateRange(LocalDate.now(fixedClock).minusDays(7), LocalDate.now(fixedClock).minusDays(1));
        assertThat(contractValidationService.validateEndAndGet(null, null)).isNull();
        assertThat(contractValidationService.validateEndAndGet(null, true)).isNull();
        assertThat(contractValidationService.validateEndAndGet(null, false)).isNull();
        assertThat(contractValidationService.validateEndAndGet(dateRange1, null)).isEqualTo(expected1);
        assertThat(contractValidationService.validateEndAndGet(dateRange2, null)).isEqualTo(expected2);
        assertThat(contractValidationService.validateEndAndGet(dateRange3, null)).isEqualTo(expected3);
        assertThat(contractValidationService.validateEndAndGet(dateRange1, true)).isEqualTo(expected1);
        assertThat(contractValidationService.validateEndAndGet(dateRange2, true)).isEqualTo(expected2);
        assertThatThrownBy(() -> contractValidationService.validateEndAndGet(dateRange3, true))
                .isInstanceOf(DateRangeValidationException.class)
                .hasMessage("У активного договора не может быть Дата окончания действия договора меньше текущей даты");
        assertThat(contractValidationService.validateEndAndGet(dateRange1, false)).isEqualTo(expected1);
        assertThatThrownBy(() -> contractValidationService.validateEndAndGet(dateRange2, false))
                .isInstanceOf(DateRangeValidationException.class)
                .hasMessage("У не активного договора не может быть Дата окончания действия договора больше или равна текущему дню");
        assertThat(contractValidationService.validateEndAndGet(dateRange3, false)).isEqualTo(expected3);
    }

    @Test
    void shouldThrowExceptionWhenInvalidDocumentTypeProvided() {
        var invalidDocumentType = DocumentType.EWB;
        assertThrows(UnexpectedDocumentTypeValidationException.class,
                () -> contractValidationService.validateFindByDocumentType(invalidDocumentType),
                "Ожидалось выбрасывание исключения");
    }

    @Test
    void shouldNotThrowExceptionWhenValidDocumentTypeProvided() {
        var validDocumentType = DocumentType.REPAIR_AND_MAINTENANCE;
        assertDoesNotThrow(() -> contractValidationService.validateFindByDocumentType(validDocumentType),
                "Ошибочно было вызвано исключение");
    }
}