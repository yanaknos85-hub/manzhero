package ru.sber.transport.fraud.monitoring.model;

import ru.sber.transport.web.model.FraudMarkerDto;
import ru.sber.transport.web.model.MessagingDto;

import java.time.ZoneId;

/**
 * Объект ответа с данными о нарушении (фроде)
 */
public class WebFraudMarkerWithMessagesDto extends FraudMarkerDto {
    private final ZoneId zoneMoscow = ZoneId.of("Europe/Moscow");

    public WebFraudMarkerWithMessagesDto(FraudCaseData delegatee) {
        setId(delegatee.getId());
        setAiVerdict(delegatee.getAiVerdict());
        setAiComment(delegatee.getAiComment());
        setComment(delegatee.getComment());
        setNeedValidation(delegatee.isNeedValidation());
        setMessaging(delegatee.getMessaging().stream()
                .map(message -> new MessagingDto(message.getFromEmail(),
                        message.getToEmail(),
                        message.getMessageDate().atZone(zoneMoscow).withFixedOffsetZone().toOffsetDateTime(),
                        message.getBody()))
                .toList());
    }
}
