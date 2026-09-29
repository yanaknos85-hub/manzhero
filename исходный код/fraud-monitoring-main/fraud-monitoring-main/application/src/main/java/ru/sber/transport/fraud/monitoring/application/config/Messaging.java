package ru.sber.transport.fraud.monitoring.application.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sber.transport.fraud.monitoring.business.*;
import ru.sber.transport.fraud.monitoring.messaging.listeners.*;
import ru.sber.transport.fraud.monitoring.messaging.listeners.avro.DepartmentAvroListener;
import ru.sber.transport.fraud.monitoring.messaging.listeners.avro.EmployeeAvroListener;
import ru.sber.transport.fraud.monitoring.messaging.listeners.avro.ExternalTripRequestAvroListener;
import ru.sber.transport.fraud.monitoring.messaging.listeners.avro.OrganizationAvroListener;
import ru.sber.transport.fraud.monitoring.messaging.listeners.avro.PositionAvroListener;
import ru.sber.transport.fraud.monitoring.messaging.listeners.mapper.FraudCaseDataMapper;
import ru.sber.transport.fraud.monitoring.messaging.listeners.mapper.FraudMapper;
import ru.sber.transport.fraud.monitoring.messaging.listeners.mapper.TripRequestMapper;
import ru.sber.transport.fraud.monitoring.messaging.listeners.mapper.WaypointMapper;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.ExternalRequestMessage;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.FraudCaseDataMessage;
import ru.sber.transport.fraud.monitoring.messaging.listeners.message.FraudListenMessage;
import ru.sber.transport.fraud.monitoring.providers.FraudsDatabaseProvider;
import ru.sber.transport.fraud.monitoring.providers.TripRequestsDatabaseProvider;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.messaging.messages.DepartmentMessage;
import ru.sberbank.ditsib.transport.messaging.messages.EmployeeMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

import java.util.function.Consumer;

/**
 * Конфигурация приложения для обмена сообщениями между микросервисами
 */
@Slf4j
@Configuration
@NoAuthorize("/ws/requests")
public class Messaging {

