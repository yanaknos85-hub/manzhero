package ru.sberbank.ditsib.corpclient.controller.impl;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.files.grpc.model.FileMeta;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.authorization.exceptions.UnauthorizedException;
import ru.sber.transport.authorization.service.CheckUserAccessService;
import ru.sberbank.ditsib.corpclient.controller.OrganizationController;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.corpclient.mapper.OrganizationMapper;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;
import ru.sberbank.ditsib.corpclient.service.FileService;
import ru.sberbank.ditsib.corpclient.service.OrganizationControllerService;
import ru.sberbank.ditsib.corpclient.service.TopicInitializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of OrganizationController
 */
@RequiredArgsConstructor
@RestController
class OrganizationControllerImpl implements OrganizationController {

    private final OrganizationControllerService organizationService;

    private final EmployeeService employeeService;

    private final OrganizationMapper mapper;

    private final TopicInitializer initializer;

    private final CheckUserAccessService checkUserAccessService;

    private final FileService fileService;

    @Setter
    @Value("#{'${organization.logo.allowedTypes:}'.split(',')}")
    private String[] allowedTypes;

    @CheckOrganizationAccess
    @Override
    public void editOrganization(@Organization UUID organizationId, NewOrganizationDTO newData) {
        organizationService.edit(organizationId, mapper.toModel(newData));
    }

    @CheckOrganizationAccess
    @Override
    public void deleteOrganization(@Organization UUID orgId) {
        organizationService.delete(orgId);
    }

    @CheckOrganizationAccess
    @Override
    @Transactional
    public ResponseEntity<OrganizationDTO> getOrganization(@Organization UUID userId) {
        return ResponseEntity.ok()
            .body(mapper.toDto(organizationService.get(userId)));
    }

    @Override
    public ResponseEntity<Iterable<OrganizationSelectDTO>> getOrganizations(JwtAuthenticationToken authentication, OrganizationParameters parameters, OrganizationProjection projection) {
        Iterable<OrganizationSelectDTO> result;
        try {
            checkUserAccessService.check();
            result = organizationService.get(parameters.getPage(), parameters.getSize(), parameters.getDirection(), parameters.getField(),
                parameters.getFilter(), projection);
        } catch (UnauthorizedException e) {
            var organization = employeeService.getOrganizationByUserId(UUID.fromString(authentication.getToken().getId()));
            var resultList = List.of(organization);
            if (OrganizationProjection.SELECT.equals(projection)) {
                result = resultList;
            } else {
                result = new PageImpl<>(List.of(organization), Pageable.unpaged(), 1);
            }
        }
        return ResponseEntity.ok()
            .body(result);
    }

    @CheckOrganizationAccess
    @Override
    public Iterable<HasEmployeeData> getEmployees(@Organization UUID organizationId, EmployeeParameters parameters,
                                                  EmployeeProjection projection) {
        organizationService.validateOrganizationId(organizationId);
        return employeeService.getEmployeesByOrgId(organizationId, parameters, projection);
    }

    @CheckOrganizationAccess
    @Override
    public Iterable<EmployeeDTO> searchEmployees(@Organization UUID organizationId, @NotBlank String searchString, EmployeeParameters parameters) {
        organizationService.validateOrganizationId(organizationId);
        return employeeService.findByFIOLike(searchString, organizationId, parameters);
    }

    @Override
    public Iterable<EmployeeDTO> searchAllEmployees(JwtAuthenticationToken authentication,
                                                    String searchString, EmployeeParameters parameters) {
        return employeeService.findByFIOLike(searchString, parameters, authentication);
    }

    @Override
    public Iterable<CustomerDTO> searchEmployeesByOrgsAndDeps(List<UUID> organizations, List<UUID> departments,
                                                              JwtAuthenticationToken authentication) {
        return employeeService.findEmployeesByOrganisationsAndDepartments(organizations, departments, authentication.getToken());
    }

    @Override
    public ResponseEntity<Void> uploadFile(MultipartFile file,
                                           UUID organizationId,
                                           JwtAuthenticationToken authentication,
                                           long length) throws IOException {
        var contentType = Optional.ofNullable(file.getContentType())
            .orElse(Files.probeContentType(Path.of(Optional.ofNullable(file.getOriginalFilename()).orElseThrow())));
        try (var stream = file.getInputStream()) {
            if (!List.of(allowedTypes).contains(contentType)) {
                throw new IllegalArgumentException(contentType);
            }
            fileService.upload(organizationId.toString(), stream, contentType, length);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()).build();
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadFile(UUID organizationId) {
        if (organizationService.get(organizationId) == null) {
            throw new EntityNotFoundException(Organization.class, organizationId);
        }
        var meta = fileService.meta(organizationId.toString());
        return createResponse(meta, fileService.download(meta));
    }

    private ResponseEntity<byte[]> createResponse(FileMeta meta, byte[] bytes) {
        var status = HttpStatusCode.valueOf(200);
        var response = ResponseEntity.status(status)
                .contentType(meta.contentType());
        if (bytes != null) {
            return response
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(bytes.length))
                    .body(bytes);
        } else {
            return response.header(HttpHeaders.CONTENT_RANGE, "bytes 0-0/%s".formatted(meta.size())).build();
        }
    }

    @Override
    public void resendAll(String key) {
        initializer.initialize(key);
    }
}