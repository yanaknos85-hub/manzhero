package ru.sber.transport.jwks.web.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.jwks.web.controller.JwksController;
import ru.sber.transport.jwks.web.providers.JwksProvider;

import java.util.Map;

@RequiredArgsConstructor
@RestController
class JwksControllerImpl implements JwksController {

    private final JwksProvider jwksProvider;

    @Override
    public Map<String, Object> getPublicKeys() {
        return jwksProvider.get().toPublicJWKSet().toJSONObject();
    }

}
