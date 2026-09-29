package ru.sberbank.ditsib.transport.limits.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitReSharingByTransportTypeV2DTO;

@RequestMapping("/transport-type")
public interface TransportTypeLimitController {

    /**
     * Reshare limits by transport types.
     *
     * @param limitReSharingByTransportTypeDTO new limit data.
     */
    @PutMapping(value = "/reshare", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Перераспределение между видами транспорта",
            description = "Перераспределение между видами транспорта")
    void reShareLimitBetweenTransportTypes(
            @Valid @RequestBody LimitReSharingByTransportTypeV2DTO limitReSharingByTransportTypeDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );

}
