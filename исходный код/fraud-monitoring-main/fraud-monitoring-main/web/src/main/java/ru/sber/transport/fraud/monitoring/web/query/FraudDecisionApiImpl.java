package ru.sber.transport.fraud.monitoring.web.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.NativeWebRequest;
import ru.sber.transport.fraud.monitoring.business.FraudDecisionService;
import ru.sber.transport.fraud.monitoring.model.WebDecisionRequest;
import ru.sber.transport.web.api.FraudDecisionApi;
import ru.sber.transport.web.model.DecisionRequest;
import ru.sber.transport.web.model.DecisionResponse;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Реализация сервиса для принятия решений по разбирательствам фрода
 */
@Slf4j
@RequiredArgsConstructor
public class FraudDecisionApiImpl implements FraudDecisionApi {

    private final FraudDecisionService fraudDecisionService;

    @Override
    public CompletableFuture<ResponseEntity<DecisionResponse>> postFraudDecision(
            DecisionRequest decisionRequest, UUID requestId, UUID caseId) {

        final var authentication = SecurityContextHolder.getContext().getAuthentication();

        return CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Получение решения по разбирательству: requestId={}, caseId={}", requestId, caseId);

            fraudDecisionService.solve(requestId, caseId,
                    new WebDecisionRequest(decisionRequest));

            log.info("Решение по разбирательству сохранено: requestId={}, caseId={}", requestId, caseId);

            var response = new DecisionResponse()
                    .status("SUCCESS")
                    .requestId(requestId);

            return ResponseEntity.ok(response);
        });
    }
}