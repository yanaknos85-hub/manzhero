package ru.sber.transport.corporate.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.corporate.messaging.senders.OrganizationSender;
import ru.sber.transport.corporate.messaging.senders.mappers.OrganizationMessageMapper;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RequiredArgsConstructor
@Component
class OrganizationSenderImpl implements OrganizationSender {

    @SuppressWarnings("Autowired")
    @Qualifier("organizationsOutput")
    private final ObjectProvider<OutputBridge> organizationsOutputBridge;

    @SuppressWarnings("Autowired")
    @Qualifier("organizationsOutputSsl")
    private final ObjectProvider<OutputBridge> organizationsOutputSslBridge;

    @SuppressWarnings("Autowired")
    @Qualifier("organizationsOutputAvro")
    private final ObjectProvider<OutputBridge> organizationsOutputAvroBridge;

    private final OrganizationMessageMapper mapper;

    @Override
    public Future<Void> send(Organization source) {
        try (var executor = Executors.newSingleThreadExecutor()) {
            return executor.submit(() -> {
                var headers = new HashMap<String, Object>();
                var syncId = source.getSyncId();
                if (syncId != null) {
                    headers.put("syncId", syncId);
                }
                var data = mapper.toMessage(source);
                organizationsOutputBridge.ifAvailable(ob -> ob.send(data, headers));
                organizationsOutputSslBridge.ifAvailable(ob -> ob.send(data, headers));
                organizationsOutputAvroBridge.ifAvailable(ob -> ob.send(mapper.toMessageAvro(source), headers));
                return null;
            });
        }
    }

}
