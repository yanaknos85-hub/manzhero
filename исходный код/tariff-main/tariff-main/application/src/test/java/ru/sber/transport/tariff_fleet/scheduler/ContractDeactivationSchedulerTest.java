package ru.sber.transport.tariff_fleet.scheduler;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.sber.transport.tariff_fleet.service.ContractAutoActivationService;

import static org.mockito.Mockito.*;

@SpringJUnitConfig
class ContractDeactivationSchedulerTest {

    @Mock
    private ContractAutoActivationService contractAutoActivationService;

    @InjectMocks
    private ContractDeactivationScheduler contractDeactivationScheduler;

    @Test
    void testSchedule() {
        doNothing().when(contractAutoActivationService).deactivate();
        contractDeactivationScheduler.schedule();
        verify(contractAutoActivationService, times(1)).deactivate();

    }
}
