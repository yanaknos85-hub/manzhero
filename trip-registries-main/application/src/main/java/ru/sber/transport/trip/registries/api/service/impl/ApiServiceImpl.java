package ru.sber.transport.trip.registries.api.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import ru.sber.transport.trip.registries.api.dto.ActNumberResponse;
import ru.sber.transport.trip.registries.api.dto.ActStatusResponse;
import ru.sber.transport.trip.registries.api.dto.RegistryRequest;
import ru.sber.transport.trip.registries.api.dto.SetTokenDto;
import ru.sber.transport.trip.registries.api.service.ApiService;
import ru.sber.transport.trip.registries.api.service.client.RestApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiServiceImpl implements ApiService {

    private final ObjectMapper objectMapper;
    private final RestApiClient restApiClient;

    private String csrfToken = "";
    private List<String> cookieList = new ArrayList<>();

    public HttpStatusCode sendRegistry(RegistryRequest registryRequest) {
        var response = trySendOrUpdateToken(() -> restApiClient.sendRegistry(registryRequest, csrfToken, cookieList));

        if (response != null) {
            log.debug("sendRegistry: response.getBody = {}", response.getBody());
            return response.getStatusCode();
        } else {
            log.debug("sendRegistry: response = null");
            return null;
        }
    }

    @Override
    public ActNumberResponse getActNumber(UUID registryId) {
        var response = trySendOrUpdateToken(() ->
                restApiClient.getActNumber(registryId.toString().toUpperCase(Locale.ROOT), csrfToken, cookieList));

        if (response != null && StringUtils.hasText(response.getBody())) {
            log.debug("getActNumber: response is {}", response.getBody());
            ActNumberResponse actNumberResponse = null;
            try {
                actNumberResponse = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            } catch (Exception e) {
                log.error("getActNumber: cannot read value from response!", e);
            }
            return actNumberResponse;
        } else {
            return null;
        }
    }

    @Override
    public ActStatusResponse getActStatus(String actId) {
        var response = trySendOrUpdateToken(() -> restApiClient.getActStatus(actId, csrfToken, cookieList));

        if (response != null && StringUtils.hasText(response.getBody())) {
            log.debug("getActStatus: response is {}", response.getBody());
            ActStatusResponse actStatusResponse = null;
            try {
                actStatusResponse = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            } catch (Exception e) {
                log.error("getActStatus: cannot read value from response!", e);
            }
            return actStatusResponse;
        } else {
            return null;
        }
    }

    private <T> ResponseEntity<T> trySendOrUpdateToken(Supplier<ResponseEntity<T>> operation) {
        ResponseEntity<T> response = null;
        try {
            response = operation.get();
        } catch (HttpClientErrorException e) {
            log.warn("HttpClientErrorException error.", e);
            if (HttpStatus.FORBIDDEN.equals(e.getStatusCode())) {
                log.info("Trying to update token");
                setCsrfAndCookie();
                response = operation.get();
            }
        }

        return response;
    }

    private void setCsrfAndCookie() {
        SetTokenDto setTokenDto = restApiClient.getCSRFToken();
        log.info("setCsrfAndCookie(): tokens recieved '{}'", setTokenDto);
        csrfToken = setTokenDto.getCsrfTokenList().stream().findFirst().orElse("empty");
        cookieList = setTokenDto.getCookieList();
    }
}
