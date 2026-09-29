package ru.sber.transport.authorization.fake.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.authorization.annotations.SkipConsentCheck;

@SkipConsentCheck("/controllerSkip")
@RestController
public class SkipUrlController implements ISkipUrlController {

    @Override
    public String controllerSkip() {
        return "alive";
    }
}
