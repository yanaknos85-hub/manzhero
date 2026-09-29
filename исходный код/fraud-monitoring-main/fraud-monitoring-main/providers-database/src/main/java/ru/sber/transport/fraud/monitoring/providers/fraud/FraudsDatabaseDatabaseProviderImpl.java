package ru.sber.transport.fraud.monitoring.providers.fraud;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.fraud_monitoring.Tables;
import ru.sber.transport.database.fraud_monitoring.tables.Fraud;
import ru.sber.transport.database.fraud_monitoring.tables.records.FraudRecord;
import ru.sber.transport.fraud.monitoring.providers.FraudsDatabaseProvider;

import java.util.List;
import java.util.UUID;

/**
 * Реализация провайдера нарушений (фрода)
 */
@Transactional
@RequiredArgsConstructor
public class FraudsDatabaseDatabaseProviderImpl implements FraudsDatabaseProvider, JooqRepository<Fraud, FraudRecord, UUID> {

    @Override
    public Fraud table() {
        return Tables.FRAUD;
    }

    @Override
    public void save(ru.sber.transport.fraud.monitoring.model.Fraud source) {
        context().insertInto(table())
                .set(table().ID, source.getId())
                .set(table().REQUEST_ID, source.getRequestId())
                .set(table().COMMENT, source.getComment())
                .set(table().FRAUD_TYPE, source.getFraudType())
                .set(table().SOURCE, source.getSource())
                .onConflict().doNothing().execute();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ru.sber.transport.fraud.monitoring.model.Fraud> findByRequestId(@NotNull UUID requestId) {
        return context().select()
                .from(table())
                .where(table().REQUEST_ID.eq(requestId))
                .fetch(fraudRecord -> new FraudModel(fraudRecord.into(FraudRecord.class)));
    }
}
