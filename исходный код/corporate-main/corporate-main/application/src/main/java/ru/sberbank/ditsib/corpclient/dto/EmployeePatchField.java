package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.sberbank.ditsib.corpclient.database.model.Employee_;

public enum EmployeePatchField {
    @JsonProperty(Employee_.MOBILE_PHONE)
    MOBILE_PHONE,
    @JsonProperty(Employee_.LAST_NAME)
    LAST_NAME,
    @JsonProperty(Employee_.FIRST_NAME)
    FIRST_NAME,
    @JsonProperty(Employee_.PATRONYMIC)
    PATRONYMIC
}
