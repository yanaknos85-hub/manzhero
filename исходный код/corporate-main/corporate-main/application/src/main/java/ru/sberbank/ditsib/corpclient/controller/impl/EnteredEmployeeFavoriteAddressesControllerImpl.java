package ru.sberbank.ditsib.corpclient.controller.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ru.sberbank.ditsib.corpclient.controller.EnteredEmployeeFavoriteAddressesController;
import ru.sberbank.ditsib.corpclient.controller.ForwardRequest;
import ru.sberbank.ditsib.corpclient.dto.NewAddressDto;

import java.util.UUID;

/**
 * Implementation of controller of entered employees addresses.
 */
@RestController
@RequiredArgsConstructor
class EnteredEmployeeFavoriteAddressesControllerImpl
        implements EnteredEmployeeFavoriteAddressesController, ForwardRequest {

    @Getter
    private final RestTemplate restTemplate;

    @Setter
    @Value("${addresses.host}")
    private String addressesHost;

    @Override
    public ResponseEntity<byte[]> addAddress(HttpServletRequest request, NewAddressDto newData) {
        return forwardRequest(request, "%s/favorite".formatted(addressesHost), newData);
    }

    @Override
    public ResponseEntity<byte[]> editAddress(UUID id, HttpServletRequest request, NewAddressDto newData) {
        return forwardRequest(request, "%s/favorite/%s".formatted(addressesHost, id), newData);
    }

    @Override
    public ResponseEntity<byte[]> deleteAddress(UUID id, HttpServletRequest request) {
        return forwardRequest(request, "%s/favorite/%s".formatted(addressesHost, id));
    }

    @Override
    public ResponseEntity<byte[]> get(UUID id, HttpServletRequest request) {
        return forwardRequest(request, "%s/favorite/%s".formatted(addressesHost, id));
    }

    @Override
    public ResponseEntity<byte[]> getAll(HttpServletRequest request) {
        return forwardRequest(request, "%s/favorite".formatted(addressesHost));
    }
}
