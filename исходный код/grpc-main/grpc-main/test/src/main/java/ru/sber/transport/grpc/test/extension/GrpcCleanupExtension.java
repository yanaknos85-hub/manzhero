package ru.sber.transport.grpc.test.extension;

import io.grpc.BindableService;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import ru.sber.transport.grpc.test.CleanupTarget;
import ru.sber.transport.grpc.test.ManagedChannelCleanupTarget;
import ru.sber.transport.grpc.test.ServerCleanupTarget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Очистка GRPC-соединений.
 */
@Slf4j
@Setter
public class GrpcCleanupExtension implements AfterEachCallback {

    @Getter(AccessLevel.PACKAGE)
    private final List<CleanupTarget> cleanupTargets = new ArrayList<>();
    
    private long maxTerminationAttempts = 100;
    
    private long timeout = 100;
    
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    /**
     * Добавить сервис.
     *
     * @param service сервис.
     * @return канал сообщений.
     * @throws IOException ошибка создания канала.
     */
    public ManagedChannel addService(BindableService service) throws IOException {
        var serverName = InProcessServerBuilder.generateName();
        
        var server = InProcessServerBuilder.forName(serverName)
                                           .directExecutor()
                                           .addService(service)
                                           .build()
                                           .start();
        
        var channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
        
        cleanupTargets.add(new ServerCleanupTarget(server));
        cleanupTargets.add(new ManagedChannelCleanupTarget(channel));
        
        return channel;
    }
    
    @Override
    public void afterEach(ExtensionContext extensionContext) {
        cleanupTargets.forEach(this::cleanupTarget);
        
        if (cleanupTargets.stream().allMatch(CleanupTarget::isTerminated)) {
            cleanupTargets.clear();
        } else {
            log.error("Not all targets are terminated");
        }
    }

    @SneakyThrows(InterruptedException.class)
    private void cleanupTarget(CleanupTarget target) {
        var count = 0;
        target.shutdown();
        do {
            target.awaitTermination(timeout, timeUnit);
            count++;
            if (count > maxTerminationAttempts) {
                log.error("Shutdown target %s is failed".formatted(target));
                break;
            }
        } while (!target.isTerminated());
    }
}
