package ru.sberbank.ditsib.corpclient.service.impl.file_resolvers;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.file_works.importer.DataImporter;
import ru.sberbank.ditsib.corpclient.database.model.Attribute;
import ru.sberbank.ditsib.corpclient.database.model.AttributeStatus;
import ru.sberbank.ditsib.corpclient.dto.AttributesFileDTO;
import ru.sberbank.ditsib.corpclient.messaging.sender.EmployeeSender;
import ru.sberbank.ditsib.corpclient.service.AttributeService;
import ru.sberbank.ditsib.corpclient.service.EmployeeService;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Сервис для распознавания и записи информации о сотруднике.
 */
@Component
@Transactional
@RequiredArgsConstructor
class AttributesResolverImpl implements DataImporter<AttributesFileDTO>, DataExporter<AttributesFileDTO> {
    
    private final OrganizationService organizationService;
    
    private final EmployeeSender sender;
    
    private final AttributeService attributeService;
    
    private final EmployeeService employeeService;
    
    @Override
    public void importData(AttributesFileDTO list, @NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
        var organizationName = list.getOrganization();
        var personNumber = list.getPersonNumber();
        
        var organization = organizationService.get(organizationName)
                                              .orElseThrow(() -> new NoSuchElementException(
                                                      String.format("Организация %s не найдена", organizationName)));
        var employee = employeeService.getEmployeeByPersonalNumberAndOrganizationId(personNumber, organization.getId())
                                      .filter(emp -> emp.getDepartment().getOrganization().getId().equals(organization.getId()))
                                      .orElseThrow(() -> new NoSuchElementException(
                                              String.format("Сотрудник с табельным номером %s не найден",
                                                            personNumber)));
            var attributes = new ArrayList<Attribute>();
            for (var attr : list.getAttributes().split(",")) {
                if (attr.isBlank()) {
                    continue;
                }
                var attributeName = attr.trim();
                var attribute = attributeService.get(attributeName).orElseGet(Attribute::new);
                attribute.setName(attributeName);
                attributes.add(attribute);
            }
            
            employee.getAttributes().clear();
            employee.getAttributes().addAll(attributeService.mergeAll(attributes));
            
            sender.send(employee);
        
    }
    
    @Override
    public @NonNull List<AttributesFileDTO> exportData(@NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
        var result = new ArrayList<AttributesFileDTO>();
        var employees = employeeService.getEmployeesWIthAttributes();
        for (var employee : employees) {
            var attributes = employee.getAttributes().stream()
                                     .filter(attributeDto -> AttributeStatus.ACTIVE.equals(attributeDto.getStatus()))
                                     .map(Attribute::getName)
                                     .collect(Collectors.joining(", "));
            if (StringUtils.hasText(attributes)) {
                var resItem = new AttributesFileDTO();
    
                resItem.setOrganization(employee.getDepartment().getOrganization().getOfficialName());
                resItem.setPersonNumber(employee.getPersonnelNumber());
                resItem.setAttributes(employee.getAttributes().stream()
                                              .filter(attributeDto -> AttributeStatus.ACTIVE.equals(attributeDto.getStatus()))
                                              .map(Attribute::getName)
                                              .collect(Collectors.joining(", ")));
    
                result.add(resItem);
            }
        }
        return result;
    }
}
