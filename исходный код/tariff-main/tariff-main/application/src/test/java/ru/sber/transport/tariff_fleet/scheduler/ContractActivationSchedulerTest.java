package ru.sber.transport.tariff_fleet.scheduler;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.sber.transport.tariff_fleet.service.ContractAutoActivationService;

import static org.mockito.Mockito.*;

@SpringJUnitConfig
class ContractActivationSchedulerTest {

    @Mock
    private ContractAutoActivationService contractAutoActivationService;

    @InjectMocks
    private ContractActivationScheduler contractActivationScheduler;

    @Test
    void testSchedule() {
        doNothing().when(contractAutoActivationService).activate();
        contractActivationScheduler.schedule();
        verify(contractAutoActivationService, times(1)).activate();

    }
}