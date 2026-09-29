package ru.sber.transport.utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

public class MockExecutor extends AbstractExecutorService {

    @Override
    public void shutdown() {
    }

    @NotNull
    @Override
    public List<Runnable> shutdownNow() {
        return List.of();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, @NotNull TimeUnit unit) {
        return false;
    }

    @Override
    public void execute(@NotNull Runnable command) {
        command.run();
    }
}
