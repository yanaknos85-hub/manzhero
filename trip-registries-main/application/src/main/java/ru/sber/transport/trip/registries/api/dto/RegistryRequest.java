package ru.sber.transport.trip.registries.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderRequest
 */

@Data
public class RegistryRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  private String registryId;

  private String contractIdUVHD;

  private String organizationId;

  private String startDate;

  private String endDate;

  List<RegistryItem> trip = new ArrayList<>();

}

