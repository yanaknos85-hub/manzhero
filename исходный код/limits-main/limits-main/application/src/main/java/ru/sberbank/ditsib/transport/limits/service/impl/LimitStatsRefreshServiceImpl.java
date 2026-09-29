package ru.sberbank.ditsib.transport.limits.service.impl;

import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import ru.sberbank.ditsib.transport.limits.service.LimitStatsRefreshService;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
@Component
class LimitStatsRefreshServiceImpl implements LimitStatsRefreshService {

    private final AtomicBoolean busy = new AtomicBoolean();

    private final Executor executor = Executors.newSingleThreadExecutor(new BasicThreadFactory.Builder().namingPattern("refresh").build());

    private final JdbcTemplate jdbcTemplate;

    @Override
    public synchronized CompletableFuture<Boolean> refresh(Class<?> statsClass) {
        var completable = new CompletableFuture<Boolean>();
        var table = getTableCoordinates(statsClass);
        if (!busy.getAndSet(true)) {
            executor.execute(() -> {
                var stopwatch = new StopWatch();
                stopwatch.start("Refresh %s".formatted(table));
                log.debug("Refreshing of table {} started", table);
                jdbcTemplate.update("refresh materialized view concurrently %s".formatted(table));
                completable.complete(true);
                busy.set(false);
                stopwatch.stop();
                var lastTask = stopwatch.lastTaskInfo();
                var took = Duration.ofMillis(lastTask.getTimeMillis());
                log.debug("%s finished. Took %02d:%02d.%03d".formatted(lastTask.getTaskName(), took.toMinutes(), took.toSecondsPart(), took.toMillisPart()));
            });
        } else {
            completable.complete(false);
        }
        return completable;
    }

    private String getTableCoordinates(Class<?> tableClass) {
        var tableAnnotation = tableClass.getAnnotation(Table.class);
        var schema = "public";
        var name = tableClass.getSimpleName();
        if (tableAnnotation != null) {
            var annSchema = tableAnnotation.schema();
            if (!annSchema.isBlank()) {
                schema = annSchema;
            }
            var annTable = tableAnnotation.name();
            if (!annTable.isBlank()) {
                name = annTable;
            }
        }
        return "\"%s\".\"%s\"".formatted(schema, name);
    }
}
