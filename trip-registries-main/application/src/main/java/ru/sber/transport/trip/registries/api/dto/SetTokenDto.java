package ru.sber.transport.trip.registries.api.dto;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * OrderRequest
 */

@Data
@ToString
public class SetTokenDto implements Serializable {
      private List<String> csrfTokenList;
      private List<String> cookieList;
}

