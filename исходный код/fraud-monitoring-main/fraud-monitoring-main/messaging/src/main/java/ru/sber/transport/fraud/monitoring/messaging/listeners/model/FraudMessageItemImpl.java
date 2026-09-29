package ru.sber.transport.fraud.monitoring.messaging.listeners.model;

import lombok.*;
import ru.sber.transport.fraud.monitoring.model.FraudCaseData;

import java.time.LocalDateTime;

/**
 * POJO-реализация email-сообщения
 */
@Value
@Builder
public class FraudMessageItemImpl implements FraudCaseData.FraudMessageItem {
    LocalDateTime messageDate;
    String fromEmail;
    String toEmail;
    String body;
}