package ru.sberbank.ditsib.transport.limits.controller.v2;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.limits.dto.v2.DepLimitPrimaryV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.GetLimitV2DTO;
import ru.sberbank.ditsib.transport.limits.dto.v2.LimitReSharingByDepartmentV2DTO;

import java.util.UUID;

/**
 * Controller interface for employee limits.
 */
@RequestMapping
@Tag(name = "Лимиты подразделений", description = "Набор операций для работы с лимитами подразделений")
public interface DepLimitController {
    
    /**
     * Add a new limit. (REDIRECTED)
     *
     * @param depLimitPrimaryDTO new limit data.
     *
     * @return added limit.
     */
    @PostMapping(value = "/organization/{organizationId}/department",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    GetLimitV2DTO add(@PathVariable("organizationId") UUID organizationId,
                              @Valid @RequestBody DepLimitPrimaryV2DTO depLimitPrimaryDTO,
                              @Parameter(hidden = true) JwtAuthenticationToken authentication,
                      @RequestHeader(value = "x-source", required = false) String source);


    /**
     * Reshare limits by departments. TODO
     *
     * @param limitReSharingByDepartmentDTO new limit data.
     */
    @PutMapping(value = "/departments/reshare", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    void reShareLimitDepartments(
            @Valid @RequestBody LimitReSharingByDepartmentV2DTO limitReSharingByDepartmentDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @RequestHeader(value = "x-source", required = false) String source
                                       );
}
