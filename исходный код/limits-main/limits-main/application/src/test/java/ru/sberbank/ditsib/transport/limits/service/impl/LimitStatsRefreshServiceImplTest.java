package ru.sberbank.ditsib.transport.limits.service.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitStatistic;
import ru.sberbank.ditsib.transport.limits.model.limit.LimitStatisticSpending;
import ru.sberbank.ditsib.transport.limits.service.LimitStatsRefreshService;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка запуска обновлятора")
class LimitStatsRefreshServiceImplTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

    private final LimitStatsRefreshService limitStatsRefreshService = new LimitStatsRefreshServiceImpl(jdbcTemplate);

    @BeforeAll
    static void beforeAll() {
        ((Logger) LoggerFactory.getLogger(LimitStatsRefreshServiceImpl.class)).setLevel(Level.DEBUG);
    }

    @DisplayName("Обновление")
    @Test
    void test_refresh() throws ExecutionException, InterruptedException {
        var refreshStats = limitStatsRefreshService.refresh(LimitStatistic.class);

        await()
            .until(refreshStats::isDone);

        assertThat(refreshStats.get()).isTrue();

        verify(jdbcTemplate).update("refresh materialized view concurrently \"limits\".\"limit_stats\"");
    }

    @DisplayName("Обновление конкуррентное")
    @Test
    @Disabled("Нестабильный тест")
    void test_refresh_concurrent() throws ExecutionException, InterruptedException {
        var pool = Executors.newFixedThreadPool(1);
        var stats = new CopyOnWriteArrayList<Future<Boolean>>();
        pool.submit(() -> stats.add(limitStatsRefreshService.refresh(LimitStatistic.class).thenComposeAsync(b -> limitStatsRefreshService.refresh(LimitStatisticSpending.class))));

        await()
            .until(() -> stats.stream().allMatch(Future::isDone));

        Thread.sleep(Duration.ofSeconds(1));

        assertThat(stats.getFirst().get()).isTrue();

        var refreshCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, times(2)).update(refreshCaptor.capture());

        assertThat(refreshCaptor.getAllValues())
            .hasSameElementsAs(List.of(
                "refresh materialized view concurrently \"limits\".\"limit_stats\"",
                "refresh materialized view concurrently \"limits\".\"limit_stats_spending\""
            ));
    }

    @DisplayName("Обновление конкуррентное. С ошибкой")
    @Test
    @Disabled
    void test_refresh_concurrent_error() throws ExecutionException, InterruptedException {
        var pool = Executors.newFixedThreadPool(2);
        var stats = new CopyOnWriteArrayList<Future<Boolean>>();

        doAnswer(invocationOnMock -> {
            Thread.sleep(1_000); // NOSONAR
            return null;
        }).when(jdbcTemplate).execute(anyString());

        pool.submit(() -> stats.add(limitStatsRefreshService.refresh(LimitStatistic.class)));
        pool.submit(() -> stats.add(limitStatsRefreshService.refresh(LimitStatistic.class)));

        await()
            .until(() -> stats.stream().allMatch(Future::isDone));

        assertThat(stats).hasSize(2);
        assertThat(stats.getFirst().get()).isTrue();
        assertThat(stats.get(1).get()).isFalse();

        var refreshCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate, times(1)).update(refreshCaptor.capture());

        assertThat(refreshCaptor.getAllValues())
            .hasSameElementsAs(List.of(
                "refresh materialized view concurrently \"limits\".\"limit_stats\""
            ));
    }

}