package ru.sber.transport.integrations.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * CouponResponse
 */

@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
public class CouponResponse {
    @JsonProperty("promocode")
    private String promoCode;
    private String description;
    
    @Override
    public String toString() {
        return "class CouponResponse {\n" +
               "    promoCode: " + toIndentedString(promoCode) + "\n" +
               "    description: " + toIndentedString(description) + "\n" +
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

