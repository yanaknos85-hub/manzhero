package ru.sber.transport.tariff_fleet.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.tariff_fleet.constant.DocumentType;
import ru.sber.transport.tariff_fleet.database.model.Contract;
import ru.sber.transport.tariff_fleet.service.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса по автоматическому смену статуса договора")
class ContractAutoActivationServiceImplTest {
    
    @InjectMocks
    private ContractAutoActivationServiceImpl contractAutoActivationServiceImpl;
    @Mock
    private ContractService contractService;
    @Mock
    private EwbContractService ewbContractService;
    @Mock
    private ContractGetInternalService contractGetInternalService;
    @Mock
    private RepairContractService repairContractService;
    @Mock
    private FuelContractService fuelContractService;
    
    @Test
    void activateEWB() {
        when(contractService.getAllStarted())
                .thenReturn(Collections.emptyList());
        contractAutoActivationServiceImpl.activate();
        verify(contractService).getAllStarted();
        verifyNoInteractions(ewbContractService);
        
        when(contractService.getAllStarted())
                .thenReturn(List.of(Instancio.create(Contract.class), Instancio.create(Contract.class), Instancio.create(Contract.class)));
        doReturn(DocumentType.EWB).when(contractGetInternalService).getContractType(any());
        doNothing().when(ewbContractService).autoActivate(any(Contract.class));
        contractAutoActivationServiceImpl.activate();
        verify(ewbContractService, times(3)).autoActivate(any(Contract.class));
    }
    
    @Test
    void deactivateEWB() {
        when(contractService.getAllEnded())
                .thenReturn(Collections.emptyList());
        doReturn(DocumentType.EWB).when(contractGetInternalService).getContractType(any());
        contractAutoActivationServiceImpl.deactivate();
        verify(contractService).getAllEnded();
        verifyNoInteractions(ewbContractService);
        
        when(contractService.getAllEnded())
                .thenReturn(List.of(Instancio.create(Contract.class), Instancio.create(Contract.class), Instancio.create(Contract.class)));
        doNothing().when(ewbContractService).autoDeactivate(any(UUID.class));
        contractAutoActivationServiceImpl.deactivate();
        verify(ewbContractService, times(3)).autoDeactivate(any(UUID.class));
    }

    @Test
    void activateFUEL() {
        when(contractService.getAllStarted())
                .thenReturn(List.of(Instancio.create(Contract.class), Instancio.create(Contract.class), Instancio.create(Contract.class)));
        doReturn(DocumentType.FUEL).when(contractGetInternalService).getContractType(any());
        doNothing().when(fuelContractService).autoActivate(any(UUID.class));
        contractAutoActivationServiceImpl.activate();
        verify(fuelContractService, times(3)).autoActivate(any(UUID.class));
    }

    @Test
    void activateREPAIR() {
        when(contractService.getAllStarted())
                .thenReturn(List.of(Instancio.create(Contract.class), Instancio.create(Contract.class), Instancio.create(Contract.class)));
        doReturn(DocumentType.REPAIR_AND_MAINTENANCE).when(contractGetInternalService).getContractType(any());
        doNothing().when(repairContractService).autoActivate(any(UUID.class));
        contractAutoActivationServiceImpl.activate();
        verify(repairContractService, times(3)).autoActivate(any(UUID.class));
    }

    @Test
    void deactivateFUEL() {
        when(contractService.getAllEnded())
                .thenReturn(List.of(Instancio.create(Contract.class), Instancio.create(Contract.class), Instancio.create(Contract.class)));
        doReturn(DocumentType.FUEL).when(contractGetInternalService).getContractType(any());
        doNothing().when(fuelContractService).autoDeactivate(any(UUID.class));
        contractAutoActivationServiceImpl.deactivate();
        verify(fuelContractService, times(3)).autoDeactivate(any(UUID.class));
    }

    @Test
    void deactivateREPAIR() {
        when(contractService.getAllEnded())
                .thenReturn(List.of(Instancio.create(Contract.class), Instancio.create(Contract.class), Instancio.create(Contract.class)));
        doReturn(DocumentType.REPAIR_AND_MAINTENANCE).when(contractGetInternalService).getContractType(any());
        doNothing().when(repairContractService).autoDeactivate(any(UUID.class));
        contractAutoActivationServiceImpl.deactivate();
        verify(repairContractService, times(3)).autoDeactivate(any(UUID.class));
    }
}
