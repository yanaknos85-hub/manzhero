package ru.sber.transport.fraud.monitoring.providers.fraud_case;

import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.NoDataFoundException;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.database.fraud_monitoring.Tables;
import ru.sber.transport.database.fraud_monitoring.tables.Fraud;
import ru.sber.transport.database.fraud_monitoring.tables.records.FraudRecord;
import ru.sber.transport.fraud.monitoring.model.FraudCaseData;
import ru.sber.transport.fraud.monitoring.model.FraudCaseDataMarker;
import ru.sber.transport.fraud.monitoring.model.FraudConstants;
import ru.sber.transport.fraud.monitoring.providers.FraudCasesDataBaseProvider;
import ru.sber.transport.fraud.monitoring.providers.fraud_case.model.FraudCaseDataMarkerModel;

import java.util.Optional;
import java.util.UUID;

@Slf4j
public class FraudCasesDataBaseProviderImpl implements FraudCasesDataBaseProvider, JooqRepository<Fraud, FraudRecord, UUID> {

    @Override
    public Fraud table() {
        return Tables.FRAUD;
    }

    @Override
    public void updateFraudCase(FraudCaseDataMarker source, FraudCaseData newData) {
        if (source == null) {
            return;
        }

        log.info("Сохранение кейса фрода: {}", source.getId());

        var cleared = FraudConstants.NOT_FRAUD_VERDICT.equals(newData.getAiVerdict());

        context().insertInto(Tables.FRAUD)
                .set(Tables.FRAUD.ID, source.getId())
                .set(Tables.FRAUD.REQUEST_ID, source.getRequestId())
                .set(Tables.FRAUD.COMMENT, source.getComment())
                .set(Tables.FRAUD.FRAUD_TYPE, source.getFraudType())
                .set(Tables.FRAUD.SOURCE, source.getSource())
                .set(Tables.FRAUD.AI_VERDICT, newData.getAiVerdict())
                .set(Tables.FRAUD.AI_COMMENT, newData.getAiComment())
                .set(Tables.FRAUD.NEED_VALIDATION, newData.isNeedValidation())
                .set(Tables.FRAUD.CLEARED, cleared)
                .onConflict(Tables.FRAUD.ID)
                .doUpdate()
                .set(Tables.FRAUD.REQUEST_ID, source.getRequestId())
                .set(Tables.FRAUD.COMMENT, source.getComment())
                .set(Tables.FRAUD.FRAUD_TYPE, source.getFraudType())
                .set(Tables.FRAUD.SOURCE, source.getSource())
                .set(Tables.FRAUD.AI_VERDICT, newData.getAiVerdict())
                .set(Tables.FRAUD.AI_COMMENT, newData.getAiComment())
                .set(Tables.FRAUD.NEED_VALIDATION, newData.isNeedValidation())
                .set(Tables.FRAUD.CLEARED, cleared)
                .execute();

        log.info("Кейс фрода сохранен: {}", source.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FraudCaseDataMarker> getFraudCaseByFraudCaseId(UUID fraudId) {
        try {
            var fraudCase = context().selectFrom(Tables.FRAUD)
                    .where(Tables.FRAUD.ID.eq(fraudId))
                    .fetchSingle();
            return Optional.of(new FraudCaseDataMarkerModel(fraudCase));
        } catch (NoDataFoundException e) {
            log.warn("Кейс фрода не найден: {}", fraudId);
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void solveFraudCase(FraudCaseDataMarker fraudCase, String verdict, String reason) {
        if (fraudCase == null) {
            return;
        }

        log.info("Сохранение решения по кейсу фрода: {}", fraudCase.getId());

        var cleared = FraudConstants.NOT_FRAUD_VERDICT.equals(verdict);

        context().update(Tables.FRAUD)
                .set(Tables.FRAUD.VERDICT, verdict)
                .set(Tables.FRAUD.REASON, reason)
                .set(Tables.FRAUD.NEED_VALIDATION, false)
                .set(Tables.FRAUD.CLEARED, cleared)
                .where(Tables.FRAUD.ID.eq(fraudCase.getId()))
                .execute();

        log.info("Решение по кейсу фрода сохранено: caseId={}, verdict={}, cleared={}", fraudCase.getId(), verdict, cleared);
    }
}