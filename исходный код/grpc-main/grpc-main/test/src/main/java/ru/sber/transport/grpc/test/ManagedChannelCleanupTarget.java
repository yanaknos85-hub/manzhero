package ru.sber.transport.grpc.test;

import io.grpc.ManagedChannel;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * Канал передачи данных.
 */
@RequiredArgsConstructor
public class ManagedChannelCleanupTarget implements CleanupTarget {
    
    private final ManagedChannel channel;
    
    @Override
    public void shutdown() {
        channel.shutdown();
    }
    
    @Override
    public boolean awaitTermination(long timeout, TimeUnit timeUnit) throws InterruptedException {
        return channel.awaitTermination(timeout, timeUnit);
    }
    
    @Override
    public boolean isTerminated() {
        return channel.isTerminated();
    }
}
