package ru.sber.transport.trip.registries.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * OrderResponse
 */

@Data
@NoArgsConstructor
public class ActNumberResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID registryId;

  private String actId;

  private String errorType;

  private String errorText;
}



