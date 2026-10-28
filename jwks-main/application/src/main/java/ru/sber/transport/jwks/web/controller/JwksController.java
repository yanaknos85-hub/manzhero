package ru.sber.transport.jwks.web.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping
public interface JwksController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> getPublicKeys();

}
