package ru.sber.transport.corporate.web.resolvers.importer;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.sber.transport.corporate.web.resolvers.model.FilePosition;
import ru.sber.transport.file_works.importer.DataImporter;
import ru.sberbank.ditsib.corpclient.database.model.Organization;
import ru.sberbank.ditsib.corpclient.database.model.Position;
import ru.sberbank.ditsib.corpclient.human_readable_id.model.Prefix;
import ru.sberbank.ditsib.corpclient.service.OrganizationService;
import ru.sberbank.ditsib.corpclient.service.PositionService;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sber.transport.humanreadableid.service.SQGenerator;

import java.util.*;

/**
 * Сервис для распознавания и записи информации о сотруднике.
 */
@Component
@Transactional
@RequiredArgsConstructor
class PositionImporterImpl implements DataImporter<FilePosition> {

    private final PositionService positionService;

    private final OrganizationService organizationService;

    private final SQGenerator generator;

    @Override
    public void importData(FilePosition item, @NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
        var organization = organizationService.get(item.getOrganization())
                .orElseGet(Organization::new);

        var position = organization.getPositions().stream()
                .filter(pos -> pos.getName().equals(item.getName()))
                .findFirst().orElseGet(Position::new);

        position.setName(item.getName());
        position.setActiveStatus(position.getActiveStatus());
        position.setSelfApproved(item.isSelfApproved());
        position.setOrganization(organization);
        position.setAvailableClasses(parseClasses(item.getAvailableClasses()));

        if (position.getId() == null) {
            position.setHumanReadableId(generator.getNextId(Prefix.PS, organization.getDigitId()));
        }

        positionService.save(position);
    }

    private Set<TaxiClass> parseClasses(String taxiClassesString) {
        if (!StringUtils.hasText(taxiClassesString)) {
            return Collections.emptySet();
        }
        final Set<TaxiClass> taxiClasses = new HashSet<>();
        String[] split = taxiClassesString.split(";");
        Arrays.stream(split).forEach(elt -> TaxiClass.getByRusName(elt.trim()).ifPresent(taxiClasses::add));
        return taxiClasses;
    }
}
