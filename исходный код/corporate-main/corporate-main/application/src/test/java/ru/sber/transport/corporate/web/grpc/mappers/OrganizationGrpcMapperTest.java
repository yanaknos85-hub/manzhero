package ru.sber.transport.corporate.web.grpc.mappers;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.corporate.business.model.Active;
import ru.sber.transport.corporate.business.model.Organization;
import ru.sber.transport.corporate.grpc.service.OrganizationsOuterClass;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationGrpcMapperTest {

    private final OrganizationGrpcMapper mapper = new OrganizationGrpcMapperImpl(
            Mappers.getMapper(ActiveStatusGrpcMapper.class),
            Mappers.getMapper(NullableMapper.class));

    @Test
    void toGrpc() {
        var source = Instancio.create(Organization.class);
        var actual = mapper.toGrpc(source);
        assertThat(actual).extracting(
                        OrganizationsOuterClass.Organization::getId,
                        OrganizationsOuterClass.Organization::getDigitId,
                        OrganizationsOuterClass.Organization::getName,
                        OrganizationsOuterClass.Organization::getAddress,
                        OrganizationsOuterClass.Organization::getMsrn,
                        OrganizationsOuterClass.Organization::getTid,
                        OrganizationsOuterClass.Organization::getCode,
                        OrganizationsOuterClass.Organization::getDeleted,
                        OrganizationsOuterClass.Organization::getGroup,
                        OrganizationsOuterClass.Organization::getContactsList,
                        OrganizationsOuterClass.Organization::getType,
                        OrganizationsOuterClass.Organization::getClassesList
                )
                .containsExactly(
                        source.getId().toString(),
                        Long.valueOf(source.getDigitId()).intValue(),
                        source.getName(),
                        source.getAddress(),
                        source.getMsrn(),
                        source.getTid(),
                        OrganizationsOuterClass.NullableInt.newBuilder()
                                .setValue(source.getCode())
                                .build(),
                        Active.INACTIVE.equals(source.getStatus()),
                        OrganizationsOuterClass.NullableString.newBuilder()
                                .setValue(source.getGroupId().toString())
                                .build(),
                        Collections.emptyList(),
                        source.getSyncId() == null
                                ? OrganizationsOuterClass.StructureType.EXTERNAL
                                : OrganizationsOuterClass.StructureType.INTERNAL,
                        Collections.emptyList()
                );
    }
}