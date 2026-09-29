package ru.sberbank.ditsib.transport.approvals.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.TaxiTariff;
import ru.sberbank.ditsib.transport.approvals.mappers.TariffMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.listeners.TaxiTariffListener;
import ru.sberbank.ditsib.transport.approvals.messaging.message.TaxiTariffMessage;
import ru.sberbank.ditsib.transport.approvals.services.TaxiTariffService;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class TaxiTariffListenerImpl implements TaxiTariffListener {
    
    private final TaxiTariffService taxiTariffService;
    private final TariffMapper mapper;
    
    @Override
    public void handle(UUID key, TaxiTariffMessage message) {
        // использовать hard delete для тарифа, т.к. он не участвует в отображении информации на фронте
        if (message.isDeleted()) {
            Optional<TaxiTariff> optional = taxiTariffService.getOptionalById(key);
            if (optional.isPresent()) {
                taxiTariffService.delete(optional.get());
                log.info("Тариф такси ID '{}' был удален", key);
            } else {
                log.error("При попытке удаления Тарифа такси ID '{}', он не был найден в БД", key);
            }
        } else {
            TaxiTariff tariff = mapper.messageToEntity(message);
            tariff.setId(key);
            tariff.setTransportType("TAXI");
            taxiTariffService.save(tariff);
            log.info("Тариф такси ID '{}' был записан / отредактирован", tariff.getId());
        }
    }
}
