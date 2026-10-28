package ru.sber.transport.trip.registries;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.sber.transport.database.trip_registries.tables.records.RegistryRecord;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.trip.registries.api.dto.ActNumberResponse;
import ru.sber.transport.trip.registries.api.dto.ActStatusResponse;
import ru.sber.transport.trip.registries.api.dto.RegistryRequest;
import ru.sber.transport.trip.registries.api.dto.SetTokenDto;
import ru.sber.transport.trip.registries.api.service.ApiService;
import ru.sber.transport.trip.registries.api.service.client.RestApiClient;
import ru.sber.transport.trip.registries.database.dao.RegistryRepository;
import ru.sber.transport.trip.registries.database.model.ActNumberError;
import ru.sber.transport.trip.registries.database.model.RegistryState;
import ru.sber.transport.trip.registries.service.RegistryProcessor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = RegistriesApplication.class)
@EmbeddedPostgres
@AutoConfigureMockMvc
@Transactional
@Slf4j
@DisplayName("Базовые тесты")
class BasicTest {

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private RegistryRepository registryRepository;

    @Autowired
    private RegistryProcessor registryProcessor;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiService apiService;

    @Autowired
    private RestApiClient restApiClient;

    public static final String REGISTRY_ID = "f10bcc5b-51db-4e1c-a747-2a229604f974";
    public static final String ACT_ID = "00001111";

    @BeforeEach
    public void init() {
    }

