package ru.sber.transport.fraud.monitoring.messaging.listeners.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.fraud.monitoring.model.FraudCaseData;

import java.util.List;
import java.util.UUID;

/**
 * POJO-реализация данных кейса фрода с email-перепиской
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FraudCaseDataImpl implements FraudCaseData {
    private UUID id;
    private String aiVerdict;
    private String aiComment;
    private String comment;
    private boolean needValidation;
    private List<? extends FraudCaseData.FraudMessageItem> messaging;
}