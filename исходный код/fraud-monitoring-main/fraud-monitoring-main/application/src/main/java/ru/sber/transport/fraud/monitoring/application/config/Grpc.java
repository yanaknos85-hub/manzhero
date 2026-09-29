package ru.sber.transport.fraud.monitoring.application.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.corporate.grpc.service.*;
import ru.sber.transport.fraud.monitoring.providers.DepartmentsGrpcProvider;
import ru.sber.transport.fraud.monitoring.providers.EmployeesGrpcProvider;
import ru.sber.transport.fraud.monitoring.providers.OrganizationsGrpcProvider;
import ru.sber.transport.fraud.monitoring.providers.TripPurposesGrpcProvider;
import ru.sber.transport.fraud.monitoring.providers.grpc.*;

/**
 * Конфигурация gRPC клиентов
 */
@Slf4j
@Configuration
public class Grpc {

    static final String GRPC_EMPLOYEES = "grpcEmployees";

    static final String GRPC_DEPARTMENTS = "grpcDepartments";

    static final String GRPC_ORGANIZATIONS = "grpcOrganizations";

    static final String GRPC_TRIP_PURPOSES = "grpcTripPurposes";

    static final String GRPC_POSITIONS = "grpcPositions";

    /**
     * Инициализация конфигурации gRPC
     */
    @PostConstruct
    public void init() {
        log.info("Configuring gRPC application");
    }

    /**
     * GRPC-провайдер организаций
     *
     * @param stub gRPC-клиент
     * @return gRPC-провайдер организаций
     */
    @Bean(name = GRPC_ORGANIZATIONS, bootstrap = Bean.Bootstrap.DEFAULT)
    public OrganizationsGrpcProvider grpcOrganizations(OrganizationsGrpc.OrganizationsBlockingStub stub) {
        log.info("Creating organizations grpc provider");
        return new OrganizationGrpcProviderImpl(stub);
    }

    /**
     * GRPC-провайдер департаментов
     *
     * @param stub gRPC-клиент
     * @return gRPC-провайдер департаментов
     */
    @Bean(name = GRPC_DEPARTMENTS, bootstrap = Bean.Bootstrap.DEFAULT)
    public DepartmentsGrpcProvider grpcDepartments(DepartmentsGrpc.DepartmentsBlockingStub stub) {
        log.info("Creating departments grpc provider");
        return new DepartmentsGrpcProviderImpl(stub);
    }

    /**
     * GRPC-провайдер сотрудников
     *
     * @param stub gRPC-клиент
     * @return gRPC-провайдер сотрудников
     */
    @Bean(name = GRPC_EMPLOYEES, bootstrap = Bean.Bootstrap.DEFAULT)
    public EmployeesGrpcProvider grpcEmployees(EmployeesGrpc.EmployeesBlockingStub stub) {
        log.info("Creating employees grpc provider");
        return new EmployeesGrpcProviderImpl(stub);
    }

    /**
     * GRPC-провайдер целей поездок
     *
     * @param stub gRPC-клиент
     * @return gRPC-провайдер целей поездок
     */
    @Bean(name = GRPC_TRIP_PURPOSES, bootstrap = Bean.Bootstrap.DEFAULT)
    public TripPurposesGrpcProvider grpcTripPurposes(TripPurposesGrpc.TripPurposesBlockingStub stub) {
        log.info("Creating trip purposes grpc provider");
        return new TripPurposesGrpcProviderImpl(stub);
    }

    /**
     * GRPC-провайдер должностей
     *
     * @param stub gRPC-клиент
     * @return gRPC-провайдер должностей
     */
    @Bean(name = GRPC_POSITIONS, bootstrap = Bean.Bootstrap.DEFAULT)
    public PositionsGrpcProviderImpl grpcPositions(PositionsGrpc.PositionsBlockingStub stub) {
        log.info("Creating positions grpc provider");
        return new PositionsGrpcProviderImpl(stub);
    }

}
