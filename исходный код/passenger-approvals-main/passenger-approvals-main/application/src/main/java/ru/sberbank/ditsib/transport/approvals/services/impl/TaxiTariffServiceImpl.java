package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.approvals.database.dao.TaxiTariffRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.Tariff;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.TaxiTariff;
import ru.sberbank.ditsib.transport.approvals.services.TaxiTariffService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxiTariffServiceImpl implements TaxiTariffService {
    
    private final TaxiTariffRepository tariffRepository;
    
    @Override
    public TaxiTariff save(TaxiTariff tariff) {
        return tariffRepository.save(tariff);
    }
    
    @Override
    public Optional<TaxiTariff> getOptionalById(UUID tariffId) {
        return tariffRepository.findById(tariffId);
    }
    
    @Override
    public TaxiTariff getTariffById(UUID tariffId) throws EntityNotFoundException {
        return getOptionalById(tariffId).orElseThrow(() -> new EntityNotFoundException(Tariff.class, tariffId));
    }

    @Override
    public void delete(TaxiTariff tariff) {
        tariffRepository.delete(tariff);
    }
}
