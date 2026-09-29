package ru.sberbank.ditsib.transport.limits.service;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sberbank.ditsib.transport.limits.dto.DepLimitEconomyDTO;
import ru.sberbank.ditsib.transport.limits.dto.DepLimitSharingDTO;
import ru.sberbank.ditsib.transport.limits.dto.PrimarySharingDTO;
import ru.sberbank.ditsib.transport.limits.dto.TaskStartedDto;
import ru.sberbank.ditsib.transport.limits.dto.v3.SecondarySharingData;
import ru.sberbank.ditsib.transport.limits.model.basic.Employee;
import ru.sberbank.ditsib.transport.limits.model.limit.DepLimit;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with limits.
 */
public interface DepLimitService {
    
    /**
     * Add limit.
     *
     * @param depLimit request.
     * @return result of adds.
     */
    DepLimit add(final DepLimit depLimit);
    
    /**
     * Save limit.
     *
     * @param depLimit request.
     */
    void save(DepLimit depLimit);
    
    /**
     * Delete limit.
     *
     * @param depLimit limit.
     */
    void delete(DepLimit depLimit);
    
    /**
     * Get limit.
     *
     * @param id ID of request.
     *
     * @return limit.
     */
    Optional<DepLimit> get(UUID id);
    
    /**
     * Get all limits.
     *
     * @return limit.
     */
    List<DepLimit> getAll();
    
    /**
     * Get limit by department.
     *
     * @param departmentId department id
     *
     * @return deplimit.
     */
    List<DepLimit> getByDepartment(UUID departmentId);

    /**
      * Поиск первого доступного лимита
      *
      * @param departmentId идентификатор подразделения
      * @return данные о лимитах
      */
    List<DepLimit> findAccessible(UUID departmentId);

    /**
     * Get limit by department and year.
     *
     * @param departmentId id of department
     * @param year year
     *
     * @return deplimit.
     */
    DepLimit getByDepartmentAndYear(UUID departmentId, Integer year);
    
    /**
     * Get limit by department and year.
     *
     * @param departmentId id of department
     * @param year year
     *
     * @return deplimit.
     */
    List<DepLimit> getByDepartmentAndYearAll(UUID departmentId, Integer year);
    
    /**
     * Get limit by department and year and service type.
     *
     * @param departmentId id of department
     * @param year year
     * @param limitServiceType тип услуги
     *
     * @return deplimit.
     */
    DepLimit getByDepartmentAndYearAndLimitServiceType(UUID departmentId, Integer year, String limitServiceType);

    /**
     * Get limit by department and year and service type.
     *
     * @param departmentId id of department
     * @param year year
     * @param limitServiceType тип услуги
     * @param withChildren вытянуть данные с детьми
     *
     * @return deplimit.
     */
    DepLimit getByDepartmentAndYearAndLimitServiceType(UUID departmentId, Integer year, String limitServiceType, boolean withChildren);
    
    /**
     * Share department limit to its child departments.
     *
     * @param parentDepLimit parent limit
     * @param dtoList input data list.
     * @param author author.
     * @return sharing data.
     */
    List<SecondarySharingData> shareDepLimitSecondary(DepLimit parentDepLimit, List<DepLimitSharingDTO> dtoList, Employee author);
    
    /**
     * Share main limit economy after limit has been allready shared.
     *
     * @param parentDepLimit parent limit
     * @param dtoList input data list.
     * @param author author.
     *
     */
    void shareDepLimitEconomy(DepLimit parentDepLimit, List<DepLimitEconomyDTO> dtoList, Employee author);
    
    /**
     * Share main limit by transport types.
     *
     * @param parentDepLimit parent limit
     * @param dtoList input data list.
     * @param author author.
     *
     */
    void shareDepLimitPrimary(DepLimit parentDepLimit, List<PrimarySharingDTO> dtoList, Employee author);
    
    /**
     * Delete all limits by organization.
     *
     * @param organizationId organization Id
     * @param author author.
     *
     * @return number of deleted limits
     */
    Integer deleteLimits(UUID organizationId, Employee author);
    
    /**
     * Delete all limits by parameters.
     *
     * @param organizationId organization Id
     * @param serviceType serviceType
     * @param year year
     * @param author author.
     *
     * @return number of deleted limits
     */
    Integer deleteLimits(UUID organizationId, String serviceType, Integer year, Employee author);
    
    /**
     * Audit limits by organization.
     *
     * @param organizationId organization Id
     * @param year year.
     * @param logOkRecords flag to log OKs.
     *
     * @return list of errors
     */
    List<String> auditLimits(UUID organizationId, Integer year, boolean logOkRecords);
    
    /**
     * Audit limits async mode by organization.
     *
     * @param organizationId organization Id
     * @param year year.
     * @param logOkRecords flag to log OKs.
     *
     * @return list of errors
     */
    TaskStartedDto auditLimitsAsync(UUID organizationId, Integer year, boolean logOkRecords);
    
    /**
     * Audit limits async mode by organization.
     *
     * @param authorId authorId
     * @param limitId ID of limits.
     * @param authentication authentication data.
     * @param appName name of application.
     * @param source source.
     *
     * @return list of errors
     */
    TaskStartedDto closeLimitsAsync(UUID authorId, UUID limitId, JwtAuthenticationToken authentication, String appName, String source);
    
    /**
     * Получение файла.
     *
     * @param fileName имя файла.
     * @return файл.
     */
    Optional<File> getAsyncFile(String fileName);
    
    /**
     * Print limits by organization.
     *
     * @param organizationId organization Id
     * @param year year of limit
     * @param str str
     */
    void printLimits(UUID organizationId, Integer year, String str);

    /**
     * Есть ли активные лимиты по списку идентификаторов подразделений
     * @param uuids список идентификаторов подразделений
     * @return есть лимиты
     */
    boolean checkHaveNotClosedChildLimits(List<UUID> uuids);
}
