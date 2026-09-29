package ru.sber.transport.integrations.service;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.sber.transport.integrations.config.ContractorBlockProperties;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ContractorCacheServiceTest {
    private static final Clock FIXED_CLOCK = Clock.systemUTC();
    private static final Instant FIXED_INSTANT = Instant.now(FIXED_CLOCK);

    @Mock
    private ContractorBlockProperties contractorBlockProperties;
    @Mock
    private Clock clock;
    @InjectMocks
    private ContractorCacheService contractorCacheService;

    @Test
    void isAllowed_newContractorId_shouldReturnTrue() {
        assertThat(contractorCacheService.isAllowed(Instancio.of(String.class).create())).isTrue();
    }

    @Test
    void isAllowed_afterMarkBlocked_shouldReturnFalse() {
        var contractorId = Instancio.of(String.class).create();
        var blockDurationMs = Instancio.of(Long.class).create();
        doReturn(blockDurationMs).when(contractorBlockProperties).getDurationMs();
        doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();

        contractorCacheService.markBlocked(contractorId);

        assertThat(contractorCacheService.isAllowed(contractorId)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"10, false", "30, true"})
    void isAllowedWithBlockDuration(int futureSecondsToAdd, boolean expected) {
        var contractorId = Instancio.of(String.class).create();
        var futureBlockedContractorIds = new ConcurrentHashMap<String, Long>();
        futureBlockedContractorIds.put(contractorId, FIXED_INSTANT.toEpochMilli() + 20 * 1000L);
        var futureInstant = FIXED_INSTANT.plusMillis(futureSecondsToAdd * 1000L);
        var futureClock = Clock.fixed(futureInstant, FIXED_CLOCK.getZone());

        var futureContractorCacheService = new ContractorCacheService(contractorBlockProperties, futureClock);
        ReflectionTestUtils.setField(futureContractorCacheService, "blockedContractorIds", futureBlockedContractorIds);

        assertThat(futureContractorCacheService.isAllowed(contractorId)).isEqualTo(expected);
    }

    @Test
    void concurrentAccess_differentContractorIds_canProcessInParallel() throws InterruptedException {
        try (var executorService = Executors.newFixedThreadPool(10)) {
            var countDownLatch = new CountDownLatch(20);
            var allowedCount = new AtomicInteger(0);

            var contractorId1 = Instancio.of(String.class).create();
            var contractorId2 = Instancio.of(String.class).create();

            for (int i = 0; i < 10; i++) {
                executorService.submit(() -> {
                    try {
                        if (contractorCacheService.isAllowed(contractorId1)) {
                            allowedCount.incrementAndGet();
                        }
                    } finally {
                        countDownLatch.countDown();
                    }
                });
                executorService.submit(() -> {
                    try {
                        if (contractorCacheService.isAllowed(contractorId2)) {
                            allowedCount.incrementAndGet();
                        }
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }

            if (countDownLatch.await(5, TimeUnit.SECONDS)) {
                executorService.shutdown();
                if (executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    assertThat(allowedCount.get()).isEqualTo(20);
                }
            }
        }
    }

    @Test
    void blockOneContractor_doesNotAffectOthers() throws InterruptedException {
        try (var executorService = Executors.newFixedThreadPool(10)) {
            var countDownLatch = new CountDownLatch(10);
            var blockedCount = new AtomicInteger(0);
            var allowedCount = new AtomicInteger(0);

            var blockedContractorId = Instancio.of(String.class).create();
            var healthyContractorId = Instancio.of(String.class).create();

            doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();

            contractorCacheService.markBlocked(blockedContractorId);

            for (int i = 0; i < 5; i++) {
                executorService.submit(() -> {
                    try {
                        if (!contractorCacheService.isAllowed(blockedContractorId)) {
                            blockedCount.incrementAndGet();
                        }
                    } finally {
                        countDownLatch.countDown();
                    }
                });
                executorService.submit(() -> {
                    if (contractorCacheService.isAllowed(healthyContractorId)) {
                        allowedCount.incrementAndGet();
                    }
                });
            }

            if (countDownLatch.await(5, TimeUnit.SECONDS)) {
                executorService.shutdown();
                if (executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    assertThat(blockedCount.get()).isEqualTo(5);
                    assertThat(allowedCount.get()).isEqualTo(5);
                }
            }
        }
    }
}
