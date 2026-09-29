package ru.sber.transport.integrations.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * OptionsTariffResponse
 */

@AllArgsConstructor
@EqualsAndHashCode
public class OptionsTariffResponse {
    private String name;
    private String title;
    private Double price;
    
    @Override
    public String toString() {
        return "class OptionsTariffResponse {\n" +
               "    name: " + toIndentedString(name) + "\n" +
               "    title: " + toIndentedString(title) + "\n" +
               "    price: " + toIndentedString(price) + "\n" +
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

