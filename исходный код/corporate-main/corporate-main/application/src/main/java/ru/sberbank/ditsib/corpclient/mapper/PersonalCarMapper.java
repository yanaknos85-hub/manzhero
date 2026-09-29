package ru.sberbank.ditsib.corpclient.mapper;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.sberbank.ditsib.corpclient.database.model.Employee;
import ru.sberbank.ditsib.corpclient.database.model.docs.EmployeeDocument;
import ru.sberbank.ditsib.corpclient.database.model.PersonalCar;
import ru.sberbank.ditsib.corpclient.dto.NewPersonalCarDTO;
import ru.sberbank.ditsib.corpclient.dto.PersonalCarDTO;
import ru.sberbank.ditsib.corpclient.dto.docs.*;
import ru.sberbank.ditsib.transport.messaging.messages.PersonalCarMessage;

import java.util.UUID;

@Mapper
public interface PersonalCarMapper {

    @Mapping(target = "engineVolume", source = "personalCar.documents.passportTs.engineVolume")
    PersonalCarMessage toMessage(PersonalCarDTO personalCar);

    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "persDataAccept", target = "persDataAccept", qualifiedByName = "mapPersDataToBoolean")
    NewPersonalCarDTO personalCarToNewDTO(PersonalCar auto);
    
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "persDataAccept", target = "persDataAccept", qualifiedByName = "mapPersDataToBoolean")
    PersonalCarDTO personalCarToDTO(PersonalCar auto);
    
    @Mapping(target = "employee", source = "employeeId")
    PersonalCar dtoToPersonalCar(PersonalCarDTO dto);
    
    @Mapping(target = "employee", source = "employeeId")
    PersonalCar newDTOToPersonalCar(NewPersonalCarDTO dto);
    
    @Mapping(target = "id", source = "uuid")
    Employee employeeFromId(UUID uuid);

    DriverLicDTO EmployeeDocumentToDriverLicDTO(EmployeeDocument driverLic);
    OsagoDTO EmployeeDocumentToOsagoDTO(EmployeeDocument osago);
    PassportTsDTO EmployeeDocumentToPassportTsDTO(EmployeeDocument passportTs);
    MarriageCertificateDTO EmployeeDocumentToMarriageCertificateDTO(EmployeeDocument marriageCertificate);
    AgreementPdnDTO EmployeeDocumentToAgreementPdnDTO(EmployeeDocument AgreementPdn);

    @Named("mapPersDataToBoolean")
    static boolean mapPersDataToBoolean(String persDataAccept) {
        return StringUtils.isNotBlank(persDataAccept);
    }

}
