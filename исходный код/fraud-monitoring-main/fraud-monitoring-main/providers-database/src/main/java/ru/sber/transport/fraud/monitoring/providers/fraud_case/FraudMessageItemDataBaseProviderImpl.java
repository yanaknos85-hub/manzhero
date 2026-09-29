package ru.sber.transport.fraud.monitoring.providers.fraud_case;

import lombok.extern.slf4j.Slf4j;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.fraud_monitoring.Tables;
import ru.sber.transport.database.fraud_monitoring.tables.Messaging;
import ru.sber.transport.database.fraud_monitoring.tables.records.MessagingRecord;
import ru.sber.transport.fraud.monitoring.providers.FraudMessageItemDataBaseProvider;

import java.util.List;
import java.util.UUID;

import static ru.sber.transport.database.fraud_monitoring.Tables.MESSAGING;
import static ru.sber.transport.fraud.monitoring.model.FraudCaseData.*;

/**
 * Реализация {@link FraudMessageItemDataBaseProvider} для сохранения email-сообщений
 * кейса фрода в таблицу {@code MESSAGING} через jOOQ.
 */
@Slf4j
public class FraudMessageItemDataBaseProviderImpl implements FraudMessageItemDataBaseProvider, JooqRepository<Messaging, MessagingRecord, UUID> {
    @Override
    public Messaging table() {
        return Tables.MESSAGING;
    }

    @Override
    public void saveFraudMessageItem(List<FraudMessageItem> messageItems, UUID fraudId) {
        if (messageItems == null || messageItems.isEmpty()) {
            log.info("Нет email-сообщений для сохранения. Кейс: {}", fraudId);
            return;
        }

        log.debug("Сохранение {} email-сообщений для кейса {}", messageItems.size(), fraudId);

        var insert = context().insertInto(MESSAGING)
                .columns(MESSAGING.ID, MESSAGING.FRAUD_CASE_ID, MESSAGING.MESSAGE_DATE,
                        MESSAGING.FROM_EMAIL, MESSAGING.TO_EMAIL, MESSAGING.BODY);

        for (var item : messageItems) {
            insert.values(UUID.randomUUID(), fraudId, item.getMessageDate(),
                    item.getFromEmail(), item.getToEmail(), item.getBody());
        }

        insert.execute();

        log.info("Данные кейса фрода успешно сохранены: {}", fraudId);
    }
}
