package ru.sber.transport.trip.registries.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * OrderRequest
 */

@Data
public class RegistryItem implements Serializable {
      private String requestSbTrId;
      private String requestSbTrHrId;
      private String tripHrId;
      private String material;
      private String mvz;
      private Double totalCostNotVAT;
      private Double totalVAT;
      private Double totalCost;
}

