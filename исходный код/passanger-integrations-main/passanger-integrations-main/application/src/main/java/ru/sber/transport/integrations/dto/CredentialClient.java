package ru.sber.transport.integrations.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;

/**
 * CredentialClient
 */

@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
public class CredentialClient {
    private URI uri;
    private String login;
    private String password;
    
    @Override
    public String toString() {
        return "class CredentialClient {\n" +
               "    uri: " + toIndentedString(uri) + "\n" +
               "    login: " + toIndentedString(login) + "\n" +
               "}";
    }
    
    /**
     * Convert the given object to string with each line indented by 4 spaces (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

