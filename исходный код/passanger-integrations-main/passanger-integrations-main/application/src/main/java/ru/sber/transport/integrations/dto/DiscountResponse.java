package ru.sber.transport.integrations.dto;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * DiscountResponse
 */

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class DiscountResponse {
    
    private String type;
    private Double absolutePrice;
    private Integer percent;
    @Valid
    private CouponResponse coupon;
    
    @Override
    public String toString() {
        return "class DiscountResponse {\n" +
               "    type: " + toIndentedString(type) + "\n" +
               "    absolutePrice: " + toIndentedString(absolutePrice) + "\n" +
               "    percent: " + toIndentedString(percent) + "\n" +
               "    coupon: " + toIndentedString(coupon) + "\n" +
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

