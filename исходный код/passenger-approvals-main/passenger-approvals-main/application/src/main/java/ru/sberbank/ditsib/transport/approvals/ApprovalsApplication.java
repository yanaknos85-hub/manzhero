package ru.sberbank.ditsib.transport.approvals;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.sberbank.ditsib.transport.Microservice;
import ru.sberbank.ditsib.transport.approvals.database.dao.DelegateRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.Approval;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;
import static ru.sberbank.ditsib.transport.Microservice.MAIN_SECURITY_SCHEME;

/**
 * Entry point to the application.
 */
@Microservice
@OpenAPIDefinition(info = @Info(title = "Согласования",
                                description = "Операции по работе с согласованиями",
                                version = "${spring.application.version}"),
                   security = @SecurityRequirement(name = MAIN_SECURITY_SCHEME))
@EnableFeignClients
@EnableJpaRepositories(basePackageClasses = DelegateRepository.class)
@EntityScan(basePackageClasses = Approval.class)
@EnableTransactionManagement
public class ApprovalsApplication {
    
    /**
     * Start a new application instance.
     *
     * @param args arguments.
     */
    public static void main(String... args) {
        SpringApplication.run(ApprovalsApplication.class, args);
    }
    
}
