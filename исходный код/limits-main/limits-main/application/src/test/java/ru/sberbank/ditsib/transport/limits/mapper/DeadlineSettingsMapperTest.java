package ru.sberbank.ditsib.transport.limits.mapper;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.deadline.messaging.DeadlineSettingsMessage;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.limits.model.deadline.DeadlineSettings;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка маппинга")
class DeadlineSettingsMapperTest {
    
    DeadlineSettingsMapper mapper = Mappers.getMapper(DeadlineSettingsMapper.class);
    
    @Test
    @DisplayName("Маппинг")
    void messageToLimitItem() {
        var depLimit = new DeadlineSettingsMessage.LimitDeadlineSettingsItem(
                UUID.randomUUID(),
                ChronoUnit.MILLIS,
                1800,
                LimitType.DEPARTMENT.getName()
        );
        var empLimit = new DeadlineSettingsMessage.LimitDeadlineSettingsItem(
                UUID.randomUUID(),
                ChronoUnit.MILLIS,
                3600,
                LimitType.EMPLOYEE.getName()
        );
        var message = new DeadlineSettingsMessage(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                empLimit,
                depLimit,
                null,
                false
        );
    
        DeadlineSettings settings = mapper.fromMessage(message);
    
    
        assertThat(settings.getId()).isEqualTo(message.getId());
        assertThat(settings.getOrganizationId()).isEqualTo(message.organizationId());
        
        assertThat(settings.getDepartmentLimitChronoUnit()).isEqualTo(depLimit.unit());
        assertThat(settings.getDepartmentLimitValue()).isEqualTo(depLimit.value());
        
        assertThat(settings.getEmployeeLimitChronoUnit()).isEqualTo(empLimit.unit());
        assertThat(settings.getEmployeeLimitValue()).isEqualTo(empLimit.value());
    }
    
}
