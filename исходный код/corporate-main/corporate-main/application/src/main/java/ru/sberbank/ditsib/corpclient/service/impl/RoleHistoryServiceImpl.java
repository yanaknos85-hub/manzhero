package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.corpclient.database.dao.RoleHistoryRepository;
import ru.sberbank.ditsib.corpclient.database.model.RoleActionType;
import ru.sberbank.ditsib.corpclient.database.model.RoleHistory;
import ru.sberbank.ditsib.corpclient.service.RoleHistoryService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Реализация сервиса для работы с историей назначения ролей.
 */
@Service
@RequiredArgsConstructor
@Slf4j
class RoleHistoryServiceImpl implements RoleHistoryService {

    private final RoleHistoryRepository repository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveHistory(UUID employeeId, String roleNames, RoleActionType actionType, String comment) {
        RoleHistory history = new RoleHistory(
                null,
                LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())),
                actionType,
                comment,
                employeeId,
                roleNames
        );

        log.info("Saving role history: employeeId={}, roleNames={}, actionType={}",
                employeeId, roleNames, actionType);

        repository.save(history);
    }
}
