package ru.sberbank.ditsib.transport.approvals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Проверка запуска")
class ApplicationTest {

    @Test
    void testApplication() {
        var mockStatic = Mockito.mockStatic(SpringApplication.class);
        mockStatic.when((MockedStatic.Verification) SpringApplication.run(ApprovalsApplication.class, new String[]{})).thenReturn(null);
        ApprovalsApplication.main();
        assertNull(SpringApplication.run(ApprovalsApplication.class));
        mockStatic.close();
    }
}