package ru.sberbank.ditsib.corpclient.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Forward requests.
 */
@Deprecated
public interface ForwardRequest {

    /**
     * Forward request.
     *
     * @param request request to forward.
     * @param addressesHost target url.
     * @return response.
     */
    default @NotNull ResponseEntity<byte[]> forwardRequest(HttpServletRequest request, String addressesHost) {
        var headers = new HttpHeaders();
        appendHeaders(request, headers);
        var entity = new HttpEntity<>(headers);
        LoggerFactory.getLogger(getClass()).info("Forward request %s %s to %s".formatted(request.getMethod(), request.getRequestURI(), addressesHost));
        var response = getRestTemplate().exchange(addressesHost, HttpMethod.valueOf(request.getMethod()), entity, byte[].class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    /**
     * Forward request.
     *
     * @param request request to forward.
     * @param addressesHost target url.
     * @param data data to send.
     * @return response.
     */
    default @NotNull ResponseEntity<byte[]> forwardRequest(HttpServletRequest request, String addressesHost, Object data) {
        var headers = new HttpHeaders();
        appendHeaders(request, headers);
        var entity = new HttpEntity<>(data, headers);
        LoggerFactory.getLogger(getClass()).info("Forward request %s to %s".formatted(request.getRequestURI(), addressesHost));
        var response = getRestTemplate().exchange(addressesHost, HttpMethod.valueOf(request.getMethod()), entity, byte[].class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    private void appendHeaders(HttpServletRequest request, HttpHeaders headers) {
        var authValue = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!authValue.matches("Bearer [-_a-zA-Z0-9]*\\.[-_a-zA-Z0-9]*\\.[-_a-zA-Z0-9]*")) {
            throw HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Wrong headers", new HttpHeaders(), null, null);
        }
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(authValue.split(" ")[1]));
    }

    RestTemplate getRestTemplate();

}
