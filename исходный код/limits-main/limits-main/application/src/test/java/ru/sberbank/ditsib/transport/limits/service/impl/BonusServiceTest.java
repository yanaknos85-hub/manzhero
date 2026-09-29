package ru.sberbank.ditsib.transport.limits.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.CommonTest;
import ru.sberbank.ditsib.transport.limits.dao.BonusRepository;
import ru.sberbank.ditsib.transport.limits.dao.BonusRequestRepository;
import ru.sberbank.ditsib.transport.limits.model.bonus.BonusOperation;
import ru.sberbank.ditsib.transport.limits.model.bonus.BonusRequestStatus;
import ru.sberbank.ditsib.transport.limits.service.BonusService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тестирование сервиса бонусного счёта")
@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@Transactional
@SpringBootTest
@ActiveProfiles("test")
class BonusServiceTest extends CommonTest {
    
    @Autowired
    BonusRepository bonusRepository;
    
    @Autowired
    BonusRequestRepository bonusRequestRepository;
    
    @Autowired
    private BonusService bonusService;
    
    
    private final String HRID = "OT-00-00";
    
    @BeforeEach
    public void beforeEach() {
        limitSharingPerPeriodRepository.deleteAllInBatch();
        limitSharingRepository.deleteAllInBatch();
        depLimitRepository.deleteAllInBatch();
        employeeRepository.deleteAllInBatch();
        bonusRequestRepository.deleteAllInBatch();
        bonusRepository.deleteAllInBatch();
    }
    
    @Test
    @DisplayName("Получение бонусного счёта, автоматическое создание счёта")
    void test_getBonus() {
        var employee = createEmployee();
        var bonus = bonusService.get(employee.getId());
        assertThat(bonus.getOwnerId()).isEqualTo(employee.getId());
        // По дефолту делаем 2000 руб
        assertThat(bonus.getSum().intValue()).isEqualTo(2000);
        assertThat(bonus.getBalance()).isZero();
        assertThat(bonus.getRequests()).isEmpty();
    }
    
    @Test
    @DisplayName("Резервирование и отмена резервирования бонусного счёта")
    @Disabled("Требуется переработка")
    void test_reserveAndCancelBonus() {
        var employee = createEmployee();
        var bonus = bonusService.get(employee.getUserId());
        bonus.setBalance(BigDecimal.valueOf(1000_00));
        bonusRepository.save(bonus);
        var requestId = UUID.randomUUID();
        var bonusRequest = bonusService.reserve(
                employee.getUserId(),
                BigDecimal.valueOf(500_00),
                requestId,
                HRID,
                TransportTypeEnum.TAXI,
                false);
        assertThat(bonusRequest.getBonus().getOwnerId()).isEqualTo(bonus.getOwnerId());
        assertThat(bonusRequest.getBonus().getBalance()).isEqualTo(500_00L);
        assertThat(bonusRequest.getSum()).isEqualTo(500_00L);
        assertThat(bonusRequest.getStatus()).isEqualTo(BonusRequestStatus.RESERVED);
        assertThat(bonusRequest.getOperation()).isEqualTo(BonusOperation.SPEND);
        assertThat(bonusRequest.getUpdateTime()).isNotNull();
        assertThat(bonusRequest.getCreationTime()).isNotNull();
        assertThat(bonusRequest.getRequestId()).isEqualTo(requestId);
        assertThat(bonusRequest.getReason()).isNotNull();
        assertThat(bonusRequest.getTransportType()).isEqualTo(TransportTypeEnum.TAXI);
        
        bonusService.cancel(bonusRequest);
        
        bonus = bonusService.get(employee.getUserId());
        bonusRequest = bonus.getRequests().getFirst();
        
        assertThat(bonusRequest.getBonus().getOwnerId()).isEqualTo(bonus.getOwnerId());
        assertThat(bonusRequest.getBonus().getBalance()).isEqualTo(1000_00L);
        assertThat(bonusRequest.getSum()).isEqualTo(500_00L);
        assertThat(bonusRequest.getStatus()).isEqualTo(BonusRequestStatus.CANCELED);
        assertThat(bonusRequest.getOperation()).isEqualTo(BonusOperation.SPEND);
        assertThat(bonusRequest.getUpdateTime()).isNotNull();
        assertThat(bonusRequest.getCreationTime()).isNotNull();
        assertThat(bonusRequest.getRequestId()).isEqualTo(requestId);
        assertThat(bonusRequest.getReason()).isNotNull();
        assertThat(bonusRequest.getTransportType()).isEqualTo(TransportTypeEnum.TAXI);
    }
    
