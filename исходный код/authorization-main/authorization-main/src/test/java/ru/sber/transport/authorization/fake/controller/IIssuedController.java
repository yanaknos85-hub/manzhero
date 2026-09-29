package ru.sber.transport.authorization.fake.controller;

import org.springframework.web.bind.annotation.*;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;

@RequestMapping("issued")
public interface IIssuedController {

    @NoAuthorize
    @PostMapping(value = "method")
    String post();

    @NoAuthorize
    @PutMapping(value = "method")
    String put();

    @NoAuthorize
    @DeleteMapping(value = "method")
    String delete();

    @NoAuthorize
    @GetMapping(value = "method")
    String get();

    @NoAuthorize
    @RequestMapping(value = "method", method = RequestMethod.PATCH)
    String request();

    @RequestMapping(value = "method/{mthd}/value/{value}", method = RequestMethod.GET)
    String values();
}
