package ru.sberbank.ditsib.transport.limits.controller.v2;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitResharingV2DTO;

@RequestMapping
@Tag(name = "Лимиты", description = "Набор операций для работы с лимитами")
public interface LimitController {

    /**
     * Reshare limits from child department to parent. (REDIRECTED)
     *
     * @param limitResharingDTO new limit data.
     */
    @PutMapping(value = "/reshare", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Перераспределение между лимитами",
            description = "Перераспределение между лимитами")
    void reShareLimit(
            @Valid @RequestBody LimitResharingV2DTO limitResharingDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @RequestHeader(value = "x-source", required = false) String source
    );
}
