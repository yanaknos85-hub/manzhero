package ru.sber.transport.tariff_fleet.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sber.transport.tariff_fleet.service.ContractAutoActivationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContractDeactivationScheduler {
    
    private final ContractAutoActivationService contractAutoActivationService;
    
    @Scheduled(cron = "${scheduler.contract.deactivation.cron}")
    @SchedulerLock(name = "contractActivationScheduler",
                   lockAtLeastFor = "${scheduler.contract.deactivation.lock-at-least-for}",
                   lockAtMostFor = "${scheduler.contract.deactivation.lock-at-most-for}")
    public void schedule() {
        log.info("Start contract deactivation scheduler");
        contractAutoActivationService.deactivate();
        log.info("End contract deactivation scheduler");
    }
}
