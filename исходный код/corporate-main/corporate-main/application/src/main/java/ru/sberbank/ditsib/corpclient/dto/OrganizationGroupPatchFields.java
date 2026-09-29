package ru.sberbank.ditsib.corpclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.sberbank.ditsib.corpclient.database.model.OrganizationGroup_;

public enum OrganizationGroupPatchFields {
    @JsonProperty(OrganizationGroup_.NAME)
    NAME,

    @JsonProperty("organizationIds")
    ORGANIZATION_IDS,
}
