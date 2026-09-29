package ru.sber.transport.authorization.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.sber.transport.authorization.service.BlackListService;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Реализация черного списка токенов.
 */
@EnableScheduling
@RequiredArgsConstructor
@Component
public class BlackListServiceImpl implements BlackListService {
    
    private final Set<String> tokens = new CopyOnWriteArraySet<>();
    
    private final Set<String> tokensPendingCleaning = new HashSet<>();
    
    @Override
    public void add(String token) {
        if (token != null) {
            tokens.add(token);
        }
    }
    
    @Override
    public boolean check(String token) {
        return tokens.contains(token);
    }
    
    @Scheduled(cron = "${security.blacklist.jwt.cleanup.cron:0 */15 * * * *}")
    void cleanupTokens() {
        tokens.removeAll(tokensPendingCleaning);
        tokensPendingCleaning.clear();
        tokensPendingCleaning.addAll(tokens);
    }
}
