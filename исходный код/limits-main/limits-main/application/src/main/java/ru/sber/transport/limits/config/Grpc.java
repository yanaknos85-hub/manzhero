package ru.sber.transport.limits.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.corporate.grpc.service.DepartmentsGrpc;
import ru.sber.transport.corporate.grpc.service.EmployeesGrpc;
import ru.sber.transport.limits.providers.Departments;
import ru.sber.transport.limits.providers.Employees;
import ru.sber.transport.limits.providers.grpc.department.DepartmentGrpcImpl;
import ru.sber.transport.limits.providers.grpc.employee.EmployeeGrpcImpl;

/**
 * Конфигурация gRPC-провайдеров
 */
@Slf4j
@Configuration
@GrpcClientBean(clazz = EmployeesGrpc.EmployeesBlockingStub.class, beanName = "employeesGrpcStub", client = @GrpcClient("corporate"))
@GrpcClientBean(clazz = DepartmentsGrpc.DepartmentsBlockingStub.class, beanName = "departmentsGrpcStub", client = @GrpcClient("corporate"))
public class Grpc {

    @PostConstruct
    void init() {
        log.info("Starting grpc providers");
    }

    /**
     * Создание провайдера сотрудников через gRPC
     *
     * @param employeesGrpcStub обертка для клиента gRPC
     * @return провайдер сотрудников через gRPC
     */
    @Bean
    public Employees employeesGrpcProvider(EmployeesGrpc.EmployeesBlockingStub employeesGrpcStub) {
        log.info("Starting grpc employees provider");
        return new EmployeeGrpcImpl(employeesGrpcStub);
    }

    /**
     * Создание провайдера подразделений через gRPC
     *
     * @param departmentsGrpcStub обертка для клиента gRPC
     * @return провайдер подразделений через gRPC
     */
    @Bean
    public Departments departmentsGrpcProvider(DepartmentsGrpc.DepartmentsBlockingStub departmentsGrpcStub) {
        log.info("Starting grpc departments provider");
        return new DepartmentGrpcImpl(departmentsGrpcStub);
    }

}
