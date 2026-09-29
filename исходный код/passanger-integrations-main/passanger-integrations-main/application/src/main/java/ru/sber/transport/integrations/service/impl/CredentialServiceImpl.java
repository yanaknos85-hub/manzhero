package ru.sber.transport.integrations.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sber.transport.integrations.dto.Token;
import ru.sber.transport.integrations.service.CredentialService;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CredentialServiceImpl implements CredentialService {
    
    private final Map<String, Map<String, Token>> credentialMap = new ConcurrentHashMap<>();
    
    @Override
    public void add(URI url, String login, Token token) {
        credentialMap.computeIfAbsent(url.getHost(), host -> new ConcurrentHashMap<>());
        credentialMap.computeIfPresent(url.getHost(),
                                       (host, credentialsByHost) -> {
                                           credentialsByHost.put(login, token);
                                           return credentialsByHost;
                                       });
    }
    
    @Override
    public Token get(URI url, String login) {
        if (url.getHost() == null || login == null) {
            return null;
        }
        var token = Optional.ofNullable(credentialMap.get(url.getHost()))
                            .map(credentialsByHost -> credentialsByHost.get(login));
        
        logGetToken(url, login, token);
        
        return token.orElse(null);
    }
    
    private static void logGetToken(URI url, String login, Optional<Token> token) {
        if (log.isDebugEnabled()) {
            String lastFiveCharsOFToken = token.map(Token::token)
                                               .map(t -> t.substring(Math.max(0, t.length() - 5)))
                                               .orElse("null (токен не был найден)");
            log.debug("Запрос получения токена по url/login. url: {}, login: {}. Последние 5 символов токена: {}", url, login, lastFiveCharsOFToken);
        }
    }
}
