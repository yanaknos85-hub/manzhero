package ru.sber.transport.integrations.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * OrderRoutePoints
 */

@Getter
@AllArgsConstructor
public class OrderRoutePoints {
    
    /**
     * Больше не используем, все точки предаём в waypoints
     */
    @Deprecated
    private Source source;
    
    /**
     * Больше не используем, все точки предаём в waypoints
     */
    @Deprecated
    private Destination destination;
    
    /**
     * Список точек с контактами пассажиров
     */
    @Valid
    private List<Waypoint> waypoints;
    
    @Override
    public String toString() {
        return "class OrderRoutePoints {\n" +
               "    source: " + toIndentedString(source) + "\n" +
               "    destination: " + toIndentedString(destination) + "\n" +
               "    waypoints: " + toIndentedString(waypoints) + "\n" +
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