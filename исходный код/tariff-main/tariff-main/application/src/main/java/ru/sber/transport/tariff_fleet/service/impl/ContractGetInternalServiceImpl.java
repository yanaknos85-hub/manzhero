package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.dao.ContractRepository;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.exception.ContractNotFoundException;
import ru.sber.transport.tariff_fleet.service.ContractGetInternalService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractGetInternalServiceImpl implements ContractGetInternalService {
    private final ContractRepository contractRepository;

    @Transactional(readOnly = true)
    public DocumentType getContractType(UUID contractId) {
        return contractRepository.getTypeById(contractId)
                .orElseThrow(() -> new ContractNotFoundException(contractId));
    }

    @Override
    public Optional<Contract> getContract(UUID contractId) {
        return contractRepository.findById(contractId);
    }
}
