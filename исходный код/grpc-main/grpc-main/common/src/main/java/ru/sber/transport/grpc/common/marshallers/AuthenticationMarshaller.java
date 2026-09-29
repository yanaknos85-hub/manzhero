package ru.sber.transport.grpc.common.marshallers;

import io.grpc.Metadata;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Маршаллер данных аутентификации.
 */
public class AuthenticationMarshaller implements Metadata.AsciiMarshaller<Authentication> {

    @Override
    public String toAsciiString(Authentication value) {
        var name = value.getName();
        var authorities = value.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(";"));
        return name + "|" + authorities;
    }

    @SuppressWarnings("java:S3958")
    @Override
    public Authentication parseAsciiString(String ciphered) {
        if (ciphered.contains("|")) {
            var parts = ciphered.split("\\|");
            if (parts.length > 2) {
                throw new IllegalArgumentException("Illegal authority header");
            }
            var name = parts[0];
            var authorities = parts.length == 2 ? List.of(parts[1].split(";")) : List.<String>of();
            var granted = authorities.stream().map(SimpleGrantedAuthority::new).toList();
            return new UsernamePasswordAuthenticationToken(name, name, granted);
        }
        return new UsernamePasswordAuthenticationToken("system","system", List.of());
    }
}
