package ru.sberbank.ditsib.transport.limits.service.impl;

import io.qameta.allure.Feature;
import jakarta.persistence.EntityManager;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.constants.LimitStatus;
import ru.sberbank.ditsib.transport.limits.dto.LimitLevelDTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitStatsV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.Month;
import ru.sberbank.ditsib.transport.limits.mapper.*;
import ru.sberbank.ditsib.transport.limits.model.limit.*;
import ru.sberbank.ditsib.transport.limits.service.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка сервиса подсчета статистики")
class LimitStatsServiceImplTest {

    private final DepLimitService depLimitService = mock(DepLimitService.class);

    private final LimitSpendingService limitSpendingService = mock(LimitSpendingService.class);

    private final LimitService limitService = mock(LimitService.class);

    private final LimitSharingService limitSharingService = mock(LimitSharingService.class);

    private final EntityManager entityManager = mock(EntityManager.class);

    private final LimitSharingPerPeriodService<ru.sberbank.ditsib.transport.limits.model.limit.Month> limitSharingPerPeriodServiceMonth = mock(LimitSharingPerPeriodService.class);

    private final PeriodMapper periodMapper = new PeriodMapperImpl();

    private final VersionConverter versionConverter = new VersionConverterImpl(periodMapper, new DateMapperImpl());

    private final LimitStatsData limitStatsData = mock(LimitStatsData.class);

    private final LimitStatsService limitStatsService = new LimitStatsServiceImpl(
        depLimitService,
        limitSpendingService,
        limitService,
        limitSharingService,
        Map.of(LimitSharingType.MONTHLY, limitSharingPerPeriodServiceMonth),
        periodMapper,
        versionConverter,
        entityManager,
        new StatsExporterImpl(List.of(new StatsExportPerformerPeriodImpl(), new StatsExportPerformerYearImpl()))
    ) {

        @Override
        LimitStatsData limitStatsData() {
            return limitStatsData;
        }
    };

    @Test
    @DisplayName("Проверка экспорта")
    void test_exportToXlsxV2() throws IOException {
        var source = Instancio.ofList(LimitStatsV2DTO.class)
            .set(Select.field(LimitStatsV2DTO::getPeriod), Instancio.create(Month.class))
            .set(Select.field(LimitStatsV2DTO::getDepartmentLevel), 1)
            .create();

        var stream = limitStatsService.exportToXlsxV2(source);

        assertThat(stream).isNotNull();

        try (var workbook = new XSSFWorkbook(new FileInputStream(stream.toFile()))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(2);

            var firstSheet = workbook.getSheetAt(1);
            assertThat(firstSheet.getSheetName()).isEqualTo("Год " + source.getFirst().getYear());

            var secondSheet = workbook.getSheetAt(0);
            assertThat(secondSheet.getSheetName()).isEqualTo("Период " + (source.getFirst().getPeriod().ordinal() + 1));
        }
    }

