package ru.sber.transport.trip.registries.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * OrderResponse
 */

@Data
@NoArgsConstructor
public class ActStatusResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  private String actId;

  private String updId;

  private String lockingIndicator;

  private String lockingIndicatorText;

  private String updStatus;

  private String updStatusText;
}

