package ru.sber.transport.integrations.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.integrations.utils.WaitTimeOwDeserializer;

/**
 * OrderInfoResponseExtra
 */
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class OrderInfoResponseExtra {
    
    private String orderPartnerId;
    @JsonProperty("orderSberTransportId")
    private String orderSbertransportId;
    private String inn;
    private Integer tariff;
    private String createOrderTime;
    private String collectionTime;
    private OrderRoutePoints routePoints;
    private Double price;
    private Double distance;
    private Integer statusCode;
    private String finishTime;
    private CalculationResponse calculation;
    private Contact passenger;
    private String performerArrivalTime;
    private DriverResponse driver;
    private String comment;
    private String purpose;
    private String webViewLink;
    private Integer eta;
    private Boolean isTest;
    private Integer waitTime;
    @JsonDeserialize(using = WaitTimeOwDeserializer.class)
    private Integer waitTimeOW;
    
    @Override
    public String toString() {
        return "class OrderInfoResponseExtra {\n" +
               "    orderPartnerId: " + toIndentedString(orderPartnerId) + "\n" +
               "    orderSberTransportId: " + toIndentedString(orderSbertransportId) + "\n" +
               "    inn: " + toIndentedString(inn) + "\n" +
               "    tariff: " + toIndentedString(tariff) + "\n" +
               "    createOrderTime: " + toIndentedString(createOrderTime) + "\n" +
               "    collectionTime: " + toIndentedString(collectionTime) + "\n" +
               "    routePoints: " + toIndentedString(routePoints) + "\n" +
               "    price: " + toIndentedString(price) + "\n" +
               "    distance: " + toIndentedString(distance) + "\n" +
               "    statusCode: " + toIndentedString(statusCode) + "\n" +
               "    finishTime: " + toIndentedString(finishTime) + "\n" +
               "    calculation: " + toIndentedString(calculation) + "\n" +
               "    passenger: " + toIndentedString(passenger) + "\n" +
               "    performerArrivalTime: " + toIndentedString(performerArrivalTime) + "\n" +
               "    driver: " + toIndentedString(driver) + "\n" +
               "    comment: " + toIndentedString(comment) + "\n" +
               "    purpose: " + toIndentedString(purpose) + "\n" +
               "    webViewLink: " + toIndentedString(webViewLink) + "\n" +
               "    eta: " + toIndentedString(eta) + "\n" +
               "    isTest: " + toIndentedString(isTest) + "\n" +
               "    waitTime: " + toIndentedString(waitTime) + "\n" +
               "    waitTimeOW: " + toIndentedString(waitTimeOW) + "\n" +
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

