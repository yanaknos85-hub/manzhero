package ru.sber.transport.integrations.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * CalculationResponse
 */

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class CalculationResponse {
    
    private String hash;
    @Valid
    private TariffResponse tariff;
    @Valid
    private TrackResponse track;
    private Double precalculatedPrice;
    private Double priceBeforeDiscount;
    @Valid
    private List<DiscountResponse> discounts;
    @Valid
    private List<List<Double>> route;
    private Integer eta;
    private BigDecimal totalPrice;
    
    @Override
    public String toString() {
        return "class CalculationResponse {\n" +
               "    hash: " + toIndentedString(hash) + "\n" +
               "    tariff: " + toIndentedString(tariff) + "\n" +
               "    track: " + toIndentedString(track) + "\n" +
               "    precalculatedPrice: " + toIndentedString(precalculatedPrice) + "\n" +
               "    priceBeforeDiscount: " + toIndentedString(priceBeforeDiscount) + "\n" +
               "    discounts: " + toIndentedString(discounts) + "\n" +
               "    route: " + toIndentedString(route) + "\n" +
               "    eta: " + toIndentedString(eta) + "\n" +
               "    totalPrice: " + toIndentedString(totalPrice) + "\n" +
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