    @Test
    @DisplayName("Занесение реестра")
    void postRegistryTest() {
        when(restTemplate.exchange(contains("/zmms_act_transp"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(HttpStatusCode.valueOf(200)));

        RegistryRequest registryRequest = new RegistryRequest();
        registryRequest.setRegistryId(REGISTRY_ID);
        registryRequest.setOrganizationId("001");
        registryRequest.setContractIdUVHD("XYUD000");
        registryRequest.setStartDate("2023-01-22");
        registryRequest.setEndDate("2023-01-22");

        final RegistryRecord registry = registryRepository.getById(UUID.fromString(REGISTRY_ID));
        assertNull(registry);

        // call sendRegistry
        HttpStatusCode statusCode = apiService.sendRegistry(registryRequest);
        assertEquals(statusCode, HttpStatusCode.valueOf(200));
    }

    @Test
    @DisplayName("Получение CSRF token")
    void getCsrfTokenTest() {
        ResponseEntity responseEntity = ResponseEntity.ok()
                .header("x-csrf-token", "csrftoken")
                .header("set-cookie", "cookie")
                .build();
        when(restTemplate.exchange(contains("/zmms_act_transp"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(responseEntity);

        // call sendRegistry
        SetTokenDto setTokenDto1 = restApiClient.getCSRFToken();
        assertEquals(setTokenDto1.getCsrfTokenList().get(0), "csrftoken");
    }

    @Test
    @DisplayName("Процессинг реестра")
    void processRegistriesTest() {
        RegistryRecord registry = new RegistryRecord();
        registry.setRegistryId(UUID.fromString(REGISTRY_ID));
        registry.setOrganizationId("001");
        registry.setContractId("XYUD000");
        registry.setStartDate("2023-01-22");
        registry.setEndDate("2023-01-22");
        registry.setRegistryState(RegistryState.REGISTRY_SENT_SUCCESS.name());
        registryRepository.save(registry);
        // make mock on RestTemplate for apiService.getActNumber call
        ActNumberResponse actNumberResponse = new ActNumberResponse();
        actNumberResponse.setRegistryId(UUID.fromString(REGISTRY_ID));
        actNumberResponse.setActId(ACT_ID);
        String actNumberResponseString = null;
        try {
            actNumberResponseString = objectMapper.writeValueAsString(actNumberResponse);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("value to string failed");
        }
        when(restTemplate.exchange(argThat((String url) -> url != null && url.contains("/sapasus/zmms_act_transp/act")
                        && url.matches(".*/sapasus/zmms_act_transp/act/.+")),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(actNumberResponseString));

        // make mock on RestTemplate for apiService.getActNumber call
        ActStatusResponse actStatusResponse = new ActStatusResponse();
        actStatusResponse.setActId(ACT_ID);
        actStatusResponse.setLockingIndicator("3");
        actStatusResponse.setUpdStatus("60");
        String actStatusResponseString = null;
        try {
            actStatusResponseString = objectMapper.writeValueAsString(actStatusResponse);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("value to string failed");
        }

        var tokenResponse = ResponseEntity.ok()
                .header("x-csrf-token", "csrftoken")
                .header("set-cookie", "cookie")
                .build();

        when(restTemplate.exchange(argThat((String url) -> url != null && url.contains("/sapasus/zmms_act_transp/act")
                        && !url.matches(".*/sapasus/zmms_act_transp/act/.+")),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(tokenResponse);

        when(restTemplate.exchange(contains("/zmms_act/upd"),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN))
                .thenReturn(ResponseEntity.ok(actStatusResponseString));

        registry = registryRepository.getById(UUID.fromString(REGISTRY_ID));
        assertEquals(RegistryState.REGISTRY_SENT_SUCCESS.name(), registry.getRegistryState());
        // call RegistryProcessor
        registryProcessor.processRegistries();
        // check new status
        registry = registryRepository.getById(UUID.fromString(REGISTRY_ID));
        assertEquals(ACT_ID, registry.getActId());
        assertEquals(RegistryState.ACT_NUMBER_RECIEVED_SUCCESS.name(), registry.getRegistryState());

        // call RegistryProcessor
        registryProcessor.processRegistries();
        // check new status
        registry = registryRepository.getById(UUID.fromString(REGISTRY_ID));
        assertEquals(ACT_ID, registry.getActId());
        assertEquals(RegistryState.REGISTRY_PROCESSED_FINISHED.name(), registry.getRegistryState());
    }

    @Test
    @DisplayName("Процессинг реестра негативный кейс по статусу")
    void processRegistriesNegative1Test() {
        RegistryRecord registry = new RegistryRecord();
        registry.setRegistryId(UUID.fromString(REGISTRY_ID));
        registry.setOrganizationId("001");
        registry.setContractId("XYUD000");
        registry.setStartDate("2023-01-22");
        registry.setEndDate("2023-01-22");
        registry.setRegistryState(RegistryState.REGISTRY_SENT_SUCCESS.name());
        registryRepository.save(registry);
        // make mock on RestTemplate for apiService.getActNumber call
        ActNumberResponse actNumberResponse = new ActNumberResponse();
        actNumberResponse.setRegistryId(UUID.fromString(REGISTRY_ID));
        actNumberResponse.setActId(ACT_ID);
        actNumberResponse.setErrorType(ActNumberError.T002.name());
        String actNumberResponseString = null;
        try {
            actNumberResponseString = objectMapper.writeValueAsString(actNumberResponse);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("value to string failed");
        }
        when(restTemplate.exchange(contains("/zmms_act_transp"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(actNumberResponseString));
        registry = registryRepository.getById(UUID.fromString(REGISTRY_ID));
        assertEquals(RegistryState.REGISTRY_SENT_SUCCESS.name(), registry.getRegistryState());
        // call RegistryProcessor
        registryProcessor.processRegistries();
        // check new status
        registry = registryRepository.getById(UUID.fromString(REGISTRY_ID));
        assertEquals(RegistryState.ACT_NUMBER_RECIEVING_FAILED.name(), registry.getRegistryState());
    }

    @Test
    @DisplayName("Процессинг реестра негативный кейс по апдейту")
    void processRegistriesNegative2Test() {
        RegistryRecord registry = new RegistryRecord();
        registry.setRegistryId(UUID.fromString(REGISTRY_ID));
        registry.setOrganizationId("001");
        registry.setContractId("XYUD000");
        registry.setStartDate("2023-01-22");
        registry.setEndDate("2023-01-22");
        registry.setRegistryState(RegistryState.ACT_NUMBER_RECIEVED_SUCCESS.name());
        registryRepository.save(registry);
        // make mock on RestTemplate for apiService.getActNumber call
        ActStatusResponse actStatusResponse = new ActStatusResponse();
        actStatusResponse.setActId(ACT_ID);
        actStatusResponse.setLockingIndicator("3");
        actStatusResponse.setUpdStatus("97");
        String actStatusResponseString = null;
        try {
            actStatusResponseString = objectMapper.writeValueAsString(actStatusResponse);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("value to string failed");
        }
        when(restTemplate.exchange(contains("/zmms_act/upd"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(ResponseEntity.ok(actStatusResponseString));

        registry = registryRepository.getById(UUID.fromString(REGISTRY_ID));
        assertEquals(RegistryState.ACT_NUMBER_RECIEVED_SUCCESS.name(), registry.getRegistryState());

        // call RegistryProcessor
        registryProcessor.processRegistries();
        // check new status
        registry = registryRepository.getById(UUID.fromString(REGISTRY_ID));
        //assertEquals(RegistryState.REGISTRY_PROCESSING_FAILED, registry.getRegistryState());
        assertEquals(RegistryState.ACT_NUMBER_RECIEVED_SUCCESS.name(), registry.getRegistryState());
    }
}