package ru.sber.transport.limits.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import ru.sber.transport.limits.business.Reserves;
import ru.sber.transport.limits.grpc.service.LimitServiceGrpc;
import ru.sber.transport.limits.web.grpc.reservation.LimitReservation;

/**
 * Конфигурация веб-компонентов
 */
@Slf4j
@Configuration
public class Web {

    @PostConstruct
    void init() {
        log.info("Starting web components");
    }

    /**
     * Создание сервера для работы с остатками
     *
     * @param reservesBusiness   бизнес-логика по работе с остатками
     * @param transactionManager менеджер транзакций
     * @return сервер для работы с остатками
     */
    @GrpcService
    public LimitServiceGrpc.LimitServiceImplBase reserveGrpcServer(Reserves reservesBusiness, PlatformTransactionManager transactionManager) {
        log.info("Starting grpc server of reservations");
        return new LimitReservation(reservesBusiness, transactionManager);
    }
}
