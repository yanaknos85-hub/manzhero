package ru.sber.transport.integrations.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.integrations.config.ContractorBlockProperties;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Сервис для управления состоянием контрагента.
 * В кеше хранятся только заблокированные (нездоровые) контрагенты ({@code contractorId}).
 * При ошибке обработки — блокирует на время, заданное в конфигурации.
 * Автоматическая разблокировка происходит при истечении таймаута в {@link ContractorCacheService#isAllowed(String)}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractorCacheService {
    private static final DateTimeFormatter BLOCKED_UNTIL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Отображение: {@code contractorId} -> время разблокировки.
     * Хранит только заблокированные контрагенты.
     */
    private final ConcurrentHashMap<String, Long> blockedContractorIds = new ConcurrentHashMap<>();

    /**
     * Блокировки на каждого контрагента, чтобы синхронизировать потоки одного {@code contractorId}
     */
    private final ConcurrentHashMap<String, Lock> contractorIdLocks = new ConcurrentHashMap<>();

    /**
     * Источник свойств
     */
    private final ContractorBlockProperties contractorBlockProperties;

    /**
     * Часы с временной зоной UTC
     */
    private final Clock clock;

    /**
     * Проверяет, можно ли обрабатывать сообщения для данного {@code contractorId}.
     * Возвращает {@code false} если контрагент заблокирован и время разблокировки ещё не истекло.
     * При истечении таймаута — автоматически удаляет запись из кеша.
     *
     * @param contractorId идентификатор контрагента
     * @return {@code true} если обработка разрешена, {@code false} если заблокирован
     */
    public boolean isAllowed(String contractorId) {
        var lock = getLock(contractorId);
        lock.lock();
        try {
            var blockedUntil = blockedContractorIds.get(contractorId);
            if (blockedUntil == null) {
                return true;
            }
            if (blockedUntil > clock.millis()) {
                return false;
            }
            log.info("Contractor ID {} is unblocked", contractorId);
            blockedContractorIds.remove(contractorId);
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Помечает {@code contractorId} как заблокированный на время, заданное в конфигурации.
     * Все последующие сообщения этого контрагента будут пропускаться.
     *
     * @param contractorId идентификатор контрагента
     */
    public void markBlocked(String contractorId) {
        var lock = getLock(contractorId);
        lock.lock();
        try {
            var blockedUntil = clock.millis() + contractorBlockProperties.getDurationMs();
            blockedContractorIds.put(contractorId, blockedUntil);
            var dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(blockedUntil), clock.getZone());
            log.warn("Contractor ID {} is blocked until {}", contractorId, dateTime.format(BLOCKED_UNTIL_FORMATTER));
        } finally {
            lock.unlock();
        }
    }

    private Lock getLock(String contractorId) {
        return contractorIdLocks.computeIfAbsent(contractorId, key -> new ReentrantLock());
    }
}