    @Test
    @DisplayName("Резервирование и подтверждение резервирования бонусного счёта")
    void test_reserveAndSpendBonus() {
        var employee = createEmployee();
        var bonus = bonusService.get(employee.getUserId());
        bonus.setBalance(BigDecimal.valueOf(1000));
        bonusRepository.save(bonus);
        var requestId = UUID.randomUUID();
        var bonusRequest = bonusService.reserve(
                employee.getUserId(),
                BigDecimal.valueOf(500),
                requestId,
                HRID,
                TransportTypeEnum.TAXI,
                false);
        assertThat(bonusRequest.getBonus().getOwnerId()).isEqualTo(bonus.getOwnerId());
        assertThat(bonusRequest.getBonus().getBalance().intValue()).isEqualTo(500L);
        assertThat(bonusRequest.getSum().intValue()).isEqualTo(500L);
        assertThat(bonusRequest.getStatus()).isEqualTo(BonusRequestStatus.RESERVED);
        assertThat(bonusRequest.getOperation()).isEqualTo(BonusOperation.SPEND);
        assertThat(bonusRequest.getUpdateTime()).isNotNull();
        assertThat(bonusRequest.getCreationTime()).isNotNull();
        assertThat(bonusRequest.getRequestId()).isEqualTo(requestId);
        assertThat(bonusRequest.getReason()).isNotNull();
        assertThat(bonusRequest.getTransportType()).isEqualTo(TransportTypeEnum.TAXI);
        
        bonusService.spend(bonusRequest);
        
        bonus = bonusService.get(employee.getUserId());
        bonusRequest = bonus.getRequests().getFirst();
        
        assertThat(bonusRequest.getBonus().getOwnerId()).isEqualTo(bonus.getOwnerId());
        assertThat(bonusRequest.getBonus().getBalance().intValue()).isEqualTo(500);
        assertThat(bonusRequest.getSum().intValue()).isEqualTo(500);
        assertThat(bonusRequest.getStatus()).isEqualTo(BonusRequestStatus.DONE);
        assertThat(bonusRequest.getOperation()).isEqualTo(BonusOperation.SPEND);
        assertThat(bonusRequest.getUpdateTime()).isNotNull();
        assertThat(bonusRequest.getCreationTime()).isNotNull();
        assertThat(bonusRequest.getRequestId()).isEqualTo(requestId);
        assertThat(bonusRequest.getReason()).isNotNull();
        assertThat(bonusRequest.getTransportType()).isEqualTo(TransportTypeEnum.TAXI);
    }
    
    @Test
    @DisplayName("Пополнение бонусного счёта за счёт лимита подразделения")
    @Disabled("Требуется переработка")
    void test_depositBonus() {
        var employee = createEmployee();
        var bonus = bonusService.get(employee.getUserId());
        bonus.setBalance(BigDecimal.valueOf(0));
        bonusRepository.save(bonus);
        
        createLimitSharingPerPeriod();
        
        var bonusRequest = bonusService.deposit(employee.getUserId(), BigDecimal.valueOf(10), HRID, TransportTypeEnum.TAXI);
        
        bonus = bonusService.get(employee.getUserId());
        bonusRequest = bonus.getRequests().getFirst();
        
        assertThat(bonusRequest.getBonus().getOwnerId()).isEqualTo(bonus.getOwnerId());
        assertThat(bonusRequest.getBonus().getBalance()).isEqualTo(10_00L);
        
        assertThat(bonusRequest.getSum()).isEqualTo(10_00L);
        assertThat(bonusRequest.getStatus()).isEqualTo(BonusRequestStatus.DONE);
        assertThat(bonusRequest.getOperation()).isEqualTo(BonusOperation.DEPOSIT);
        assertThat(bonusRequest.getUpdateTime()).isNotNull();
        assertThat(bonusRequest.getCreationTime()).isNotNull();
        assertThat(bonusRequest.getRequestId()).isNull();
        assertThat(bonusRequest.getReason()).isNotNull();
        assertThat(bonusRequest.getTransportType()).isEqualTo(TransportTypeEnum.TAXI);
    }
    
}
