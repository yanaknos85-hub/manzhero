package ru.sberbank.ditsib.corpclient.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Optional;

/**
 * Controller for working with delegates.
 */
@RequestMapping({"/self/delegates", "/self/delegates/"})
@Tag(name = "Делегаты", description = "Набор операций для работы с делегатами  текущего пользователя")
public interface CurrentUserDelegateController {
    
    /**
     * Get all delegates for logged user for specified transport type
     *
     * @param transportType transport type
     *
     * @return collection of delegate candidates.
     */
    @GetMapping(value = {"/candidates/{transportType}", "/candidates/{transportType}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка кандидатов в делегаты", description = "Получение списка кандидатов в " +
                                                                                 "делегаты текущего пользователя на " +
                                                                                 "указанную дату")
    Iterable<EmployeeDTO> getAllCandidatesForCurrentUser(
            @Parameter(hidden = true) JwtAuthenticationToken authentication,
            @Parameter(description = "Тип транспорта", required = true)
            @PathVariable("transportType") TransportTypeEnum transportType,
            @Parameter(description = "Дата для получения кандидатов", required = true)
            @NotNull @RequestParam("date") String date,
            @Parameter(description = "Параметры запроса") EmployeeParameters parameters
                                                                  );
    
    /**
     * Get all delegates for logged user
     *
     * @return get collection of delegate records
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение списка делегатов",
               description = "Получение данных всех делегатов текущего пользователя")
    Collection<GetDelegateRecordDTO> getAllDelegatesForLoggedUser(
            JwtAuthenticationToken authentication,
            @RequestParam("date") Optional<String> date
                                                                           );
    
    /**
     * @param authentication параметры аутентификации
     * @param date дата на которую искать записи
     *
     * @return сведения о делегированных полномочиях текущему сотруднику в т.ч. о сотрудниках делегировавших свои
     * полномочия
     */
    @GetMapping(value = {"/delegated", "/delegated/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение сведений о делегированных полномочиях текущему " +
                                                    "сотруднику в т.ч. о сотрудниках делегировавших свои полномочия")
    Collection<GetDelegateRecordDTO> getDelegateRecordsByEmpoyee(
            JwtAuthenticationToken authentication, @RequestParam("date") String date
                                                                          );
    
}
