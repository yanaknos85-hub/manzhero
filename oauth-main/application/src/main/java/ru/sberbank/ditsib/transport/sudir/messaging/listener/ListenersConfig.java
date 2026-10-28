package ru.sberbank.ditsib.transport.sudir.messaging.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.binder.kafka.BinderHeaderMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.DefaultKafkaHeaderMapper;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import ru.sber.transport.contractor.messages.EmployeeRoleMessage;
import ru.sber.transport.roles.messages.RoleMessage;
import ru.sber.transport.scim.messages.AccountRoleLinkMessage;
import ru.sberbank.ditsib.transport.messaging.messages.UserMessage;
import ru.sberbank.ditsib.transport.sudir.business.Accounts;
import ru.sberbank.ditsib.transport.sudir.messaging.mappers.AccountMessageMapper;
import ru.sberbank.ditsib.transport.sudir.messaging.providers.RoleProvider;
import ru.sberbank.ditsib.transport.sudir.providers.account.dao.AccountRepository;
import ru.sberbank.ditsib.transport.sudir.providers.role.dao.RoleRepository;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Slf4j
@Configuration
class ListenersConfig {

    @Bean
    Consumer<Message<UserMessage>> usersInput(Accounts accounts, AccountMessageMapper mapper) {
        return usersInputSsl(accounts, mapper);
    }

    @Bean
    Consumer<Message<RoleMessage>> rolesInput(RoleProvider roleProvider) {
        return rolesInputSsl(roleProvider);
    }

    @Bean
    Consumer<Message<AccountRoleLinkMessage>> accountsRolesInput(Accounts cases) {
        return accountsRolesInputSsl(cases);
    }

    @Bean
    Consumer<Message<UserMessage>> usersInputSsl(Accounts accounts, AccountMessageMapper mapper) {
        return message -> {
            var userMessage = message.getPayload();
            if (userMessage.orgStructureType().equals("INTERNAL")) {
                accounts.save(mapper.toBusiness(userMessage));
            }
        };
    }

    @Bean
    Consumer<Message<EmployeeRoleMessage>> employeeRoleInput(AccountRepository accountRepository, RoleRepository roleRepository) {
        return employeeRoleInputSsl(accountRepository, roleRepository);
    }

    @Bean
    Consumer<Message<EmployeeRoleMessage>> employeeRoleInputSsl(AccountRepository accountRepository, RoleRepository roleRepository) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var accountOpt = accountRepository.findById(message.id());
            var roleOpt = roleRepository.findById(message.role());
            if (accountOpt.isPresent() && roleOpt.isPresent()) {
               roleRepository.addRole(accountOpt.get(), roleOpt.get());
            }
        };
    }

    @Bean
    Consumer<Message<RoleMessage>> rolesInputSsl(RoleProvider roleProvider) {
        return message -> {
            var headers = message.getHeaders();
            var payload = message.getPayload();
            var code = String.valueOf(headers.getOrDefault(KafkaHeaders.RECEIVED_KEY, payload.code()));

            var deleted = payload.deleted();

            if (deleted) {
                roleProvider.delete(code);
            } else if (payload.exclusives().contains("INTERNAL")) {
                roleProvider.save(payload);
            }
        };
    }

    @Bean
    Consumer<Message<AccountRoleLinkMessage>> accountsRolesInputSsl(Accounts cases) {
        return rawMessage -> {
            var message = rawMessage.getPayload();
            var id = rawMessage.getHeaders().get(KafkaHeaders.RECEIVED_KEY, String.class);

            if (id != null && Pattern.matches("[a-f\\d]{8}(-[a-f\\d]{4}){3}-[a-f\\d]{12}", id)) {
                cases.activation(UUID.fromString(id), message.getPersonnelNumber(), message.getRoles(), message.isActive());
            } else {
                log.warn("Received id is not valid uuid: %s".formatted(id));
            }
        };
    }

    private String getType(Map<?, ?> headers) {
        var headerValue = headers.get("type");
        if (headerValue instanceof DefaultKafkaHeaderMapper.NonTrustedHeaderType nonTrusted) {
            return new String(nonTrusted.getHeaderValue(), StandardCharsets.UTF_8).replace("\"", "");
        } else if (headerValue instanceof BinderHeaderMapper.NonTrustedHeaderType nonTrusted) {
            return new String(nonTrusted.getHeaderValue(), StandardCharsets.UTF_8).replace("\"", "");
        } else {
            return String.valueOf(headerValue);
        }
    }
}
