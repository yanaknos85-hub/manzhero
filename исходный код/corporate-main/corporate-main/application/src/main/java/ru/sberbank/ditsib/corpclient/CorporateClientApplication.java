package ru.sberbank.ditsib.corpclient;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sber.transport.files.grpc.annotation.FileExchange;
import ru.sber.transport.files.grpc.service.DeleteServiceGrpc;
import ru.sber.transport.files.grpc.service.DownloadServiceGrpc;
import ru.sber.transport.files.grpc.service.UploadServiceGrpc;
import ru.sberbank.ditsib.transport.Microservice;

import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Corporate client application
 */
@Microservice
@OpenAPIDefinition(info = @Info(title = "Корпоративные клиенты",
                                description = "Операции по работе с корпоративными клиентами",
                                version = "${spring.application.version}"),
                   security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
@EnableJpaRepositories(
        basePackages = { "ru.sberbank.ditsib.corpclient", "ru.sber.transport.humanreadableid", "ru.sber.transport" })
@EntityScan(basePackages = { "ru.sberbank.ditsib.corpclient", "ru.sber.transport.humanreadableid", "ru.sber.transport" })
@ComponentScan(basePackages = { "ru.sber.transport.corporate", "ru.sberbank.ditsib.corpclient", "ru.sber.transport.humanreadableid", "ru.sber.transport.files", "ru.sber.transport" })
@EnableAsync
@EnableTransactionManagement
@EnableDiscoveryClient
@FileExchange
@GrpcClientBean(client = @GrpcClient("files"), clazz = DeleteServiceGrpc.DeleteServiceStub.class)
@GrpcClientBean(client = @GrpcClient("files"), clazz = DownloadServiceGrpc.DownloadServiceStub.class)
@GrpcClientBean(client = @GrpcClient("files"), clazz = UploadServiceGrpc.UploadServiceStub.class)
public class CorporateClientApplication {
    
    /**
     * Start a new application instance.
     *
     * @param args arguments.
     */
    public static void main(String... args) {
        SpringApplication.run(CorporateClientApplication.class, args);
    }

}