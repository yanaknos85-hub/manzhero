package ru.sber.transport.corporate.web.resolvers.exporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.corporate.web.resolvers.model.FilePosition;
import ru.sberbank.ditsib.corpclient.dto.FileExportFilterDTO;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;
import ru.sberbank.ditsib.corpclient.service.PositionService;
import ru.sberbank.ditsib.corpclient.service.impl.file_resolvers.FilterDataResolver;
import ru.sberbank.ditsib.transport.constants.TaxiClass;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для распознавания и записи информации о сотруднике.
 */
@Component
@Transactional
class PositionExporterImpl extends FilterDataResolver<FilePosition> {

    private final PositionService positionService;

    private final OrganizationService organizationService;

    /**
     * Конструктор сервиса.
     *
     * @param positionService     сервис должностей.
     * @param organizationService сервис организаций.
     * @param objectMapper        маппер объектов.
     */
    public PositionExporterImpl(PositionService positionService,
                                OrganizationService organizationService,
                                ObjectMapper objectMapper) {
        super(objectMapper);
        this.positionService = positionService;
        this.organizationService = organizationService;
    }

    @Override
    protected List<FilePosition> exportFilteredData(FileExportFilterDTO filter, Map<String, ?> parameters,
                                                       JwtAuthenticationToken authentication) {
        var result = new ArrayList<FilePosition>();
        for (var item : positionService.findAllByOrganizationId(filter.getOrganizationId())) {
            var resItem = new FilePosition();

            resItem.setName(item.getName());
            resItem.setOrganization(organizationService.get(item.getOrganization().getId()).getOfficialName());
            resItem.setSelfApproved(item.isSelfApproved());
            resItem.setAvailableClasses(item.getAvailableClasses().stream().map(TaxiClass::getRusName).collect(
                    Collectors.joining(";")));

            result.add(resItem);
        }
        return result;
    }
}
