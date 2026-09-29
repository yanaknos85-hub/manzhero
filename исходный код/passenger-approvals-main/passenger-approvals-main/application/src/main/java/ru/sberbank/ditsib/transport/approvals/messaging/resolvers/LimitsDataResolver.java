package ru.sberbank.ditsib.transport.approvals.messaging.resolvers;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sberbank.ditsib.transport.constants.limits.LimitServiceType;
import ru.sberbank.ditsib.transport.dto.limits.GetLimitDTO;
import ru.sberbank.ditsib.transport.dto.limits.GetLimitSharingDTO;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "limits", url = "${feign.url.limits:}")
public interface LimitsDataResolver {
    
    /**
     * Get limit by department and year and service type.
     *
     * @param departmentId department id
     * @param year year
     * @param limitServiceType тип услуги
     *
     * @return limit dto.
     */
    @GetMapping(value = "/deplimits/getByDepartmentAndYearAndLimitServiceTypeFull/{departmentId}/year/{year}/limitServiceType/{limitServiceType}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение лимита по подразделению и году и типу услуги",
               description = "Получение лимита по подразделению и году и типу услуги")
    GetLimitDTO getByDepartmentAndYearAndLimitServiceTypeFull(
            @PathVariable("departmentId") @NotNull UUID departmentId,
            @PathVariable("year") @NotNull Integer year,
            @PathVariable("limitServiceType") @NotNull LimitServiceType limitServiceType,
            @RequestHeader("Authorization") String token);
    
    /**
     * Get limit sharing by limit full
     * @param limitId limit id
     * @return list of limit sharing dto
     */
    @GetMapping(value = "/limitsharing/getByLimit/full/{limitId}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение распределения лимита c остатками за период",
               description = "Получение распределения лимита c остатками за период")
    List<GetLimitSharingDTO> getByLimitFull(@PathVariable("limitId") @NotNull UUID limitId,
                                            @RequestHeader("Authorization") String token);
}