    @Test
    @DisplayName("Проверка получения статистики по лимитам")
    void test_getLimitStatsV2() {
        var organizationId = UUID.randomUUID();
        var data = Instancio.ofList(LimitLevelDTO.class)
            .generate(Select.field(LimitLevelDTO::getLimit), gen -> gen.oneOf(Instancio.ofList(DepLimit.class).size(1000).set(Select.field(DepLimit::getLimitSharingType), LimitSharingType.MONTHLY).create()))
            .create();
        var date = Instancio.create(LocalDate.class);
        var spendings = data.parallelStream()
            .map(LimitLevelDTO::getLimit)
            .map(it -> new AbstractMap.SimpleEntry<>(it.getId(),
                Instancio.ofList(LimitSpending.class).supply(Select.field(LimitSpending::getLimitSharingPerPeriod), () -> Instancio.of(LimitSharingPerPeriod.class).generate(Select.field(LimitSharingPerPeriod::getLimitSharing), gen -> gen.oneOf(it.getSharings().parallelStream().peek(sh -> sh.setLimit(it)).toList())).generate(Select.field(LimitSharingPerPeriod::getPeriodData), gen -> gen.enumOf(PeriodData.class)).create()).create()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        when(limitStatsData.department(any())).then(inv ->
            data.parallelStream()
                .map(LimitLevelDTO::getLimit)
                .filter(l -> Objects.equals(inv.getArgument(0), l.getId()))
                .map(DepLimit.class::cast)
                .map(DepLimit::getDepartment)
                .findFirst()
                .orElseThrow()
        );
        when(limitStatsData.untilDate()).thenReturn(date);
        when(limitSharingService.getByLimit(any())).then(inv -> spendings.get(inv.getArgument(0, DepLimit.class).getId()).parallelStream().map(LimitSpending::getLimitSharingPerPeriod).map(LimitSharingPerPeriod::getLimitSharing).toList());
        when(limitStatsData.popSpends(any())).then(inv -> new LinkedList<>(spendings.get(inv.getArgument(0, UUID.class))));
        when(limitSpendingService.getSpendingPeriods(any())).thenReturn(spendings.values().parallelStream().flatMap(Collection::parallelStream).map(it -> new AbstractMap.SimpleEntry<>(it.getId(), it.getLimitSharingPerPeriod().getPeriod())).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, it -> (Period) it.getValue())));
        when(limitStatsData.employees(any())).thenReturn(Instancio.create(Long.class));
        when(limitSpendingService.getSpendingSharings(any())).thenReturn(spendings.values().parallelStream().flatMap(Collection::parallelStream).map(it -> new AbstractMap.SimpleEntry<>(it.getId(), it.getLimitSharingPerPeriod())).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, it -> it.getValue().getId())));
        when(limitSharingPerPeriodServiceMonth.getPeriodNumber(any(), any())).thenReturn(ru.sberbank.ditsib.transport.limits.model.limit.Month.JANUARY);
        when(limitSharingPerPeriodServiceMonth.getForPeriod(any(), any())).then(inv -> spendings.get(inv.getArgument(0, LimitSharing.class).getLimit().getId()).parallelStream().map(LimitSpending::getLimitSharingPerPeriod).findAny().orElseThrow());
        when(limitSharingPerPeriodServiceMonth.getPeriodsBySharings(any(), any())).then(inv -> getLimitSharingLimitSharingPerPeriodMap(inv, spendings));
        when(limitSharingPerPeriodServiceMonth.getForDate(any(), any())).then(inv -> spendings.get(inv.getArgument(0, LimitSharing.class).getLimit().getId()).parallelStream().map(LimitSpending::getLimitSharingPerPeriod).findAny().orElseThrow());
        when(limitSharingPerPeriodServiceMonth.getPeriodsForDateBySharings(any(), any())).then(inv -> getLimitSharingLimitSharingPerPeriodMap(inv, spendings));

        var actual = limitStatsService.getLimitStatsV2(organizationId, data, date);

        assertThat(actual).isNotEmpty().allMatch(Objects::nonNull);
    }

    @Test
    @DisplayName("Проверка исключения при отсутствии периода")
    void shouldThrowExceptionWhenPeriodNotFound() {
        var organizationId = UUID.randomUUID();
        var date = LocalDate.now();
        var depLimit = Instancio.create(DepLimit.class);
        depLimit.setLimitStatus(LimitStatus.CLOSED);
        depLimit.setLimitSharingType(LimitSharingType.MONTHLY);

        var sharing = Instancio.create(LimitSharing.class);
        sharing.setLimit(depLimit);
        depLimit.setSharings(List.of(sharing));

        var levelDTO = Instancio.create(LimitLevelDTO.class);
        levelDTO.setLimit(depLimit);

        when(limitStatsData.department(any())).thenReturn(depLimit.getDepartment());
        when(limitStatsData.untilDate()).thenReturn(date);
        when(limitSharingService.getByLimit(any())).thenReturn(List.of(sharing));
        when(limitStatsData.popSpends(any())).thenReturn(new LinkedList<>());
        when(limitSharingPerPeriodServiceMonth.getPeriodsBySharings(any(), any())).thenReturn(Collections.emptyMap());

        var exception = assertThrows(
                ExecutionException.class,
                () -> limitStatsService.getLimitStatsV2(organizationId, List.of(levelDTO), date)
        );

        assertThat(exception.getMessage())
                .contains("Ошибка: LimitSharingPerPeriod не найден для limitSharing '")
                .contains(sharing.getId().toString());
    }

    @NotNull
    private static Map<LimitSharing, LimitSharingPerPeriod> getLimitSharingLimitSharingPerPeriodMap(InvocationOnMock inv, Map<UUID, List<LimitSpending>> spendings) {
        Collection<LimitSharing> sharings = inv.getArgument(0);

        Map<LimitSharing, LimitSharingPerPeriod> result = new HashMap<>();

        for (LimitSharing sharing : sharings) {
            LimitSharingPerPeriod periodObj = spendings.get(sharing.getLimit().getId())
                    .parallelStream()
                    .map(LimitSpending::getLimitSharingPerPeriod)
                    .filter(p -> p.getLimitSharing().equals(sharing))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No period found for sharing " + sharing.getId()));

            result.put(sharing, periodObj);
        }

        return result;
    }

}