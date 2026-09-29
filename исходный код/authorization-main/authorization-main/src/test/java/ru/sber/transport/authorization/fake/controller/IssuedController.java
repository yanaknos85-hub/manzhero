package ru.sber.transport.authorization.fake.controller;

import org.springframework.web.bind.annotation.*;

@RestController
public class IssuedController implements IIssuedController {

    public String post() {
        return "response";
    }

    public String put() {
        return "Response";
    }

    public String delete() {
        return "Response";
    }

    public String get() {
        return "Response";
    }

    public String request() {
        return "Response";
    }

    @Override
    public String values() {
        return "values";
    }
}
