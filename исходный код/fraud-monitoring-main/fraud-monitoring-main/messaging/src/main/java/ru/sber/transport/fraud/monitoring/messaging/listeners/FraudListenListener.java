package ru.sber.transport.fraud.monitoring.messaging.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import ru.sber.transport.fraud.monitoring.messaging.listeners.mapper.FraudMapper;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.FraudListenMessage;
import ru.sber.transport.fraud.monitoring.providers.FraudsDatabaseProvider;

import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class FraudListenListener implements Consumer<Message<FraudListenMessage>> {

    public static final String AI_ANTIFRAUD_SOURCE = "ai_antifraud";
    public static final String UNKNOWN_SOURCE = "UNKNOWN";
    public static final String REPEAT_REQUEST_PATTERN = "повторяющиеся заявки";
    private final FraudsDatabaseProvider fraudsDatabaseProvider;
    private final FraudMapper fraudMapper;

    @Override
    public void accept(Message<FraudListenMessage> raw) {
        final var payload = raw.getPayload();
        final var requestId = payload.requestId();

        for (var dataItem : payload.fraudData()) {
            var fraud = fraudMapper.toFraudListenItem(payload, dataItem);
            if (fraud == null) {
                log.error("Failed to map fraud data for request {}", requestId);
                continue;
            }
            fraud.setId(UUID.randomUUID());
            final var comment = dataItem.comment();
            if (comment != null && comment.toLowerCase().contains(REPEAT_REQUEST_PATTERN)) {
                fraud.setSource(AI_ANTIFRAUD_SOURCE);
            } else if (fraud.getSource() == null || fraud.getSource().isEmpty()) {
                fraud.setSource(UNKNOWN_SOURCE);
            }
            fraudsDatabaseProvider.save(fraud);
            log.debug("Successfully saved fraud with id: {}", fraud.getId());
        }
    }
}
