package ru.sber.transport.integrations.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * VehicleResponse
 */
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class VehicleResponse {
    private String mark;
    private String model;
    private String color;
    private String registrationNumber;
    
    @Override
    public String toString() {
        return "class VehicleResponse {\n" +
               "    mark: " + toIndentedString(mark) + "\n" +
               "    model: " + toIndentedString(model) + "\n" +
               "    color: " + toIndentedString(color) + "\n" +
               "    registrationNumber: " + toIndentedString(registrationNumber) + "\n" +
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

