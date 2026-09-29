package ru.sber.transport.fraud.monitoring.messaging.listeners.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.FraudCaseDataMessage;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.instancio.Select.field;

@UnitTest
@IsolatedTest
@Isolated
@DisplayName("Проверка маппинга FraudCaseDataMessage в FraudCaseData")
class FraudCaseDataMapperTest {

    private final FraudCaseDataMapper mapper = Mappers.getMapper(FraudCaseDataMapper.class);

    private FraudCaseDataMessage createMessage() {
        return new FraudCaseDataMessage(
                UUID.randomUUID(),
                null,
                "AI_VERDICT",
                "AI_COMMENT",
                true
        );
    }

    @Test
    @DisplayName("Маппинг полного сообщения")
    void testFullMapping() {
        final var messaging = Instancio.ofList(FraudCaseDataMessage.MessageItem.class)
                .size(2)
                .create();

        final var message = Instancio.of(FraudCaseDataMessage.class)
                .set(field(FraudCaseDataMessage.class, "id"), UUID.randomUUID())
                .set(field(FraudCaseDataMessage.class, "aiVerdict"), "FRAUD_CONFIRMED")
                .set(field(FraudCaseDataMessage.class, "needValidation"), true)
                .set(field(FraudCaseDataMessage.class, "messaging"), messaging)
                .create();

        var result = mapper.toFraudCaseData(message);

        assertSoftly(softly -> {
            softly.assertThat(result.getId()).isEqualTo(message.id());
            softly.assertThat(result.getAiVerdict()).isEqualTo(message.aiVerdict());
            softly.assertThat(result.isNeedValidation()).isEqualTo(message.needValidation());
            softly.assertThat(result.getMessaging()).hasSize(2);
        });
    }

    @Test
    @DisplayName("Маппинг с null messaging")
    void testNullMessaging() {
        final var message = createMessage();

        final var result = mapper.toFraudCaseData(message);

        assertSoftly(softly -> {
            softly.assertThat(result.getId()).isEqualTo(message.id());
            softly.assertThat(result.getAiVerdict()).isEqualTo(message.aiVerdict());
            softly.assertThat(result.isNeedValidation()).isEqualTo(message.needValidation());
            softly.assertThat(result.getMessaging()).isNull();
        });
    }

    @Test
    @DisplayName("Маппинг каждого MessageItem")
    void testMessageItemMapping() {
        final var item = Instancio.of(FraudCaseDataMessage.MessageItem.class)
                .set(field(FraudCaseDataMessage.MessageItem.class, "messageDate"), LocalDateTime.now())
                .set(field(FraudCaseDataMessage.MessageItem.class, "fromEmail"), "sender@test.com")
                .set(field(FraudCaseDataMessage.MessageItem.class, "toEmail"), "receiver@test.com")
                .set(field(FraudCaseDataMessage.MessageItem.class, "body"), "test body")
                .create();

        var result = mapper.toMessageItem(item);

        assertSoftly(softly -> {
            softly.assertThat(result.getMessageDate()).isEqualTo(item.messageDate());
            softly.assertThat(result.getFromEmail()).isEqualTo(item.fromEmail());
            softly.assertThat(result.getToEmail()).isEqualTo(item.toEmail());
            softly.assertThat(result.getBody()).isEqualTo(item.body());
        });
    }

    @Test
    @DisplayName("Маппинг списка MessageItem")
    void testMessageItemListMapping() {
        final var items = Instancio.ofList(FraudCaseDataMessage.MessageItem.class)
                .size(3)
                .create();

        final var message = createMessage();
        final var messageWithItems = new FraudCaseDataMessage(
                message.id(),
                items,
                message.aiVerdict(),
                message.aiComment(),
                message.needValidation()
        );

        var result = mapper.toMessageItemList(messageWithItems);

        assertThat(result).hasSize(3);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getMessageDate()).isEqualTo(items.get(i).messageDate());
            assertThat(result.get(i).getFromEmail()).isEqualTo(items.get(i).fromEmail());
            assertThat(result.get(i).getToEmail()).isEqualTo(items.get(i).toEmail());
            assertThat(result.get(i).getBody()).isEqualTo(items.get(i).body());
        }
    }
}