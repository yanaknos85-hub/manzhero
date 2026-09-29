package ru.sber.transport.fraud.monitoring.application.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.fraud.monitoring.business.FraudDecisionService;
import ru.sber.transport.fraud.monitoring.business.TripRequestsService;
import ru.sber.transport.fraud.monitoring.web.query.FraudDecisionApiImpl;
import ru.sber.transport.fraud.monitoring.web.query.FraudMonitoringQueryApiImpl;
import ru.sber.transport.web.api.FraudDecisionApi;
import ru.sber.transport.web.api.FraudMonitoringQueryApi;

/**
 * Веб-конфигурация приложения
 */
@Slf4j
@Configuration
public class Web {

    /**
     * Апи мониторинга фродовых заявок
     *
     * @return делегат заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public FraudMonitoringQueryApi queryApi(TripRequestsService tripRequestsService) {
        log.info("Creating request query");
        return new FraudMonitoringQueryApiImpl(tripRequestsService);
    }

    /**
     * Апи принятия решений по разбирательствам фрода
     *
     * @return делегат принятия решений
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public FraudDecisionApi decisionApi(FraudDecisionService fraudDecisionService) {
        log.info("Creating fraud decision api");
        return new FraudDecisionApiImpl(fraudDecisionService);
    }

}
