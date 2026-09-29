package ru.sber.transport.integrations.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * TariffResponse
 */

@AllArgsConstructor
@Setter
@Getter
@EqualsAndHashCode
public class TariffResponse {
    private Long id;
    private String name;
    private Double startPrice;
    private Double oneKmPrice;
    private Double oneMinPrice;
    private Integer freeWaitMinutes;
    private Double waitTimePrice;
    private Double cancellationPrice;
    @Valid
    private List<OptionsTariffResponse> options = null;
    
    @Override
    public String toString() {
        return "class TariffResponse {\n" +
               "    id: " + toIndentedString(id) + "\n" +
               "    name: " + toIndentedString(name) + "\n" +
               "    startPrice: " + toIndentedString(startPrice) + "\n" +
               "    oneKmPrice: " + toIndentedString(oneKmPrice) + "\n" +
               "    oneMinPrice: " + toIndentedString(oneMinPrice) + "\n" +
               "    freeWaitMinutes: " + toIndentedString(freeWaitMinutes) + "\n" +
               "    waitTimePrice: " + toIndentedString(waitTimePrice) + "\n" +
               "    cancellationPrice: " + toIndentedString(cancellationPrice) + "\n" +
               "    options: " + toIndentedString(options) + "\n" +
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

