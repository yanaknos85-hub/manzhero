package ru.sber.transport.integrations.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.integrations.dto.Token;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Проверка сохранения токена в каждой комбинации хост/логин")
class CredentialServiceImplTest {
    
    @Test
    @DisplayName("Одинаковый host, разный логин")
    void sameHostDiffLogins() {
        var cut = new CredentialServiceImpl();
        URI sameURI = URI.create("http://www.same.host.ru");
        var login1 = "login1";
        var login2 = "login2";
        var token1 = new Token("token1", null, null, null, null);
        var token2 = new Token("token2", null, null, null, null);
        cut.add(sameURI, login1, token1);
        cut.add(sameURI, login2, token2);
        
        var result = cut.get(sameURI, login1);
        assertEquals("token1",
                     result.token(),
                     "Даже в случае одинакового host'а для каждого логина должен сохраняться/возвращаться токен для данного логина");
        
        result = cut.get(sameURI, login2);
        assertEquals("token2",
                     result.token(),
                     "Даже в случае одинакового host'а для каждого логина должен сохраняться/возвращаться токен для данного логина");
    }
    
    @Test
    @DisplayName("Одинаковый логин, разный host")
    void sameLoginsDiffHosts() {
        var cut = new CredentialServiceImpl();
        var uri1 = URI.create("http://www.same.host1.ru");
        var uri2 = URI.create("http://www.same.host2.ru");
        var sameLogin = "sameLogin";
        var token1 = new Token("token1", null, null, null, null);
        var token2 = new Token("token2", null, null, null, null);
        cut.add(uri1, sameLogin, token1);
        cut.add(uri2, sameLogin, token2);
        
        var result = cut.get(uri1, sameLogin);
        assertEquals("token1",
                     result.token(),
                     "Даже в случае одинакового логина, для разных хостов должен сохраняться/возвращаться свой токен");
        
        result = cut.get(uri2, sameLogin);
        assertEquals("token2",
                     result.token(),
                     "Даже в случае одинакового логина, для разных хостов должен сохраняться/возвращаться свой токен");
    }
}