package ru.sber.transport.limits.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.sber.transport.limits.providers.*;
import ru.sber.transport.limits.providers.database.departments.DepartmentsImpl;
import ru.sber.transport.limits.providers.database.employees.EmployeesImpl;
import ru.sber.transport.limits.providers.database.services.ServicesImpl;
import ru.sber.transport.limits.providers.database.sharings.PeriodSharingsImpl;
import ru.sber.transport.limits.providers.database.spendings.SpendingsImpl;
import ru.sber.transport.limits.providers.database.types.TypesImpl;

/**
 * Конфигурация провайдеров для работы с базой данных
 */
@Slf4j
@Configuration
public class Database {

    @PostConstruct
    void init() {
        log.info("Starting database components");
    }

    /**
     * Создание провайдера видов услуг
     *
     * @param ctx объект для работы с базой данных
     * @return провайдер видов услуг
     */
    @Primary
    @Bean
    public Services servicesProvider(DSLContext ctx) {
        log.info("Starting database services provider");
        return new ServicesImpl() {
            @Override
            public DSLContext context() {
                return ctx;
            }
        };
    }

    /**
     * Создание провайдера трат
     *
     * @param ctx объект для работы с базой данных
     * @return провайдер трат
     */
    @Primary
    @Bean
    public Spendings spendingsProvider(DSLContext ctx) {
        return new SpendingsImpl() {

            @Override
            public DSLContext context() {
                return ctx;
            }

        };
    }

    /**
     * Создание провайдера типов услуг
     *
     * @param ctx объект для работы с базой данных
     * @return провайдер типов услуг
     */
    @Primary
    @Bean
    public Types typesProvider(DSLContext ctx) {
        log.info("Starting database types provider");
        return new TypesImpl() {

            @Override
            public DSLContext context() {
                return ctx;
            }

        };
    }

    /**
     * Создание провайдера департаментов
     *
     * @param ctx                     объект для работы с базой данных
     * @param departmentsGrpcProvider провайдер департаментов из gRPC
     * @return провайдер департаментов
     */
    @Primary
    @Bean
    public Departments departmentsProvider(DSLContext ctx, Departments departmentsGrpcProvider) {
        log.info("Starting database departments provider");
        return new DepartmentsImpl(departmentsGrpcProvider) {
            @Override
            public DSLContext context() {
                return ctx;
            }
        };
    }

    /**
     * Создание провайдера распределения трат департаментам
     *
     * @param ctx         объект для работы с базой данных
     * @param departments провайдер департаментов
     * @return провайдер распределения трат департаментам
     */
    @Primary
    @Bean
    public PeriodSharings periodSharingsProvider(DSLContext ctx, Departments departments) {
        log.info("Starting database period sharings provider");
        return new PeriodSharingsImpl(departments) {

            @Override
            public DSLContext context() {
                return ctx;
            }
        };
    }

    /**
     * Создание провайдера сотрудников
     *
     * @param employeesGrpcProvider провайдер сотрудников через gRPC
     * @param ctx                   объект для работы с базой данных
     * @return провайдер сотрудников
     */
    @Primary
    @Bean
    public Employees employeesProvider(Employees employeesGrpcProvider, DSLContext ctx) {
        return new EmployeesImpl(employeesGrpcProvider) {
            @Override
            public DSLContext context() {
                return ctx;
            }
        };
    }
}
