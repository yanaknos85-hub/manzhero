package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.tariff_fleet.service.HumanReadableIdService;
import ru.sber.transport.tariff_fleet.service.OrganizationService;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static ru.sber.transport.tariff_fleet.human_readable_id.constant.Prefix.TF;

@Service
@RequiredArgsConstructor
@Slf4j
public class HumanReadableIdServiceImpl implements HumanReadableIdService {

    private final OrganizationService organizationService;
    private final SQGenerator sqGenerator;
    private final ConcurrentHashMap<UUID, ReentrantLock> uuidLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, ReentrantLock> longLocks = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public String createHumanReadableIdByUserId(UUID userId) {
        var lock = uuidLocks.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();
        try {
            return sqGenerator.getNextId(
                    TF,
                    organizationService.getDigitIdByUserId(userId)
            );
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional
    public String createHumanReadableIdByDigitId(Long digitId) {
        var lock = longLocks.computeIfAbsent(digitId, k -> new ReentrantLock());
        lock.lock();
        try {
            return sqGenerator.getNextId(
                    TF,
                    digitId
            );
        } finally {
            lock.unlock();
        }
    }
}
