package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.database.dao.SubContractInternalRepository;
import ru.sber.transport.tariff_fleet.database.model.AbstractContract;
import ru.sber.transport.tariff_fleet.exception.ContractNotFoundException;
import ru.sber.transport.tariff_fleet.service.SubContractInternalService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubContractInternalServiceImpl<E extends AbstractContract> implements SubContractInternalService<E> {
    private final SubContractInternalRepository<E> subContractInternalRepository;

    @Override
    @Transactional
    public E getContract(UUID contractId) {
        return subContractInternalRepository.findById(contractId).orElseThrow(() -> new ContractNotFoundException(contractId));
    }
}
