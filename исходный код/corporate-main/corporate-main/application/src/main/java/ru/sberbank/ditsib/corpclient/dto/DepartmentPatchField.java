package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.sberbank.ditsib.corpclient.database.model.Department_;

public enum DepartmentPatchField {
    @JsonProperty(Department_.FILIAL_FLAG)
    FILIAL_FLAG
}
