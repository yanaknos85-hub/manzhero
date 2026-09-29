package ru.sber.transport.fraud.monitoring.application.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.sber.transport.files.grpc.annotation.FileExchange;
import ru.sber.transport.fraud.monitoring.business.*;
import ru.sber.transport.fraud.monitoring.business.impl.*;
import ru.sber.transport.fraud.monitoring.providers.*;

import javax.sql.DataSource;
import java.time.Clock;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Конфигурация веб-приложения
 */
@Slf4j
@Configuration
@FileExchange
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class App {
    /**
     * Инициализация конфигурации веб-приложения
     */
    @PostConstruct
    public void init() {
        log.info("Configuring web application");
    }

    /**
     * Фоновый загрузчик бинов
     *
     * @return Фоновый загрузчик бинов
     */
    @Bean
    public Executor bootstrapExecutor() {
        log.info("Creating bootstrap executor");
        return Executors.newCachedThreadPool();
    }

    /**
     * Бин времени
     *
     * @return Время
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Clock clock() {
        final var clock = Clock.systemDefaultZone();
        log.info("Time zone configured to {}", clock.getZone());
        return clock;
    }

    /**
     * Провайдер блокировок
     *
     * @param dataSource связь с БД
     * @return Провайдер блокировок
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public LockProvider lockProvider(@Value("${spring.application.name}") String appName, DataSource dataSource) {
        log.info("Creating lock provider");
        return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .withTableName("\"%s\".\"shedlock\"".formatted(appName))
                .build());
    }

    /**
     * Бизнес-логика работы с организациями
     *
     * @param organizationsDatabaseProvider провайдер организаций (БД)
     * @param organizationsGrpcProvider     провайдер организаций (Grpc)
     * @return Сервис работы с организациями
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public OrganizationsService organizationsService(
            OrganizationsDatabaseProvider organizationsDatabaseProvider,
            OrganizationsGrpcProvider organizationsGrpcProvider) {
        log.info("Creating organizations service");
        return new OrganizationsServiceImpl(organizationsDatabaseProvider, organizationsGrpcProvider);
    }

    /**
     * Бизнес-логика работы с подразделениями
     *
     * @param departmentsDatabaseProvider провайдер подразделений (БД)
     * @param departmentsGrpcProvider     провайдер подразделений (Grpc)
     * @param employeesService            сервис работы с сотрудниками
     * @return Сервис работы с подразделениями
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public DepartmentsService departmentsService(
            DepartmentsDatabaseProvider departmentsDatabaseProvider,
            DepartmentsGrpcProvider departmentsGrpcProvider,
            EmployeesService employeesService) {
        log.info("Creating departments service");
        return new DepartmentsServiceImpl(departmentsDatabaseProvider, departmentsGrpcProvider, employeesService);
    }

    /**
     * Бизнес-логика работы с должностями
     *
     * @param positionsDatabaseProvider провайдер должностей (БД)
     * @param positionsGrpcProvider     провайдер должностей (Grpc)
     * @return Сервис работы с должностями
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public PositionsService positionsService(
            PositionsDatabaseProvider positionsDatabaseProvider,
            PositionsGrpcProvider positionsGrpcProvider) {
        log.info("Creating positions service");
        return new PositionsServiceImpl(positionsDatabaseProvider, positionsGrpcProvider);
    }

    /**
     * Бизнес-логика работы с целями поездок
     *
     * @param tripPurposesDatabaseProvider провайдер целей поездок (БД)
     * @param tripPurposesGrpcProvider     провайдер целей поездок (Grpc)
     * @return Сервис работы с целями поездок
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public TripPurposesService tripPurposesService(
            TripPurposesDatabaseProvider tripPurposesDatabaseProvider,
            TripPurposesGrpcProvider tripPurposesGrpcProvider) {
        log.info("Creating trip purposes service");
        return new TripPurposesServiceImpl(tripPurposesDatabaseProvider, tripPurposesGrpcProvider);
    }

    /**
     * Бизнес-логика работы с сотрудниками
     *
     * @param employeesDatabaseProvider провайдер сотрудников (БД)
     * @param employeesGrpcProvider     провайдер сотрудников (Grpc)
     * @param positionsService          сервис работы с должностями
     * @return Сервис работы с сотрудниками
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public EmployeesService employeesService(
            EmployeesDatabaseProvider employeesDatabaseProvider,
            EmployeesGrpcProvider employeesGrpcProvider,
            PositionsService positionsService) {
        log.info("Creating employees service");
        return new EmployeesServiceImpl(employeesDatabaseProvider, employeesGrpcProvider, positionsService);
    }

    /**
     * Бизнес-логика работы с заявками на поездки
     *
     * @param tripRequestsDatabaseProvider провайдер заявок на поездки
     * @param tripPurposesService          сервис работы с целями поездок
     * @param employeesService             сервис работы с сотрудниками
     * @param departmentsService           сервис работы с подразделениями
     * @return Сервис работы с заявками на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public TripRequestsService tripRequestsService(
            TripRequestsDatabaseProvider tripRequestsDatabaseProvider,
            TripPurposesService tripPurposesService,
            EmployeesService employeesService,
            DepartmentsService departmentsService) {
        log.info("Creating trip requests service");
        return new TripRequestsServiceImpl(tripRequestsDatabaseProvider, tripPurposesService, employeesService, departmentsService);
    }

    /**
     * Бизнес-логика работы с данными по случаям мошенничества
     *
     * @param fraudCaseDatabaseProvider       провайдер случаев мошенничества (БД)
     * @param fraudMessageItemDataBaseProvider провайдер элементов сообщений мошенничества (БД)
     * @return Сервис работы с данными по случаям мошенничества
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public FraudCaseDataService fraudCaseDataService(
            FraudCasesDataBaseProvider fraudCaseDatabaseProvider,
            FraudMessageItemDataBaseProvider fraudMessageItemDataBaseProvider) {
        log.info("Creating fraud case data service");
        return new FraudCaseDataServiceImpl(fraudCaseDatabaseProvider, fraudMessageItemDataBaseProvider);
    }

    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public FraudDecisionService fraudDecisionService(
            FraudCasesDataBaseProvider fraudCasesDataBaseProvider) {
        log.info("Creating fraud decision service");
        return new FraudDecisionServiceImpl(fraudCasesDataBaseProvider);
    }

}
