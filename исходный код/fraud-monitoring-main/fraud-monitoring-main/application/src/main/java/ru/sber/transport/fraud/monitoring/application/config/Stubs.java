package ru.sber.transport.fraud.monitoring.application.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.corporate.grpc.service.*;

/**
 * Конфигурация gRPC клиентов
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "grpc.enabled", havingValue = "true", matchIfMissing = true)
@GrpcClientBean(clazz = OrganizationsGrpc.OrganizationsBlockingStub.class, client = @GrpcClient("corporate"))
@GrpcClientBean(clazz = DepartmentsGrpc.DepartmentsBlockingStub.class, client = @GrpcClient("corporate"))
@GrpcClientBean(clazz = EmployeesGrpc.EmployeesBlockingStub.class, client = @GrpcClient("corporate"))
@GrpcClientBean(clazz = TripPurposesGrpc.TripPurposesBlockingStub.class, client = @GrpcClient("corporate"))
@GrpcClientBean(clazz = PositionsGrpc.PositionsBlockingStub.class, client = @GrpcClient("corporate"))
public class Stubs {

    /**
     * Инициализация конфигурации gRPC
     */
    @PostConstruct
    public void init() {
        log.info("Configuring gRPC stubs");
    }

}