    /**
     * Слушатель организаций
     *
     * @param organizationsService сервис организаций
     * @return Слушатель организаций
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "true")
    public Consumer<Message<ru.sber.transport.messages.corporate.avro.OrganizationMessage>> organizations(OrganizationsService organizationsService) {
        log.info("Creating organizations Avro listener");
        return new OrganizationAvroListener(organizationsService);
    }

    /**
     * Слушатель организаций
     *
     * @param organizationsService сервис организаций
     * @return Слушатель организаций
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<OrganizationMessage>> organizationInput(OrganizationsService organizationsService) {
        log.info("Creating organizations listener");
        return new OrganizationListener(organizationsService);
    }

    /**
     * Слушатель организаций
     *
     * @param organizationsService сервис организаций
     * @return Слушатель организаций
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<OrganizationMessage>> organizationInputSsl(OrganizationsService organizationsService) {
        log.info("Creating organizations Ssl listener");
        return new OrganizationListener(organizationsService);
    }

    /**
     * Слушатель департаментов
     *
     * @param departmentsService сервис департаментов
     * @return Слушатель департаментов
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "true")
    public Consumer<Message<ru.sber.transport.messages.corporate.avro.DepartmentMessage>> departments(DepartmentsService departmentsService) {
        log.info("Creating departments Avro listener");
        return new DepartmentAvroListener(departmentsService);
    }

    /**
     * Слушатель департаментов
     *
     * @param departmentsService сервис департаментов
     * @return Слушатель департаментов
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<DepartmentMessage>> departmentInput(DepartmentsService departmentsService) {
        log.info("Creating departments listener");
        return new DepartmentListener(departmentsService);
    }

    /**
     * Слушатель департаментов
     *
     * @param departmentsService сервис департаментов
     * @return Слушатель департаментов
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<DepartmentMessage>> departmentInputSsl(DepartmentsService departmentsService) {
        log.info("Creating departments Ssl listener");
        return new DepartmentListener(departmentsService);
    }

    /**
     * Слушатель сотрудников
     *
     * @param employeesService поставщик сотрудников
     * @return Слушатель сотрудников
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "true")
    public Consumer<Message<ru.sber.transport.messages.corporate.avro.EmployeeMessage>> employees(EmployeesService employeesService) {
        log.info("Creating employees Avro listener");
        return new EmployeeAvroListener(employeesService);
    }

    /**
     * Слушатель сотрудников
     *
     * @param employeesService поставщик сотрудников
     * @return Слушатель сотрудников
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<EmployeeMessage>> employeeInput(EmployeesService employeesService) {
        log.info("Creating employees listener");
        return new EmployeeListener(employeesService);
    }

    /**
     * Слушатель сотрудников
     *
     * @param employeesService поставщик сотрудников
     * @return Слушатель сотрудников
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<EmployeeMessage>> employeeInputSsl(EmployeesService employeesService) {
        log.info("Creating employees Ssl listener");
        return new EmployeeListener(employeesService);
    }

    /**
     * Слушатель должностей
     *
     * @param positionsService сервис должностей
     * @return слушатель должностей
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "true")
    public Consumer<Message<ru.sber.transport.messages.corporate.avro.PositionMessage>> positions(PositionsService positionsService) {
        log.info("Creating positions Avro listener");
        return new PositionAvroListener(positionsService);
    }

    /**
     * Слушатель должностей
     *
     * @param positionsService сервис должностей
     * @return слушатель должностей
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<PositionMessage>> positionInput(PositionsService positionsService) {
        log.info("Creating positions listener");
        return new PositionListener(positionsService);
    }

    /**
     * Слушатель должностей
     *
     * @param positionsService сервис должностей
     * @return слушатель должностей
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<PositionMessage>> positionInputSsl(PositionsService positionsService) {
        log.info("Creating positions SSL listener");
        return new PositionListener(positionsService);
    }

    /**
     * Слушатель внешних заявок на поездки
     *
     * @return Слушатель внешних заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "true")
    public Consumer<Message<ru.sber.transport.messages.request.external.avro.RequestMessage>> externalRequests(TripRequestsService tripRequestsService,
                                                                                                               TripRequestMapper tripRequestMapper,
                                                                                                               WaypointMapper waypointMapper) {
        log.info("Creating request external Avro listener");
        return new ExternalTripRequestAvroListener(tripRequestsService, tripRequestMapper, waypointMapper);
    }

    /**
     * Слушатель внешних заявок на поездки
     *
     * @return Слушатель внешних заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<ExternalRequestMessage>> externalRequestInput(TripRequestsService tripRequestsService,
                                                                          TripRequestMapper tripRequestMapper,
                                                                          WaypointMapper waypointMapper) {
        log.info("Creating request external listener");
        return new ExternalTripRequestListener(tripRequestsService, tripRequestMapper, waypointMapper);
    }

    /**
     * Слушатель внешних заявок на поездки
     *
     * @return Слушатель внешних заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<ExternalRequestMessage>> externalRequestInputSsl(TripRequestsService tripRequestsService,
                                                                             TripRequestMapper tripRequestMapper,
                                                                             WaypointMapper waypointMapper) {
        log.info("Creating request external Ssl listener");
        return new ExternalTripRequestListener(tripRequestsService, tripRequestMapper, waypointMapper);
    }

    /**
     * Слушатель заявок на поездки
     *
     * @return Слушатель заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    public Consumer<Message<RequestMessage>> requests(TripRequestsService tripRequestsService) {
        log.info("Creating trip request listener");
        return new TripRequestListener(tripRequestsService);
    }

    /**
     * Слушатель заявок на поездки
     *
     * @return Слушатель заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    public Consumer<Message<RequestMessage>> requestsSsl(TripRequestsService tripRequestsService) {
        log.info("Creating trip request SSL listener");
        return new TripRequestListener(tripRequestsService);
    }

    /**
     * Слушатель информации о фроде от сервиса мониторинга нарушений
     *
     * @return Слушатель информации о фроде
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    public Consumer<Message<FraudListenMessage>> fraudListenInput(
            FraudsDatabaseProvider fraudsDatabaseProvider,
            FraudMapper fraudMapper) {
        log.info("Creating fraud listen listener");
        return new FraudListenListener(fraudsDatabaseProvider, fraudMapper);
    }

    /**
     * Слушатель данных по случаям мошенничества
     *
     * @param fraudCaseDataService сервис данных по случаям мошенничества
     * @param fraudCaseDataMapper  маппер данных по случаям мошенничества
     * @return Слушатель данных по случаям мошенничества
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    public Consumer<Message<FraudCaseDataMessage>> fraudCaseDataInput(
            FraudCaseDataService fraudCaseDataService, FraudCaseDataMapper fraudCaseDataMapper) {
        log.info("Creating fraud case data listener");
        return new FraudCaseDataListener(fraudCaseDataService, fraudCaseDataMapper);
    }

    /**
     * Слушатель данных по случаям мошенничества
     *
     * @param fraudCaseDataService сервис данных по случаям мошенничества
     * @param fraudCaseDataMapper  маппер данных по случаям мошенничества
     * @return Слушатель данных по случаям мошенничества
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    public Consumer<Message<FraudCaseDataMessage>> fraudCaseDataInputSsl(
            FraudCaseDataService fraudCaseDataService, FraudCaseDataMapper fraudCaseDataMapper) {
        log.info("Creating fraud case data SSL listener");
        return new FraudCaseDataListener(fraudCaseDataService, fraudCaseDataMapper);
    }
}
