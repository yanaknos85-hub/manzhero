package ru.sberbank.ditsib.corpclient.controller.impl;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.authorization.annotations.CheckOrganizationAccess;
import ru.sber.transport.authorization.annotations.Organization;
import ru.sber.transport.files.grpc.model.FileMeta;
import ru.sberbank.ditsib.corpclient.controller.EmployeeController;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.docs.EmployeeDocumentFileFormats;
import ru.sberbank.ditsib.corpclient.dto.*;
import ru.sberbank.ditsib.corpclient.dto.docs.EmployeeDocumentFileDTO;
import ru.sberbank.ditsib.corpclient.mapper.EmployeeMapper;
import ru.sberbank.ditsib.corpclient.messaging.sender.EmployeeSender;
import ru.sberbank.ditsib.corpclient.messaging.sender.UserSender;
import ru.sberbank.ditsib.corpclient.service.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * Реализация контроллера сотрудников.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
class EmployeeControllerImpl implements EmployeeController {

    private static final String ROLE_COURIER = "ROLE_COURIER";
    
    private final EmployeeService employeeService;
    
    private final EmployeeSender employeeSender;
    
    private final DepartmentService departmentService;
    
    private final OrganizationService organizationService;
    
    private final PositionService positionService;
    
    private final EmployeeMapper employeeMapper;

    private final FileService fileService;

    private final UserSender userSender;

    @CheckOrganizationAccess
    @Override
    @Transactional
    public EmployeeDTO saveEmployee(@Organization UUID organizationId, UUID departmentId, NewEmployeeDTO newEmployee) {
        log.debug("A new user creating request. Data: {}", newEmployee.toString());
        validateIds(organizationId, departmentId);
        return employeeService.saveEmployee(departmentId, newEmployee);
    }
    
    @CheckOrganizationAccess
    @Override
    public void isUserExist(
            @Organization UUID organizationId,
            UUID departmentId,
            UUID userId
                       ) {
        validateIds(organizationId, departmentId);
        employeeService.validateUser(userId);
    }
    
    @CheckOrganizationAccess
    @Override
    public void editEmployee(@Organization UUID organizationId, UUID departmentId, UUID employeeId, NewEmployeeDTO newData) {
        var employee = getEmployee(organizationId, departmentId, employeeId);
        employeeService.updateEmployee(employee, newData);
    }
    
    @CheckOrganizationAccess
    @Override
    public EmployeeDTO getEmployeeByUser(@Organization UUID organizationId, UUID userId) {
        organizationService.validateOrganizationId(organizationId);
        return employeeService.getEmployeeByOrganizationIdAndUserId(organizationId, userId);
    }
    
    @CheckOrganizationAccess
    @Override
    public void deleteEmployee(@Organization UUID organizationId, UUID departmentId, UUID employeeId) {
        var deleted = getEmployee(organizationId, departmentId, employeeId);
        employeeService.deleteEmployee(deleted.getId());
    }

    @CheckOrganizationAccess
    @Override
    @Transactional
    public EmployeeDTO get(@Organization UUID organizationId, UUID departmentId, UUID employeeId) {
        validateIds(organizationId, departmentId);
        var employee = employeeService.validateAndGetEmployeeByIdAndDepartmentId(employeeId, departmentId);
        return employeeMapper.toDto(employee, organizationId);
    }
    
    @CheckOrganizationAccess
    @Override
    public Iterable<HasEmployeeData> getEmployees(@Organization UUID organizationId, UUID departmentId, EmployeeParameters parameters,
                                                              EmployeeProjection projection) {
        departmentService.validateDepartmentByIdAndOrgId(departmentId, organizationId);
        return employeeService.getEmployeesByDepartmentId(departmentId, parameters, projection);
    }
    
    @CheckOrganizationAccess
    @Override
    public Iterable<EmployeeDTO> getEmployeesByPosition(@Organization UUID organizationId, UUID positionId, EmployeeParameters parameters) {
        validatePositionIds(organizationId,positionId);
        return employeeService.getEmployeesByOrganizationIdAndPositionId(organizationId, positionId, parameters);
    }
    
    @CheckOrganizationAccess
    @Override
    public Iterable<EmployeeDTO> searchEmployees(
            @Organization UUID organizationId,
            UUID departmentId,
            String searchString,
            EmployeeParameters parameters
                                                            ) {
        validateIds(organizationId, departmentId);
        return employeeService.findByDepartmentAndByFIOLike(searchString, departmentId, parameters);
    }

    /**
     * Check that ids exist
     *
     * @param organizationId org id
     * @param departmentId department id
     */
    private void validateIds(@NotNull UUID organizationId, UUID departmentId) {
        organizationService.validateOrganizationId(organizationId);
        departmentService.validateDepartmentByIdAndOrgId(departmentId, organizationId);
    }
    
    
    /**
     * Проверка id на существование
     *
     * @param organizationId id организации
     * @param positionId id должности
     */
    private void validatePositionIds(@NotNull UUID organizationId, UUID positionId) {
        organizationService.validateOrganizationId(organizationId);
        positionService.validatePositionByIdAndOrgId(positionId, organizationId);
    }
    
    private Employee getEmployee(UUID organizationId, UUID departmentId, UUID employeeId) {
        validateIds(organizationId, departmentId);
        return employeeService.validateAndGetEmployeeByIdAndDepartmentId(employeeId, departmentId);
    }

    @Override
    @SneakyThrows(IOException.class)
    public ResponseEntity<EmployeeDocumentFileDTO> uploadFile(
            UUID organizationId,
            UUID departmentId,
            UUID employeeId,
            MultipartFile file,
            long length) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).build();
        }

        if (length == 0) {
            length = file.getSize();
        }

        String originalFileName = file.getOriginalFilename();

        EmployeeDocumentFileFormats fileFormat = getFileFormat(originalFileName);
        if (fileFormat == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).build();
        }

        var contentType = Optional.ofNullable(file.getContentType())
                .orElse(Files.probeContentType(Path.of(originalFileName)));
        if (contentType == null) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value()).build();
        }

        String fileFullName = employeeId.toString() + "_" +
                (LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss_SSS")) + " " +
                originalFileName.substring(0, originalFileName.length() - fileFormat.getFileFormat().length() - 1) + "." +
                fileFormat.getFileFormat().toLowerCase()).replace(" ", "_");

        try (var stream = file.getInputStream()) {
            fileService.upload(fileFullName, stream, contentType, length);
            return ResponseEntity.ok()
                    .body(EmployeeDocumentFileDTO
                            .builder()
                            .fileName(fileFullName)
                            .fileSize((int)length)
                            .fileFormat(fileFormat.getFileFormat())
                            .build());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadFile(UUID organizationId, UUID departmentId, UUID employeeId, String fileName) {
        var meta = fileService.meta(fileName);
        return createResponse(meta, fileService.download(meta));
    }

    private EmployeeDocumentFileFormats getFileFormat(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return null;
        }
        int doublePointIndex = originalFilename.indexOf("..");
        int lastPointIndex = originalFilename.lastIndexOf('.');
        if (doublePointIndex != -1 || lastPointIndex == -1) {
            return null;
        }
        String potentialFileFormat = originalFilename.substring(lastPointIndex + 1).toLowerCase();
        Optional<EmployeeDocumentFileFormats> optionalFormat = EmployeeDocumentFileFormats.getByFileFormat(potentialFileFormat);
        return optionalFormat.orElse(null);
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

}
