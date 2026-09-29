package ru.sber.transport.integrations.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * DriverResponse
 */

@EqualsAndHashCode
@Getter
@Setter
@AllArgsConstructor
public class DriverResponse {
    private String id;
    private String name;
    @JsonProperty("patronimyc") // так поле пишется в аналитике
    private String patronymic; // намеренно поставленно в правильной грамматике
    private String secName;
    private String phone;
    private String imageUrl;
    private Double rating;
    private Integer companyId;
    @Valid
    private LicenseResponse license;
    @Valid
    private VehicleResponse vehicle;
    
    @Override
    public String toString() {
        return "class DriverResponse {\n" +
               "    id: " + toIndentedString(id) + "\n" +
               "    name: " + toIndentedString(name) + "\n" +
               "    patronymic: " + toIndentedString(patronymic) + "\n" +
               "    secName: " + toIndentedString(secName) + "\n" +
               "    phone: " + toIndentedString(phone) + "\n" +
               "    imageUrl: " + toIndentedString(imageUrl) + "\n" +
               "    rating: " + toIndentedString(rating) + "\n" +
               "    companyId: " + toIndentedString(companyId) + "\n" +
               "    license: " + toIndentedString(license) + "\n" +
               "    vehicle: " + toIndentedString(vehicle) + "\n" +
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

