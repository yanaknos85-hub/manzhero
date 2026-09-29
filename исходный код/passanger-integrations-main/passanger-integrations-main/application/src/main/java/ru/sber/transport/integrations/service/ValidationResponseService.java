package ru.sber.transport.integrations.service;

import org.springframework.http.ResponseEntity;

public interface ValidationResponseService {

    void validate(ResponseEntity<?> response);

}
