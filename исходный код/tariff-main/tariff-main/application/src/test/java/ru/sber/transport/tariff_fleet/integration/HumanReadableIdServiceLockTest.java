package ru.sber.transport.tariff_fleet.integration;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.humanreadableid.model.interfaces.Prefix;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.tariff_fleet.service.OrganizationService;
import ru.sber.transport.tariff_fleet.service.impl.HumanReadableIdServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class HumanReadableIdServiceLockTest {
    @Mock
    OrganizationService organizationService;
    private final AtomicLong atomicLong = new AtomicLong();

    @Test
    void testParallelAccessToDifferentOrganizationsByUserId() throws ExecutionException, InterruptedException {
        var humanReadableIdService = new HumanReadableIdServiceImpl(organizationService, new TestSQGenerator());
        doReturn(atomicLong.incrementAndGet()).when(organizationService).getDigitIdByUserId(any(UUID.class));
        int numThreads = 10;
        List<Callable<String>> tasks = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++) {
            var uuid = UUID.randomUUID();
            Callable<String> task = () -> humanReadableIdService.createHumanReadableIdByUserId(uuid);
            tasks.add(task);
        }

        var executor = Executors.newFixedThreadPool(numThreads);
        var futures = executor.invokeAll(tasks);

        assertEquals(numThreads, futures.size());
        for (var future : futures) {
            assertNotNull(future.get());
        }
    }

    @Test
    void testSequentialAccessToSameOrganizationByUserId() throws InterruptedException {
        doReturn(atomicLong.incrementAndGet()).when(organizationService).getDigitIdByUserId(any(UUID.class));
        var humanReadableIdService = new HumanReadableIdServiceImpl(organizationService, new TestSQGenerator());
        UUID commonUuid = UUID.randomUUID();
        int numThreads = 10;
        var latch = new CountDownLatch(numThreads);
        var counter = new AtomicInteger();

        Runnable runnableTask = () -> {
            latch.countDown();
            try {
                latch.await();
                humanReadableIdService.createHumanReadableIdByUserId(commonUuid);
                counter.incrementAndGet();
            } catch (InterruptedException ignored) {
                //skip
            }
        };

        var executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            executor.submit(runnableTask);
        }

        executor.shutdown();
        var result = executor.awaitTermination(10L, TimeUnit.SECONDS);
        assertTrue(result);
        assertEquals(numThreads, counter.get());
    }

    @Test
    void testParallelAccessToDifferentOrganizationsByDigitId() throws ExecutionException, InterruptedException {
        var humanReadableIdService = new HumanReadableIdServiceImpl(organizationService, new TestSQGenerator());
        int numThreads = 10;
        List<Callable<String>> tasks = new ArrayList<>(numThreads);

        for (int i = 0; i < numThreads; i++) {
            var digitId = Instancio.create(Long.class);
            Callable<String> task = () -> humanReadableIdService.createHumanReadableIdByDigitId(digitId);
            tasks.add(task);
        }

        var executor = Executors.newFixedThreadPool(numThreads);
        var futures = executor.invokeAll(tasks);

        assertEquals(numThreads, futures.size());
        for (var future : futures) {
            assertNotNull(future.get());
        }
    }

    @Test
    void testSequentialAccessToSameOrganizationByDigitId() throws InterruptedException {
        var humanReadableIdService = new HumanReadableIdServiceImpl(organizationService, new TestSQGenerator());
        var commonUuid = Instancio.create(Long.class);
        int numThreads = 10;
        var latch = new CountDownLatch(numThreads);
        var counter = new AtomicInteger();

        Runnable runnableTask = () -> {
            latch.countDown();
            try {
                latch.await();
                humanReadableIdService.createHumanReadableIdByDigitId(commonUuid);
                counter.incrementAndGet();
            } catch (InterruptedException ignored) {
                //skip
            }
        };

        var executor = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            executor.submit(runnableTask);
        }

        executor.shutdown();
        var result = executor.awaitTermination(10L, TimeUnit.SECONDS);
        assertTrue(result);
        assertEquals(numThreads, counter.get());
    }

    @Transactional
    @SuppressWarnings("java:S2925")
    public static class TestSQGenerator implements SQGenerator {
        @Override
        public String getNextId(@NotNull Prefix prefix, @Min(1L) @Max(9999L) Long organizationId) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "ID-" + organizationId;
        }

        @Override
        public Iterable<String> reserve(@NotNull Prefix prefix, @Min(1L) @Max(9999L) Long organizationId, int count) {
            return null;
        }
    }
}
