package ru.sber.transport.fraud.monitoring.application.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.sber.transport.fraud.monitoring.providers.*;
import ru.sber.transport.fraud.monitoring.providers.department.DepartmentsDatabaseProviderImpl;
import ru.sber.transport.fraud.monitoring.providers.employees.EmployeeDatabaseProviderImpl;
import ru.sber.transport.fraud.monitoring.providers.fraud.FraudsDatabaseDatabaseProviderImpl;
import ru.sber.transport.fraud.monitoring.providers.fraud_case.FraudCasesDataBaseProviderImpl;
import ru.sber.transport.fraud.monitoring.providers.fraud_case.FraudMessageItemDataBaseProviderImpl;
import ru.sber.transport.fraud.monitoring.providers.organization.OrganizationsDatabaseProviderImpl;
import ru.sber.transport.fraud.monitoring.providers.position.PositionsDatabaseProviderImpl;
import ru.sber.transport.fraud.monitoring.providers.trip_purpose.TripPurposesDatabaseProviderImpl;
import ru.sber.transport.fraud.monitoring.providers.trip_request.TripRequestsDatabaseDatabaseProviderImpl;
import ru.sber.transport.fraud.monitoring.providers.waypoint.WaypointsDatabaseDatabaseProviderImpl;

/**
 * Конфигурация приложения для работы с базой данных
 */
@Slf4j
@Configuration
public class Database {

    /**
     * Инициализация конфигурации базы данных
     */
    @PostConstruct
    public void init() {
        log.info("Configuring database");
    }

    /**
     * Провайдер организаций (БД)
     *
     * @param context контекст работы с базой данных
     * @return Провайдер организаций
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public OrganizationsDatabaseProvider organizationsDatabaseProvider(DSLContext context) {
        log.info("Creating organizations database provider");
        return new OrganizationsDatabaseProviderImpl() {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер подразделений (БД)
     *
     * @param context контекст работы с базой данных
     * @return Провайдер подразделений
     */
    @Primary
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public DepartmentsDatabaseProvider departmentsDatabaseProvider(DSLContext context) {
        log.info("Creating departments provider");
        return new DepartmentsDatabaseProviderImpl() {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер целей поездок (БД)
     *
     * @param context контекст работы с базой данных
     * @return Провайдер целей поездок
     */
    @Primary
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public TripPurposesDatabaseProvider tripPurposesProvider(DSLContext context) {
        log.info("Creating trip purposes database provider");
        return new TripPurposesDatabaseProviderImpl() {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер сотрудников
     *
     * @param context контекст работы с базой данных
     * @return Провайдер сотрудников
     */
    @Primary
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public EmployeesDatabaseProvider employeesDatabaseProvider(DSLContext context) {
        log.info("Creating employees database provider");
        return new EmployeeDatabaseProviderImpl() {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер должностей (БД)
     *
     * @param context контекст работы с базой данных
     * @return Провайдер должностей
     */
    @Primary
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public PositionsDatabaseProvider positionsDatabaseProvider(DSLContext context) {
        log.info("Creating positions provider");
        return new PositionsDatabaseProviderImpl() {
            @Override
            public DSLContext context() {
                return context;
            }

        };
    }

    /**
     * Провайдер заявок на поездки
     *
     * @param waypointsDatabaseProvider провайдер путевых точек
     * @param context                   контекст работы с базой данных
     * @return Провайдер заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public TripRequestsDatabaseProvider tripRequestsProvider(WaypointsDatabaseProvider waypointsDatabaseProvider, DSLContext context) {
        log.info("Creating trip requests provider");
        return new TripRequestsDatabaseDatabaseProviderImpl(waypointsDatabaseProvider) {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер путевых точек
     *
     * @param context контекст работы с базой данных
     * @return Провайдер путевых точек
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public WaypointsDatabaseProvider waypointsProvider(DSLContext context) {
        log.info("Creating waypoints provider");
        return new WaypointsDatabaseDatabaseProviderImpl() {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер нарушений (фрода)
     *
     * @param context контекст работы с базой данных
     * @return Провайдер нарушений (фрода)
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public FraudsDatabaseProvider fraudsProvider(DSLContext context) {
        log.info("Creating frauds provider");
        return new FraudsDatabaseDatabaseProviderImpl() {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер данных по случаям мошенничества (БД)
     *
     * @param context контекст работы с базой данных
     * @return Провайдер данных по случаям мошенничества
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public FraudCasesDataBaseProvider fraudCasesProvider(DSLContext context) {
        log.info("Creating fraud cases provider");
        return new FraudCasesDataBaseProviderImpl() {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер элементов сообщений мошенничества (БД)
     *
     * @param context контекст работы с базой данных
     * @return Провайдер элементов сообщений мошенничества
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public FraudMessageItemDataBaseProvider fraudMessageItemProvider(DSLContext context) {
        log.info("Создание провайдера сообщений о фроде");
        return new FraudMessageItemDataBaseProviderImpl() {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }
}
