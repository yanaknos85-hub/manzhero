package ru.sber.transport.integrations.service;

import ru.sber.transport.integrations.dto.Token;

import java.net.URI;

public interface CredentialService {
    
    void add(URI url, String login, Token token);
    
    Token get(URI url, String login);
}
