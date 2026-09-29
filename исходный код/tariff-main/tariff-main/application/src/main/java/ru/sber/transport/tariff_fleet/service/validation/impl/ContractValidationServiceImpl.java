package ru.sber.transport.tariff_fleet.service.validation.impl;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.dao.ContractRepository;
import ru.sber.transport.tariff_fleet.dto.DateRange;
import ru.sber.transport.tariff_fleet.exception.ContractAlreadyExistsException;
import ru.sber.transport.tariff_fleet.exception.DateRangeValidationException;
import ru.sber.transport.tariff_fleet.exception.UnexpectedDocumentTypeValidationException;
import ru.sber.transport.tariff_fleet.service.validation.ContractValidationService;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

import static ru.sber.transport.tariff_fleet.exception.UnexpectedDocumentTypeValidationException.DOCUMENT_TYP_MSG_FORMAT;

@Service
@RequiredArgsConstructor
public class ContractValidationServiceImpl implements ContractValidationService {
    
    private static final String DATE_RANGE_VALIDATION_EXCEPTION = "Не верно задан период, дата и время начала больше, чем дата и время окончания";
    private final Clock clock;
    private final ContractRepository contractRepository;
    
    @Transactional(readOnly = true)
    public void validateUvhdUnique(String uvhd) {
        if (!StringUtils.isBlank(uvhd) && contractRepository.existsByUvhdAndActiveTrue(uvhd)) {
            throw new ContractAlreadyExistsException(uvhd);
        }
    }
    
    public void validateDateRange(LocalDate start, LocalDate end) {
        if ((start != null && end != null) && start.isAfter(end)) {
            throw new DateRangeValidationException(DATE_RANGE_VALIDATION_EXCEPTION);
        }
    }
    
    @Override
    public LocalDate validateStartAndGet(DateRange period, Boolean active) {
        var now = LocalDate.now(clock);
        if (Objects.nonNull(active) && Objects.nonNull(period)) {
            return getStartByPeriodAndActive(period.start(), active, now);
        } else if (Objects.isNull(active) && Objects.nonNull(period)) {
            return period.start();
        }
        return null;
    }
    
    @Override
    public LocalDate validateEndAndGet(DateRange period, Boolean active) {
        var now = LocalDate.now(clock);
        if (Objects.nonNull(active) && Objects.nonNull(period)) {
            return getEndByPeriodAndActive(period.end(), active, now);
        } else if (Objects.isNull(active) && Objects.nonNull(period)) {
            return period.end();
        }
        return null;
    }
    
    @Override
    public void validateFindByDocumentType(DocumentType documentType) {
        if (DocumentType.EWB.equals(documentType)) {
            throw new UnexpectedDocumentTypeValidationException(DOCUMENT_TYP_MSG_FORMAT, documentType);
        }
    }
    
    private LocalDate getStartByPeriodAndActive(@NotNull LocalDate start, Boolean active, LocalDate now) {
        var isActive = Boolean.TRUE.equals(active);
        if (isActive && start.isAfter(now)) {
            throw new DateRangeValidationException("У активного договора не может быть Дата начала действия договора меньше текущего дня");
        }
        if (!isActive && now.isBefore(start)) {
            throw new DateRangeValidationException("У не активного договора не может быть Дата начала действия договора больше или равна текущему дню");
        }
        return start;
    }
    
    private LocalDate getEndByPeriodAndActive(@NotNull LocalDate end, Boolean active, LocalDate now) {
        var isActive = Boolean.TRUE.equals(active);
        if (isActive && end.isBefore(now)) {
            throw new DateRangeValidationException("У активного договора не может быть Дата окончания действия договора меньше текущей даты");
        }
        if (!isActive && now.isBefore(end)) {
            throw new DateRangeValidationException("У не активного договора не может быть Дата окончания действия договора больше или равна текущему дню");
        }
        return end;
    }
}