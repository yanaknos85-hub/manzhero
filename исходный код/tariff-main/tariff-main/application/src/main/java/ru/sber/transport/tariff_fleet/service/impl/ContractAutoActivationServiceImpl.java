package ru.sber.transport.tariff_fleet.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.tariff_fleet.exception.UnsupportedDocumentTypeException;
import ru.sber.transport.tariff_fleet.service.*;

import java.util.List;

import static ru.sber.transport.tariff_fleet.constant.DocumentType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractAutoActivationServiceImpl implements ContractAutoActivationService {
    
    private final ContractService contractService;
    private final ContractGetInternalService contractGetInternalService;
    private final EwbContractService ewbContractService;
    private final RepairContractService repairContractService;
    private final FuelContractService fuelContractService;
    
    @Override
    public void activate() {
        var allStartedContracts = contractService.getAllStarted();
        if (allStartedContracts.isEmpty()) {
            log.info("Все действующие контракты уже активны");
            return;
        }
        
        log.info("Найдено {} действующих контрактов для активации", allStartedContracts.size());
        int activatedCount = 0;
        for (var contract : allStartedContracts) {
            try {
                var documentType = contractGetInternalService.getContractType(contract.getId());
                switch (documentType) {
                    case EWB -> ewbContractService.autoActivate(contract);
                    case REPAIR_AND_MAINTENANCE -> repairContractService.autoActivate(contract.getId());
                    case FUEL -> fuelContractService.autoActivate(contract.getId());
                    default -> throw new UnsupportedDocumentTypeException(documentType, List.of(REPAIR_AND_MAINTENANCE, FUEL, EWB));
                }

            } catch (Exception e) {
                log.error("Ошибка при активации действующего контракта {}", contract, e);
                continue;
            }
            activatedCount++;
        }
        log.info("Успешно активировали {}/{} действующих контрактов", activatedCount, allStartedContracts.size());
    }
    
    @Override
    public void deactivate() {
        var allEnded = contractService.getAllEnded();
        if (allEnded.isEmpty()) {
            log.info("Все истёкшие контракты уже деактивированы");
            return;
        }
        
        log.info("Найдено {} истёкших контрактов для деактивации", allEnded.size());
        int activatedCount = 0;
        for (var contract : allEnded) {
            try {
                var documentType = contractGetInternalService.getContractType(contract.getId());
                switch (documentType) {
                    case EWB -> ewbContractService.autoDeactivate(contract.getId());
                    case REPAIR_AND_MAINTENANCE -> repairContractService.autoDeactivate(contract.getId());
                    case FUEL -> fuelContractService.autoDeactivate(contract.getId());
                    default -> throw new UnsupportedDocumentTypeException(documentType,
                            List.of(REPAIR_AND_MAINTENANCE, FUEL, EWB));
                }

            } catch (Exception e) {
                log.error("Ошибка при деактивации истёкшего контракта {}", contract, e);
                continue;
            }
            activatedCount++;
        }
        log.info("Успешно деактивировали {}/{} истёкших контрактов", activatedCount, allEnded.size());
    }
}
