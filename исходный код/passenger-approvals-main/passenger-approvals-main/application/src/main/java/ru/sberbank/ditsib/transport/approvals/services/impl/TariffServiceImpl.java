package ru.sberbank.ditsib.transport.approvals.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.approvals.database.dao.TariffRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.messages.Tariff;
import ru.sberbank.ditsib.transport.approvals.services.TariffService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TariffServiceImpl implements TariffService {
    
    private final TariffRepository tariffRepository;
    
    @Override
    public Tariff save(Tariff tariff) {
        return tariffRepository.save(tariff);
    }
    
    @Override
    public Optional<Tariff> getOptionalById(UUID tariffId) {
        return tariffRepository.findById(tariffId);
    }
    
    @Override
    public Tariff getTariffById(UUID tariffId) throws EntityNotFoundException {
        return getOptionalById(tariffId).orElseThrow(() -> new EntityNotFoundException(Tariff.class, tariffId));
    }

    @Override
    public void delete(Tariff tariff) {
        tariffRepository.delete(tariff);
    }
}
