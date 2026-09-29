package ru.sber.transport.authorization.fake.controller;

import org.springframework.web.bind.annotation.*;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;

@RestController
@RequestMapping("/sub")
@NoAuthorize
public class SubController {

    @PutMapping("/login/token/")
    public String authorizeToken() {
        return "response";
    }

    @DeleteMapping
    public String authorizeSudir() {
        return "Response";
    }

    @PatchMapping
    public String patchSudir() {
        return "Response";
    }

    @GetMapping("")
    public String getSudir() {
        return "Response";
    }
    
}
