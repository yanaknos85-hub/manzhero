package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.Tariff;
import ru.sberbank.ditsib.transport.approvals.mappers.TariffMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.listeners.TariffListener;
import ru.sberbank.ditsib.transport.approvals.messaging.message.TariffMessage;
import ru.sberbank.ditsib.transport.approvals.services.TariffService;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class TariffListenerImpl implements TariffListener {
    
    private final TariffMapper mapper;
    private final TariffService tariffService;
    
    @Override
    public void handle(UUID key, TariffMessage message) {
        // использовать hard delete для тарифа, т.к. он не участвует в отображении информации на фронте
        if (message.isDeleted()) {
            Optional<Tariff> optional = tariffService.getOptionalById(key);
            if (optional.isPresent()) {
                tariffService.delete(optional.get());
                log.info("Тариф ID '{}' был удален", key);
            } else {
                log.error("При попытке удаления Тарифа ID '{}', он не был найден в БД", key);
            }
        } else {
            Tariff tariff = mapper.messageToEntity(message);
            tariff.setId(key);
            if (key != null) {
                tariffService.save(tariff);
                log.info("Тариф ID '{}' был записан / отредактирован", tariff.getId());
            } else {
                log.warn("Handled tariff message with no ID: {}", tariff.getHumanReadableId());
            }
        }
    }
}
