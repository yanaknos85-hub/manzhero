package ru.sberbank.ditsib.corpclient.controller.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ru.sberbank.ditsib.corpclient.controller.EnteredEmployeeAddressesController;
import ru.sberbank.ditsib.corpclient.controller.ForwardRequest;

/**
 * Implementation of entered employees addresses controller.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
class EnteredEmployeeAddressesControllerImpl implements EnteredEmployeeAddressesController, ForwardRequest {

    @Getter
    private final RestTemplate restTemplate;

    @Setter
    @Value("${addresses.host}")
    private String addressesHost;

    @Override
    public ResponseEntity<byte[]> getAll(HttpServletRequest request) {
        return forwardRequest(request, "%s/self".formatted(addressesHost));
    }
}
