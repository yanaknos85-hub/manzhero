package ru.sber.transport.trip.registries.api.service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.sber.transport.trip.registries.api.dto.RegistryRequest;
import ru.sber.transport.trip.registries.api.dto.SetTokenDto;
import ru.sber.transport.trip.registries.api.exception.AuthorizationException;
import ru.sber.transport.trip.registries.api.exception.RequestException;
import ru.sber.transport.trip.registries.api.exception.ServerErrorException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestApiClient {

    @Value("${config.portal.baseurl:}") private String portalBasicUrl;
    @Value("${config.portal.post_registry:}") private String portalPathPostRegistry;
    @Value("${config.portal.act_number:}") private String portalActNumber;
    @Value("${config.portal.act_status:}") private String portalActStatus;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public SetTokenDto getCSRFToken() {
        try {
            HttpEntity<String> httpEntity = new HttpEntity<>(getHeadersCSRF());
            HttpMethod method = HttpMethod.GET;
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(portalBasicUrl);
            urlBuilder.append(portalPathPostRegistry);
            String url = urlBuilder.toString();
            log.debug("getHCRFToken: url is " + url);

            ResponseEntity<String> response = restTemplate.exchange(url, method, httpEntity,
                    new ParameterizedTypeReference<>() {
                    });
            validate(response);

            SetTokenDto setTokenDto = new SetTokenDto();
            setTokenDto.setCsrfTokenList(response.getHeaders().get("x-csrf-token"));
            setTokenDto.setCookieList(response.getHeaders().get("set-cookie"));
            return setTokenDto;
        } catch (Exception e) {
            log.error("getHCRFToken: unknown error", e);
        }
        return null;
    }

    public ResponseEntity<HttpStatus> sendRegistry(RegistryRequest registryRequest, String csrfToken, List<String> cookieList) {
        String json = "";
        try {
            json = objectMapper.writeValueAsString(registryRequest);
        } catch (JsonProcessingException e) {
            log.error("sendRegistry: error making json string", e);
            throw new RuntimeException("sendRegistry: error making json string");
        }
        log.debug("sendRegistry: input is '" + json + "'");
        HttpHeaders httpHeaders = getHeaders(csrfToken, cookieList);
        HttpEntity<String> httpEntity = new HttpEntity<>(json, httpHeaders);
        HttpMethod method = HttpMethod.POST;
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(portalBasicUrl);
        urlBuilder.append(portalPathPostRegistry);
        String url = urlBuilder.toString();
        log.debug("sendRegistry: httpHeaders is " + httpHeaders);
        log.debug("sendRegistry: url is " + url);

        ResponseEntity<HttpStatus> response = restTemplate.exchange(url, method, httpEntity,
                new ParameterizedTypeReference<>() {
                });
        return response;
    }

    public ResponseEntity<String> getActNumber(String registryId, String csrfToken, List<String> cookieList) {
        HttpEntity<String> httpEntity = new HttpEntity<>(getHeaders(csrfToken, cookieList));
        HttpMethod method = HttpMethod.GET;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(portalBasicUrl);
        urlBuilder.append(portalActNumber);
        urlBuilder.append(registryId);
        urlBuilder.append("/");
        String url = urlBuilder.toString();
        log.debug("getActNumber: request is: " + url);

        ResponseEntity<String> response = restTemplate.exchange(url, method, httpEntity,
                new ParameterizedTypeReference<>() {
                });

        return response;
    }

    public ResponseEntity<String> getActStatus(String actId, String csrfToken, List<String> cookieList) {
        HttpEntity<String> httpEntity = new HttpEntity<>(getHeaders(csrfToken, cookieList));
        HttpMethod method = HttpMethod.GET;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(portalBasicUrl);
        urlBuilder.append(portalActStatus);
        urlBuilder.append(actId);
        urlBuilder.append("/");
        String url = urlBuilder.toString();
        log.debug("getActStatus: request is: " + url);

        ResponseEntity<String> response = restTemplate.exchange(url, method, httpEntity,
                new ParameterizedTypeReference<>() {
                });

        return response;

    }

    private HttpHeaders getHeadersCSRF() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("X-CSRF-Token", "fetch");
        return httpHeaders;
    }

    private HttpHeaders getHeaders(String csrfToken, List<String> cookieList) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (csrfToken != null && csrfToken.length() > 0) {
            httpHeaders.add("X-CSRF-Token", csrfToken);
        }
        if (cookieList.size() > 0) {
            httpHeaders.add("Cookie", cookieList.stream().collect(Collectors.joining("; ")));
        }
        return httpHeaders;
    }

    private void validate(ResponseEntity<?> response) {
        log.info("Response: {}", response);
        if (response.getStatusCode().value() == 401) {
            throw new AuthorizationException("Authorization exception. " + response.getBody());
        }
        if (response.getStatusCode().is4xxClientError()) {
            throw new RequestException(response.getStatusCode() + " Error. Response: " + response.getBody());
        }
        if (response.getStatusCode().is5xxServerError()) {
            throw new ServerErrorException(response.getStatusCode() + " Error. Response: " + response.getBody());
        }
    }

}
