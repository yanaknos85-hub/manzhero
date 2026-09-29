package ru.sber.transport.integrations.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * TrackResponse
 */

@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class TrackResponse {
    
    private Double distance;
    private Double duration;
    
    
    @Override
    public String toString() {
        return "class TrackResponse {\n" +
               "    distance: " + toIndentedString(distance) + "\n" +
               "    duration: " + toIndentedString(duration) + "\n" +
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

