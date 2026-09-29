package ru.sber.transport.grpc.test;

import io.grpc.Server;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * Сервер GRPC.
 */
@RequiredArgsConstructor
public class ServerCleanupTarget implements CleanupTarget {
    
    private final Server server;
    
    @Override
    public void shutdown() {
        server.shutdownNow();
    }
    
    @Override
    public boolean awaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return server.awaitTermination(timeout, timeUnit);
    }
    
    @Override
    public boolean isTerminated() {
        return server.isTerminated();
    }
}
