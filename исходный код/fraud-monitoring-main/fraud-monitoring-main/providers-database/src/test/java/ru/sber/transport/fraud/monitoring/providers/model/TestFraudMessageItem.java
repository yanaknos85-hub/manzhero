package ru.sber.transport.fraud.monitoring.providers.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.fraud.monitoring.model.FraudCaseData;

import java.time.LocalDateTime;

/**
 * Тестовая реализация {@link FraudCaseData.FraudMessageItem} для embedded postgres тестов
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestFraudMessageItem implements FraudCaseData.FraudMessageItem {

    private LocalDateTime messageDate;
    private String fromEmail;
    private String toEmail;
    private String body;
}