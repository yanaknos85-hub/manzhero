package ru.sberbank.ditsib.transport.approvals.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.Tariff;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.TaxiTariff;
import ru.sberbank.ditsib.transport.approvals.messaging.message.TariffMessage;
import ru.sberbank.ditsib.transport.approvals.messaging.message.TaxiTariffMessage;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface TariffMapper {
    
    Tariff messageToEntity(TariffMessage message);
    TaxiTariff messageToEntity(TaxiTariffMessage message);
}
