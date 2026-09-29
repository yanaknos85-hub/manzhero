package ru.sberbank.ditsib.transport.limits.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.limits.constants.LimitRequestStatus;
import ru.sberbank.ditsib.transport.limits.dao.LimitRequestRepository;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.service.LimitRequestService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка сервиса для работы с заявками на лимит")
public class LimitRequestServiceImplTest {

    private final LimitRequestRepository limitRequestRepository = mock(LimitRequestRepository.class);
    private final LimitRequestService limitRequestService = new LimitRequestServiceImpl(limitRequestRepository, null, null, null, null, null, null, null, null);

    @Test
    @DisplayName("Получение статистики количества заявок по типу транспорта. Активные")
    void test_getStatistic_active() {
        var employee = Instancio.create(Employee.class);
        limitRequestService.getStatistic(true, employee);

        var listCaptor = ArgumentCaptor.forClass(List.class);
        var employeeCaptor = ArgumentCaptor.forClass(UUID.class);

        verify(limitRequestRepository, times(1)).getStatsByTransportType(listCaptor.capture(), employeeCaptor.capture());

        assertEquals(employee.getId(), employeeCaptor.getValue());

        var actualList = listCaptor.getValue();
        assertEquals(1, actualList.size());
        assertEquals(LimitRequestStatus.INIT, actualList.getFirst());
    }

    @Test
    @DisplayName("Получение статистики количества заявок по типу транспорта. Не активные")
    void test_getStatistic_notActive() {
        var employee = Instancio.create(Employee.class);
        limitRequestService.getStatistic(false, employee);

        var listCaptor = ArgumentCaptor.forClass(List.class);
        var employeeCaptor = ArgumentCaptor.forClass(UUID.class);

        verify(limitRequestRepository, times(1)).getStatsByTransportType(listCaptor.capture(), employeeCaptor.capture());

        assertEquals(employee.getId(), employeeCaptor.getValue());

        var actualList = listCaptor.getValue();
        assertEquals(3, actualList.size());
        assertEquals(LimitRequestStatus.DONE_FULLY, actualList.getFirst());
        assertEquals(LimitRequestStatus.DONE_PARTLY, actualList.get(1));
        assertEquals(LimitRequestStatus.CANCELLED, actualList.get(2));
    }
}
