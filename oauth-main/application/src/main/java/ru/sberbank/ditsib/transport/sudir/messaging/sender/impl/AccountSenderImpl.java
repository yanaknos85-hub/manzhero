package ru.sberbank.ditsib.transport.sudir.messaging.sender.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;
import ru.sberbank.ditsib.transport.sudir.messaging.sender.AccountSender;
import ru.sberbank.ditsib.transport.sudir.messaging.sender.mapper.AccountScimMessageMapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class AccountSenderImpl implements AccountSender {

    @Qualifier("accountsOutput")
    private final ObjectProvider<OutputBridge> accountsOutput;

    @Qualifier("accountsOutputSsl")
    private final ObjectProvider<OutputBridge> accountsOutputSsl;

    private final AccountScimMessageMapper mapper;

    @Override
    public void send(Account account) {
        final var message = mapper.toMessage(account, null);
        accountsOutput.ifAvailable(ob -> ob.send(message));
        accountsOutputSsl.ifAvailable(ob -> ob.send(message));
    }

    @Override
    public void sendDeleted(Collection<UUID> accounts) {
        accounts.stream()
                .map(mapper::toDeleted)
                .forEach(m -> {
                    accountsOutput.ifAvailable(ob -> ob.send(m));
                    accountsOutputSsl.ifAvailable(ob -> ob.send(m));
                });
    }

    @Override
    public void send(Account account, LocalDateTime loggedIn) {
        var message = mapper.toMessage(account, loggedIn);
        accountsOutput.ifAvailable(ob -> ob.send(message));
        accountsOutputSsl.ifAvailable(ob -> ob.send(message));
    }
}
