package ru.sber.transport.integrations.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.sber.transport.integrations.exception.AuthorizationException;
import ru.sber.transport.integrations.exception.RequestException;
import ru.sber.transport.integrations.exception.ServerErrorException;
import ru.sber.transport.integrations.service.ValidationResponseService;

@Service
public class ValidationResponseServiceImpl implements ValidationResponseService {
    
    @Override
    public void validate(ResponseEntity<?> response) {
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
