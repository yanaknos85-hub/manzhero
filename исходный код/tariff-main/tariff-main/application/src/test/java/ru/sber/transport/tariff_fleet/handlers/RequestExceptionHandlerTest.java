package ru.sber.transport.tariff_fleet.handlers;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.LoggingExtension;
import ru.sber.transport.tariff_fleet.dto.AbstractContractPostDto;
import ru.sber.transport.tariff_fleet.exception.ContractAlreadyExistsException;
import ru.sber.transport.tariff_fleet.handler.RequestExceptionHandler;
import ru.sber.transport.tariff_fleet.service.ContractService;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sber.transport.tariff_fleet.TestData.*;
import static ru.sber.transport.tariff_fleet.constant.Role.ROLE_ADMIN_CORP_CLIENT;

@DisplayName("Обработка rest исключений")
@SpringBootTest(properties = { "logging.level.ru.sber.transport.tariff_fleet=DEBUG",
                               "spring.servlet.multipart.max-file-size=1MB"})
@EmbeddedPostgres
@AutoConfigureMockMvc
class RequestExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private ContractService contractService;
    @RegisterExtension
    private static final LoggingExtension LOGGING_EXTENSION = new LoggingExtension(RequestExceptionHandler.class);

    @SneakyThrows
    @Test
    void handleBusinessException() {
        var request = String.format("""
                        {
                            "contractorOrganizationId": "cb9f17e7-f658-43ca-a70f-40c1c93ad0a6",
                            "inspectionType": "MEDIC",
                            "amount": 9999999999,
                            "edfOperatorId": "2AL",
                            "edfCode": "QtYxWfGYISewoO",
                            "contractorMedicalLicense": {
                                "series": "xvkMlZqCHggHqNBBh",
                                "number": "BNajVxsxdnskNxEFCsSz",
                                "issueDate": "%s",
                                "expiryDate": "%s"
                            },
                            "documentType": "EWB",
                            "number": "oQwNBfTJRBFN",
                            "uvhd": "qLuUfYIREgipFDHDZPcdjSwdHgtuvX",
                            "period": {
                                "start": "%s",
                                "end": "%s"
                            }
                        }
                      """, LocalDate.now(), LocalDate.now().plusYears(1), LocalDate.now(), LocalDate.now().plusYears(1));
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        doThrow(new ContractAlreadyExistsException(CONTRACTOR_1_ID, CONTRACT_NUMBER))
                .when(contractService).create(any(AbstractContractPostDto.class));
        mockMvc.perform(post("/contracts")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
               .andExpect(status().isBadRequest())
               .andExpect(result -> assertInstanceOf(ContractAlreadyExistsException.class, result.getResolvedException()))
               .andExpect(result -> assertEquals(String.format(ContractAlreadyExistsException.MSG_FORMAT_1, CONTRACTOR_1_ID, CONTRACT_NUMBER),
                                                 Optional.ofNullable(result.getResolvedException()).orElseThrow().getMessage()));
        assertEquals(2, LOGGING_EXTENSION.getEvents().size());
        var firstEvent = LOGGING_EXTENSION.getEvents().get(0);
        var secondEvent = LOGGING_EXTENSION.getEvents().get(1);
        assertEquals(RequestExceptionHandler.class.getName(), firstEvent.getLoggerName());
        assertEquals(String.format(ContractAlreadyExistsException.MSG_FORMAT_1, CONTRACTOR_1_ID, CONTRACT_NUMBER), firstEvent.getFormattedMessage());
        assertEquals(Level.INFO, firstEvent.getLevel());
        assertEquals(RequestExceptionHandler.class.getName(), secondEvent.getLoggerName());
        assertEquals(String.format(ContractAlreadyExistsException.MSG_FORMAT_1, CONTRACTOR_1_ID, CONTRACT_NUMBER), secondEvent.getFormattedMessage());
        assertEquals(Level.DEBUG, secondEvent.getLevel());
        assertTrue(secondEvent.getThrowableProxy().getStackTraceElementProxyArray().length > 10);
    }
}
