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
public class ContractActivationScheduler {
    
    private final ContractAutoActivationService contractAutoActivationService;
    
    @Scheduled(cron = "${scheduler.contract.activation.cron}")
    @SchedulerLock(name = "contractActivationScheduler",
                   lockAtLeastFor = "${scheduler.contract.activation.lock-at-least-for}",
                   lockAtMostFor = "${scheduler.contract.activation.lock-at-most-for}")
    public void schedule() {
        log.info("Start contract activation scheduler");
        contractAutoActivationService.activate();
        log.info("End contract activation scheduler");
    }
    
}
