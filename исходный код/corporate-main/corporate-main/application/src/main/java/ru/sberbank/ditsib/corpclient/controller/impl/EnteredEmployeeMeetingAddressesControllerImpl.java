package ru.sberbank.ditsib.corpclient.controller.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sberbank.ditsib.corpclient.controller.EnteredEmployeeMeetingAddressesController;
import ru.sberbank.ditsib.corpclient.controller.ForwardRequest;
import ru.sberbank.ditsib.corpclient.dto.NewMeetingAddressesDTO;

import java.util.UUID;

/**
 * Implementation of controller of entered employees meeting addresses.
 */
@RestController
@RequiredArgsConstructor
public class EnteredEmployeeMeetingAddressesControllerImpl
        implements EnteredEmployeeMeetingAddressesController, ForwardRequest {

    @Getter
    private final RestTemplate restTemplate;

    private final EmployeeOrganizationFunction organizationFunction;

    @Setter
    @Value("${addresses.host}")
    private String addressesHost;

    @Override
    public ResponseEntity<byte[]> addAddress(HttpServletRequest request, @Valid NewMeetingAddressesDTO newData) {
        return forwardRequest(request, "%s/meeting".formatted(addressesHost), newData);
    }

    @Override
    public ResponseEntity<byte[]> editAddress(UUID id, HttpServletRequest request, @Valid NewMeetingAddressesDTO newData) {
        return forwardRequest(request, "%s/meeting/%s".formatted(addressesHost, id), newData);
    }

    @Override
    public ResponseEntity<byte[]> deleteAddress(UUID id, HttpServletRequest request) {
        return forwardRequest(request, "%s/meeting/%s".formatted(addressesHost, id));
    }

    @Override
    public ResponseEntity<byte[]> getAll(HttpServletRequest request, JwtAuthenticationToken jwtAuthenticationToken) {
        var organizationId = organizationFunction.apply(UUID.fromString(jwtAuthenticationToken.getToken().getId()));
        return forwardRequest(request, "%s/meeting/%s".formatted(addressesHost, organizationId));
    }

}
