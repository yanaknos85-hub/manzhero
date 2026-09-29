package ru.sberbank.ditsib.corpclient.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.corpclient.controller.CurrentUserActivePersonalCarController;
import ru.sberbank.ditsib.corpclient.dto.PersonalCarDTO;
import ru.sberbank.ditsib.corpclient.mapper.PersonalCarMapper;
import ru.sberbank.ditsib.corpclient.service.ActivePersonalCarService;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;
import ru.sberbank.ditsib.corpclient.service.FileService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Имплементация контроллера ЛС по умолчанию
 */
@RestController
public class CurrentUserActivePersonalCarControllerImpl extends BaseControllerImpl
        implements CurrentUserActivePersonalCarController {

    private final PersonalCarMapper mapper;

    private final ActivePersonalCarService activePersonalCarService;

    private final FileService fileService;

    private static final String FILE_NAME = "classpath:file/agreement.pdf";

    public CurrentUserActivePersonalCarControllerImpl(PersonalCarMapper mapper, ActivePersonalCarService activePersonalCarService, FileService fileService, EmployeeService employeeService) {
        super(employeeService);
        this.mapper = mapper;
        this.activePersonalCarService = activePersonalCarService;
        this.fileService = fileService;
    }

    @Override
    public void save(UUID personalCarId, JwtAuthenticationToken authentication) {
        var currentUser = getEmployeeIdByUserId(authentication);
        activePersonalCarService.saveOrUpdate(personalCarId, currentUser);
    }
    
    @Override
    public PersonalCarDTO get(JwtAuthenticationToken authentication) {
        var currentUser = getEmployeeIdByUserId(authentication);
        var active = activePersonalCarService.getActive(currentUser);
        if (active == null) {
            return null;
        } else {
            return mapper.personalCarToDTO(active);
        }
    }

    @Override
    public ResponseEntity<InputStreamResource> getAgreement() {

        try {
             return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + FILE_NAME.replaceAll(".*/(.*)", "$1"))
                    .body(new InputStreamResource(fileService.getFromResource(FILE_NAME).getInputStream()));
        } catch (IOException e) {
            var errorStream = new ByteArrayInputStream(e.getMessage().getBytes());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new InputStreamResource(errorStream));
        }
    }
}
