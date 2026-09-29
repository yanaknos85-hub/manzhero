package ru.sber.transport.authorization.fake.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class ForeignController {

    @GetMapping("/foreign/controller/")
    public String authorizeToken() {
        return "response";
    }

}
