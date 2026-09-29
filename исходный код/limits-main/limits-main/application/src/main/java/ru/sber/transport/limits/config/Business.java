package ru.sber.transport.limits.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.limits.business.Reserves;
import ru.sber.transport.limits.business.impl.ReservesImpl;
import ru.sber.transport.limits.providers.*;

/**
 * Конфигурация бизнес-логики
 */
@Slf4j
@Configuration
public class Business {

    @PostConstruct
    void init() {
        log.info("Starting business components");
    }

    /**
     * Создание бизнес-логики по работе с остатками
     *
     * @param spendingsProvier      провайдер трат
     * @param employeesProvider     провайдер сотрудников
     * @param servicesProvier       провайдер услуг
     * @param typesProvier          провайдер типов
     * @param periodSharingsProvier провайдер остатков по периодам
     * @return бизнес-логика по работе с резервами
     */
    @Bean
    public Reserves reservesBusiness(Spendings spendingsProvier, Employees employeesProvider, Services servicesProvier, Types typesProvier, PeriodSharings periodSharingsProvier) {
        log.info("Starting business layer of reservations");
        return new ReservesImpl(spendingsProvier, employeesProvider, servicesProvier, typesProvier, periodSharingsProvier);
    }
}
