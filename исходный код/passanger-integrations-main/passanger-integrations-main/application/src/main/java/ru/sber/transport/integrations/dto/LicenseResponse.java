package ru.sber.transport.integrations.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * LicenseResponse
 */

@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
public class LicenseResponse {
    private String number;
    private String type;
    
    @Override
    public String toString() {
        return "class LicenseResponse {\n" +
               "    number: " + toIndentedString(number) + "\n" +
               "    type: " + toIndentedString(type) + "\n" +
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

