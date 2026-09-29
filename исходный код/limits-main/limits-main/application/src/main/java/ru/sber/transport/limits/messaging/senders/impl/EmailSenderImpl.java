package ru.sber.transport.limits.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.sber.transport.limits.messaging.senders.EmailSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.transport.messaging.messages.EmailMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@RequiredArgsConstructor
@Component
@Slf4j
public class EmailSenderImpl implements EmailSender {

    @Qualifier("emailOutputBridge")
    private final ObjectProvider<OutputBridge> emailOutput;

    @Qualifier("emailOutputSslBridge")
    private final ObjectProvider<OutputBridge> emailOutputSsl;

    @Setter
    @Value("${email.limit.ends.subject:Истечение лимита}")
    private String subject;

    @Setter
    @Value("${email.limit.ends.template:email/limit-ends.html}")
    private String templatePath;

    @Override
    public List<Future<Void>> send(Collection<String> emails, String departmentId, String limitId, int percent) {
        if (emails.isEmpty()) {
            return null;
        }
        final var email = EmailMessage.builder()
                .emails(emails)
                .subject(subject)
                .data(Map.of("percent", percent, "departmentId", departmentId, "limitId", limitId))
                .html(true)
                .template(readTemplate())
                .build();
        final var futures = new ArrayList<Future<Void>>();
        emailOutput.ifAvailable(it -> futures.add(it.send(email)));
        emailOutputSsl.ifAvailable(it -> futures.add(it.send(email)));
        return futures;
    }

    @SneakyThrows(IOException.class)
    private String readTemplate() {
        return Files.readString(Path.of(templatePath));
    }

}
