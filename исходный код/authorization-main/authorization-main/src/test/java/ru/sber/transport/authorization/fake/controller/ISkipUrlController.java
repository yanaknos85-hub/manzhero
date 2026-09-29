package ru.sber.transport.authorization.fake.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;

@RequestMapping
public interface ISkipUrlController {

    @NoAuthorize
    @GetMapping("/controllerSkip/")
    String controllerSkip();
}
