package ru.sberbank.ditsib.transport.limits.controller.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.limits.constants.LimitSharingType;
import ru.sberbank.ditsib.transport.limits.dto.GetLimitInfoDto;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.service.EmployeeService;
import ru.sberbank.ditsib.transport.limits.service.LimitService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка контроллера лимитов")
@ExtendWith(MockitoExtension.class)
class LimitControllerImplTest {
    
    @InjectMocks
    private LimitControllerImpl controller;
    
    @Mock
    private EmployeeService employeeService;
    
    @Mock
    private LimitService limitService;
    
    @BeforeEach
    void setUp() {
    }
    
    @AfterEach
    void tearDown() {
    }
    
    @Test
    @DisplayName("Получение информации о доступном лимимте по дате, типу транспорту и employee")
    void getLimitInfoByDate() {
        var date = LocalDate.of(2023, 2, 2);
        UUID id = UUID.randomUUID();
        var emp = Optional.of(Employee.builder().id(id)
                                   .humanReadableId("TEST")
                                   .organizationId(id).build());
        doReturn(emp)
                .when(employeeService).get(id);
        doReturn(GetLimitInfoDto.builder().transportType(TransportTypeEnum.DEDICATED)
                         .balance(BigDecimal.valueOf(100000))
                         .limitSharingType(LimitSharingType.MONTHLY)
                         .sum(BigDecimal.valueOf(200000))
                         .year(2023)
                         .build())
                .when(limitService)
                .getLimiInfoByDate(emp.get(), date, TransportTypeEnum.DEDICATED);
        var result = controller.getLimitInfoByDate(id, date, TransportTypeEnum.DEDICATED);
        assertEquals(200000L, result.getSum().longValue());
        assertEquals(100000L, result.getBalance().longValue());
        assertEquals(2023, result.getYear());
        assertEquals(LimitSharingType.MONTHLY, result.getLimitSharingType());
        assertEquals(TransportTypeEnum.DEDICATED, result.getTransportType());
    }
}