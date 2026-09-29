package ru.sberbank.ditsib.corpclient.service.impl;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.corpclient.database.dao.ActivePersonalCarRepository;
import ru.sberbank.ditsib.corpclient.database.dao.DocumentTypeRepository;
import ru.sberbank.ditsib.corpclient.database.dao.EmployeeDocumentRepository;
import ru.sberbank.ditsib.corpclient.database.dao.PersonalCarRepository;
import ru.sberbank.ditsib.corpclient.database.model.DocumentCode;
import ru.sberbank.ditsib.corpclient.database.model.DocumentType;
import ru.sberbank.ditsib.corpclient.database.model.docs.EmployeeDocument;
import ru.sberbank.ditsib.corpclient.database.model.PersonalCar;
import ru.sberbank.ditsib.corpclient.dto.NewPersonalCarDTO;
import ru.sberbank.ditsib.corpclient.dto.PersonalCarDTO;
import ru.sberbank.ditsib.corpclient.dto.docs.*;
import ru.sberbank.ditsib.corpclient.mapper.PersonalCarMapper;
import ru.sberbank.ditsib.corpclient.service.PersonalCarEnrichmentService;
import ru.sberbank.ditsib.corpclient.service.PersonalCarService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of PersonalCarService
 */
@Validated
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PersonalCarsServiceImpl implements PersonalCarService {
    private final PersonalCarMapper mapper;
    private final ActivePersonalCarRepository activePersonalCarRepository;
    private final PersonalCarRepository repository;
    private final PersonalCarEnrichmentService personalCarEnrichmentService;
    private final DocumentTypeRepository documentTypeRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;

    @Override
    public PersonalCarDTO savePersonalCar(@Valid NewPersonalCarDTO newDTO, UUID currentUser) {
        var newEntity = mapper.newDTOToPersonalCar(newDTO);
        personalCarEnrichmentService.addUserAcceptInfo(newEntity);
        var personalCarToDTO = mapper.personalCarToDTO(repository.save(newEntity));
        saveDocuments(newDTO, personalCarToDTO.getId(), currentUser);
        enrichWithDocumentsInfo(List.of(personalCarToDTO));
        return personalCarToDTO;
    }

    private void saveDocuments(NewPersonalCarDTO personalCarDTO, UUID carId, UUID currentUser) {
        var employeeId = personalCarDTO.getEmployeeId();
        var personalCarDTOWithOldDocuments = PersonalCarDTO.builder()
                .id(carId)
                .employeeId(employeeId)
                .build();
        enrichWithDocumentsInfo(List.of(personalCarDTOWithOldDocuments));

        var oldDocuments = Optional.ofNullable(personalCarDTOWithOldDocuments).map(PersonalCarDTO::getDocuments);
        var documents = Optional.ofNullable(personalCarDTO).map(NewPersonalCarDTO::getDocuments);

        // Водительское удостоверение
        DriverLicDTO oldDriverLicDTO = oldDocuments.map(DocumentsDTO::driverLic).orElse(null);
        DriverLicDTO driverLicDTO = documents.map(DocumentsDTO::driverLic).orElse(null);
        driverLicDTO = fillEmployeeId(driverLicDTO, employeeId);
        saveDriverLic(oldDriverLicDTO, driverLicDTO, currentUser);

        // Свидетельство о браке
        MarriageCertificateDTO oldMarriageCertificateDTO = oldDocuments.map(DocumentsDTO::marriageCertificate).orElse(null);
        MarriageCertificateDTO marriageCertificateDTO = documents.map(DocumentsDTO::marriageCertificate).orElse(null);
        marriageCertificateDTO = fillEmployeeId(marriageCertificateDTO, employeeId);
        saveMarriageCertificate(oldMarriageCertificateDTO, marriageCertificateDTO, currentUser);

        // Полис ОСАГО
        OsagoDTO oldOsagoDTO = oldDocuments.map(DocumentsDTO::osago).orElse(null);
        OsagoDTO osagoDTO = documents.map(DocumentsDTO::osago).orElse(null);
        osagoDTO = fillEmployeeId(osagoDTO, employeeId);
        osagoDTO = fillCarId(osagoDTO, carId);
        saveOsago(oldOsagoDTO, osagoDTO, currentUser);

        // ПТС
        PassportTsDTO oldPassportTsDTO = oldDocuments.map(DocumentsDTO::passportTs).orElse(null);
        PassportTsDTO passportTsDTO = documents.map(DocumentsDTO::passportTs).orElse(null);
        passportTsDTO = fillEmployeeId(passportTsDTO, employeeId);
        passportTsDTO = fillCarId(passportTsDTO, carId);
        savePassportTs(oldPassportTsDTO, passportTsDTO, currentUser);

        // Согласие об обработке персональных данных
        AgreementPdnDTO oldAgreementPdnDTO = oldDocuments.map(DocumentsDTO::agreementPdn).orElse(null);
        AgreementPdnDTO agreementPdnDTO = documents.map(DocumentsDTO::agreementPdn).orElse(null);
        agreementPdnDTO = fillEmployeeId(agreementPdnDTO, employeeId);
        agreementPdnDTO = agreementPdnDTO == null || agreementPdnDTO.carId() != null ?
                agreementPdnDTO : agreementPdnDTO.toBuilder().carId(carId).build();
        saveAgreementPdn(oldAgreementPdnDTO, agreementPdnDTO, currentUser);
    }

    private void saveAgreementPdn(AgreementPdnDTO oldAgreementPdnDTO, AgreementPdnDTO agreementPdnDTO, UUID currentUser) {
        DocumentType documentType = documentTypeRepository.findById(DocumentCode.AGREEMENT_PDN).orElse(null);
        if (documentType == null) {
            log.warn("В настройках системы нет вида документа 'Согласие на обработку персональных данных' (AGREEMENT_PDN)");
            log.warn("Информация о согласии на обработку персональных данных не сохранена");
            return;
        }
        if (agreementPdnDTO == null || isEqual(oldAgreementPdnDTO, agreementPdnDTO)) {
            return;
        }
        UUID id = (isNew(oldAgreementPdnDTO, agreementPdnDTO)) ? UUID.randomUUID() : agreementPdnDTO.id();

        var document = employeeDocumentRepository.findById(id).orElseGet(EmployeeDocument::new);
        document.setId(id);
        document.setDocumentType(documentType);
        document.setEmployeeId(agreementPdnDTO.employeeId());
        document.setCarId(agreementPdnDTO.carId());
        document.setFileName(agreementPdnDTO.fileName());
        document.setFileSize(agreementPdnDTO.fileSize());
        document.setFileFormat(agreementPdnDTO.fileFormat());
        document.updateAuthorInfo(currentUser);
        document.setIssueDateDocument(LocalDateTime.now());

        employeeDocumentRepository.save(document);
    }

    public boolean isNew(AgreementPdnDTO oldAgreementPdnDTO, AgreementPdnDTO newAgreementPdnDTO) {
        // Если id документа не совпадает, то документ новый
        // Если id документа совпадает, то считаем, что это редактируется старый документ
        return (oldAgreementPdnDTO == null || oldAgreementPdnDTO.id() == null || !oldAgreementPdnDTO.id().equals(newAgreementPdnDTO.id()));
    }

    public boolean isEqual(AgreementPdnDTO oldAgreementPdnDTO, AgreementPdnDTO newAgreementPdnDTO) {
        if (oldAgreementPdnDTO == null || newAgreementPdnDTO == null) {
            return false;
        }
        return (!(isNotEqual(oldAgreementPdnDTO.employeeId(), newAgreementPdnDTO.employeeId()) ||
                isNotEqual(oldAgreementPdnDTO.fileName(), newAgreementPdnDTO.fileName()) ||
                isNotEqual(oldAgreementPdnDTO.fileSize(), newAgreementPdnDTO.fileSize()) ||
                isNotEqual(oldAgreementPdnDTO.fileFormat(), newAgreementPdnDTO.fileFormat())));
    }

    private void savePassportTs(PassportTsDTO oldPassportTsDTO, PassportTsDTO passportTsDTO, UUID currentUser) {
        DocumentType documentType = documentTypeRepository.findById(DocumentCode.PASSPORT_TS).orElse(null);
        if (documentType == null) {
            log.warn("В настройках системы нет вида документа 'ПТС' (PASSPORT_TS)");
            log.warn("Информация о ПТС не сохранена");
            return;
        }
        if (passportTsDTO == null || isEqual(oldPassportTsDTO, passportTsDTO)) {
            return;
        }
        UUID id = (isNew(oldPassportTsDTO, passportTsDTO)) ? UUID.randomUUID() : passportTsDTO.id();

        var document = employeeDocumentRepository.findById(id).orElseGet(EmployeeDocument::new);
        document.setId(id);
        document.setDocumentType(documentType);
        document.setEmployeeId(passportTsDTO.employeeId());
        document.setCarId(passportTsDTO.carId());
        document.setFileName(passportTsDTO.fileName());
        document.setFileSize(passportTsDTO.fileSize());
        document.setFileFormat(passportTsDTO.fileFormat());
        document.updateAuthorInfo(currentUser);
        document.setColor(passportTsDTO.color());
        document.setPassengerSeatsCount(passportTsDTO.passengerSeatsCount());
        document.setVin(passportTsDTO.vin());
        document.setEngineVolume(passportTsDTO.engineVolume());
        document.setEnginePower(passportTsDTO.enginePower());
        document.setIssueDateDocument(LocalDateTime.now());
        document.setBrandName(passportTsDTO.brandName());
        document.setModel(passportTsDTO.model());
        employeeDocumentRepository.save(document);
    }

    public boolean isNew(PassportTsDTO oldPassportTsDTO, PassportTsDTO newPassportTsDTO) {
        // Если id документа не совпадает, то документ новый
        // Если id документа совпадает, то считаем, что это редактируется старый документ
        return (oldPassportTsDTO == null || oldPassportTsDTO.id() == null || !oldPassportTsDTO.id().equals(newPassportTsDTO.id()));
    }

    public boolean isEqual(PassportTsDTO oldPassportTsDTO, PassportTsDTO newPassportTsDTO) {
        if (oldPassportTsDTO == null || newPassportTsDTO == null) {
            return false;
        }
        return (!(isNotEqual(oldPassportTsDTO.employeeId(), newPassportTsDTO.employeeId()) ||
                isNotEqual(oldPassportTsDTO.fileName(), newPassportTsDTO.fileName()) ||
                isNotEqual(oldPassportTsDTO.fileSize(), newPassportTsDTO.fileSize()) ||
                isNotEqual(oldPassportTsDTO.fileFormat(), newPassportTsDTO.fileFormat()) ||
                isNotEqual(oldPassportTsDTO.color(), newPassportTsDTO.color()) ||
                isNotEqual(oldPassportTsDTO.passengerSeatsCount(), newPassportTsDTO.passengerSeatsCount()) ||
                isNotEqual(oldPassportTsDTO.vin(), newPassportTsDTO.vin()) ||
                isNotEqual(oldPassportTsDTO.engineVolume(), newPassportTsDTO.engineVolume()) ||
                isNotEqual(oldPassportTsDTO.enginePower(), newPassportTsDTO.enginePower())));
    }

    private void saveOsago(OsagoDTO oldOsagoDTO, OsagoDTO osagoDTO, UUID currentUser) {
        DocumentType documentType = documentTypeRepository.findById(DocumentCode.OSAGO).orElse(null);
        if (documentType == null) {
            log.warn("В настройках системы нет вида документа 'Полис ОСАГО' (OSAGO)");
            log.warn("Информация о полисе ОСАГО не сохранена");
            return;
        }
        if (osagoDTO == null || isEqual(oldOsagoDTO, osagoDTO)) {
            return;
        }
        UUID id = (isNew(oldOsagoDTO, osagoDTO)) ? UUID.randomUUID() : osagoDTO.id();

        var document = employeeDocumentRepository.findById(id).orElseGet(EmployeeDocument::new);
        document.setId(id);
        document.setDocumentType(documentType);
        document.setEmployeeId(osagoDTO.employeeId());
        document.setCarId(osagoDTO.carId());
        document.setFileName(osagoDTO.fileName());
        document.setFileSize(osagoDTO.fileSize());
        document.setFileFormat(osagoDTO.fileFormat());
        document.updateAuthorInfo(currentUser);
        document.setSeria(osagoDTO.seria());
        document.setNumber(osagoDTO.number());

        // На фронте нет даты выдачи документа - решили приравнять к дате начала действия
        document.setIssueDateDocument(osagoDTO.startTimeDocument());

        document.setStartTimeDocument(osagoDTO.startTimeDocument());
        document.setFinalTimeDocument(osagoDTO.finalTimeDocument());
        document.setRegistrationNumber(osagoDTO.registrationNumber());

        employeeDocumentRepository.save(document);
    }

    public boolean isNew(OsagoDTO oldOsago, OsagoDTO newOsago) {
        // Если id документа не совпадает, то документ точно новый
        if (oldOsago == null || oldOsago.id() == null || !oldOsago.id().equals(newOsago.id())) {
            return true;
        }
        // Если id документа совпадает, но поменялись одновременно дата начала действия, серия и номер, то считаем, что это новый документ
        // Если id документа совпадает, но изменения не значительны, то считаем, что это редактируется старый документ
        return (isNotEqual(newOsago.startTimeDocument(), oldOsago.startTimeDocument()) &&
                isNotEqual(newOsago.seria(), oldOsago.seria()) &&
                isNotEqual(newOsago.number(), oldOsago.number()));
    }

    public boolean isEqual(OsagoDTO oldOsago, OsagoDTO newOsago) {
        if (oldOsago == null || newOsago == null) {
            return false;
        }
        return (!(isNotEqual(oldOsago.employeeId(), newOsago.employeeId()) ||
                isNotEqual(oldOsago.carId(), newOsago.carId()) ||
                isNotEqual(oldOsago.fileName(), newOsago.fileName()) ||
                isNotEqual(oldOsago.fileSize(), newOsago.fileSize()) ||
                isNotEqual(oldOsago.fileFormat(), newOsago.fileFormat()) ||
                isNotEqual(oldOsago.seria(), newOsago.seria()) ||
                isNotEqual(oldOsago.number(), newOsago.number()) ||
                isNotEqual(oldOsago.startTimeDocument(), newOsago.startTimeDocument()) ||
                isNotEqual(oldOsago.finalTimeDocument(), newOsago.finalTimeDocument()) ||
                isNotEqual(oldOsago.registrationNumber(), newOsago.registrationNumber())));
    }

    private void saveMarriageCertificate(MarriageCertificateDTO oldMarriageCertificateDTO, MarriageCertificateDTO marriageCertificateDTO, UUID currentUser) {
        DocumentType documentType = documentTypeRepository.findById(DocumentCode.MARRIAGE_CERTIFICATE).orElse(null);
        if (documentType == null) {
            log.warn("В настройках системы нет вида документа 'Свидетельство о браке' (MARRIAGE_CERTIFICATE)");
            log.warn("Информация о свидетельстве о браке не сохранена");
            return;
        }
        if (marriageCertificateDTO == null || isEqual(oldMarriageCertificateDTO, marriageCertificateDTO)) {
            return;
        }
        UUID id = (isNew(oldMarriageCertificateDTO, marriageCertificateDTO)) ? UUID.randomUUID() : marriageCertificateDTO.id();

        var document = employeeDocumentRepository.findById(id).orElseGet(EmployeeDocument::new);
        document.setId(id);
        document.setDocumentType(documentType);
        document.setEmployeeId(marriageCertificateDTO.employeeId());
        document.setCarId(null); // свидетельство о браке связано с сотрудником, а не с автомобилем
        document.setFileName(marriageCertificateDTO.fileName());
        document.setFileSize(marriageCertificateDTO.fileSize());
        document.setFileFormat(marriageCertificateDTO.fileFormat());
        document.updateAuthorInfo(currentUser);
        document.setSeria(marriageCertificateDTO.seria());
        document.setNumber(marriageCertificateDTO.number());
        document.setIssueDateDocument(marriageCertificateDTO.issueDateDocument());

        employeeDocumentRepository.save(document);
    }

    public boolean isNew(MarriageCertificateDTO oldMarriageCertificateDTO, MarriageCertificateDTO newMarriageCertificateDTO) {
        // Если id документа не совпадает, то документ точно новый
        if (oldMarriageCertificateDTO == null || oldMarriageCertificateDTO.id() == null ||
                !oldMarriageCertificateDTO.id().equals(newMarriageCertificateDTO.id())) {
            return true;
        }
        // Если id документа совпадает, но поменялись одновременно дата выдачи, серия и номер, то считаем, что это новый документ
        // Если id документа совпадает, но изменения не значительны, то считаем, что это редактируется старый документ
        return (isNotEqual(newMarriageCertificateDTO.issueDateDocument(), oldMarriageCertificateDTO.issueDateDocument()) &&
                isNotEqual(newMarriageCertificateDTO.seria(), oldMarriageCertificateDTO.seria()) &&
                isNotEqual(newMarriageCertificateDTO.number(), oldMarriageCertificateDTO.number()));
    }

    public boolean isEqual(MarriageCertificateDTO oldMarriageCertificateDTO, MarriageCertificateDTO newMarriageCertificateDTO) {
        if (oldMarriageCertificateDTO == null || newMarriageCertificateDTO == null) {
            return false;
        }
        if (!(isNotEqual(oldMarriageCertificateDTO.employeeId(), newMarriageCertificateDTO.employeeId()) ||
                isNotEqual(oldMarriageCertificateDTO.fileName(), newMarriageCertificateDTO.fileName()) ||
                isNotEqual(oldMarriageCertificateDTO.fileSize(), newMarriageCertificateDTO.fileSize()) ||
                isNotEqual(oldMarriageCertificateDTO.fileFormat(), newMarriageCertificateDTO.fileFormat()) ||
                isNotEqual(oldMarriageCertificateDTO.seria(), newMarriageCertificateDTO.seria()) ||
                isNotEqual(oldMarriageCertificateDTO.number(), newMarriageCertificateDTO.number()) ||
                isNotEqual(oldMarriageCertificateDTO.issueDateDocument(), newMarriageCertificateDTO.issueDateDocument()))) {
            return true;
        }
        return false;
    }

    private void saveDriverLic(DriverLicDTO oldDriverLicDTO, DriverLicDTO driverLicDTO, UUID currentUser) {
        DocumentType documentType = documentTypeRepository.findById(DocumentCode.DRIVER_LIC).orElse(null);
        if (documentType == null) {
            log.warn("В настройках системы нет вида документа 'Водительское удостоверение' (DRIVER_LIC)");
            log.warn("Информация о водительском удостоверении не сохранена");
            return;
        }

        if ((driverLicDTO == null) || isEqual(oldDriverLicDTO, driverLicDTO)) {
            return;
        }
        UUID id = (isNew(oldDriverLicDTO, driverLicDTO)) ? UUID.randomUUID() : driverLicDTO.id();

        var document = employeeDocumentRepository.findById(id).orElseGet(EmployeeDocument::new);
        document.setId(id);
        document.setDocumentType(documentType);
        document.setEmployeeId(driverLicDTO.employeeId());
        document.setCarId(null); // водительское удостоверение связано с сотрудником, а не с автомобилем
        document.setFileName(driverLicDTO.fileName());
        document.setFileSize(driverLicDTO.fileSize());
        document.setFileFormat(driverLicDTO.fileFormat());
        document.updateAuthorInfo(currentUser);
        document.setSeria(driverLicDTO.seria());
        document.setNumber(driverLicDTO.number());
        document.setIssueDateDocument(driverLicDTO.issueDateDocument());
        document.setIssue(driverLicDTO.issue());
        document.setPlaceIssue(driverLicDTO.placeIssue());
        document.setStartTimeDocument(driverLicDTO.issueDateDocument());
        document.setFinalTimeDocument(driverLicDTO.finalTimeDocument());
        document.setCategoria(driverLicDTO.categoria());

        employeeDocumentRepository.save(document);
    }

    public boolean isNew(DriverLicDTO oldDriverLicDTO, DriverLicDTO newDriverLicDTO) {
        // Если id документа не совпадает, то документ точно новый
        if (oldDriverLicDTO == null || oldDriverLicDTO.id() == null || !oldDriverLicDTO.id().equals(newDriverLicDTO.id())) {
            return true;
        }

        // Если id документа совпадает, но поменялись одновременно дата выдачи, серия и номер, то считаем, что это новый документ
        // Если id документа совпадает, но изменения не значительны, то считаем, что это редактируется старый документ
        return (isNotEqual(newDriverLicDTO.issueDateDocument(), oldDriverLicDTO.issueDateDocument()) &&
                isNotEqual(newDriverLicDTO.seria(), oldDriverLicDTO.seria()) &&
                isNotEqual(newDriverLicDTO.number(), oldDriverLicDTO.number()));
    }

    private boolean isNotEqual(Object a, Object b) {
        return ((a != null && !a.equals(b)) || (b != null && !b.equals(a)));
    }

    public boolean isEqual(DriverLicDTO oldDriverLicDTO, DriverLicDTO newDriverLicDTO) {
        if (oldDriverLicDTO == null || newDriverLicDTO == null) {
            return false;
        }
        return (!(isNotEqual(oldDriverLicDTO.employeeId(), newDriverLicDTO.employeeId()) ||
                isNotEqual(oldDriverLicDTO.fileName(), newDriverLicDTO.fileName()) ||
                isNotEqual(oldDriverLicDTO.fileSize(), newDriverLicDTO.fileSize()) ||
                isNotEqual(oldDriverLicDTO.fileFormat(), newDriverLicDTO.fileFormat()) ||
                isNotEqual(oldDriverLicDTO.seria(), newDriverLicDTO.seria()) ||
                isNotEqual(oldDriverLicDTO.number(), newDriverLicDTO.number()) ||
                isNotEqual(oldDriverLicDTO.issueDateDocument(), newDriverLicDTO.issueDateDocument()) ||
                isNotEqual(oldDriverLicDTO.issue(), newDriverLicDTO.issue()) ||
                isNotEqual(oldDriverLicDTO.placeIssue(), newDriverLicDTO.placeIssue()) ||
                isNotEqual(oldDriverLicDTO.finalTimeDocument(), newDriverLicDTO.finalTimeDocument()) ||
                isNotEqual(oldDriverLicDTO.categoria(), newDriverLicDTO.categoria())));
    }

    @Override
    public PersonalCarDTO getPersonalCarByIdAndEmployeeId(UUID carId, UUID employeeId) {
        var personalCarDTO = mapper.personalCarToDTO(repository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException(PersonalCar.class, carId)));

        enrichWithDocumentsInfo(List.of(personalCarDTO));

        return personalCarDTO;
    }

    private void enrichWithDocumentsInfo(List<PersonalCarDTO> personalCarDTOList) {

        if (personalCarDTOList == null || personalCarDTOList.isEmpty()) {
            return;
        }

        UUID employeeId = personalCarDTOList.get(0).getEmployeeId();

        List<EmployeeDocument> employeeDocuments = employeeDocumentRepository.findAllByEmployeeId(employeeId);

        // DRIVER_LIC - Водительское удостоверение
        DriverLicDTO driverLic =
                mapper.EmployeeDocumentToDriverLicDTO(
                        getActualDocument(employeeDocuments, DocumentCode.DRIVER_LIC));

        // MARRIAGE_CERTIFICATE - Свидетельство о браке
        MarriageCertificateDTO marriageCertificate =
                mapper.EmployeeDocumentToMarriageCertificateDTO(
                        getActualDocument(employeeDocuments, DocumentCode.MARRIAGE_CERTIFICATE));

        for (var personalCarDTO : personalCarDTOList) {
            UUID carId = personalCarDTO.getId();

            // OSAGO - Полис ОСАГО
            OsagoDTO osago =
                    mapper.EmployeeDocumentToOsagoDTO(
                            getActualDocument(employeeDocuments, DocumentCode.OSAGO, carId));

            // PASSPORT_TS - ПТС
            PassportTsDTO passportTs =
                    mapper.EmployeeDocumentToPassportTsDTO(
                            getActualDocument(employeeDocuments, DocumentCode.PASSPORT_TS, carId));

            // AGREEMENT_PDN - Согласие на обработку ПДН
            AgreementPdnDTO agreementPdn = mapper.EmployeeDocumentToAgreementPdnDTO(
                    getActualDocument(employeeDocuments, DocumentCode.AGREEMENT_PDN, carId));

            personalCarDTO.setDocuments(new DocumentsDTO(driverLic, osago, passportTs, marriageCertificate, agreementPdn));

            // Заполним insuranceNumber в старой структуре для обратной совместимости
            if (osago != null && (osago.seria() != null || osago.number() != null)) {
                StringBuilder insuranceNumber = new StringBuilder();
                if (osago.seria() != null && !osago.seria().isEmpty()) {
                    insuranceNumber.append(osago.seria());
                }
                if (osago.number() != null && !osago.number().isEmpty()) {
                    if (insuranceNumber.length() > 0) {
                        insuranceNumber.append(" ");
                    }
                    insuranceNumber.append(osago.number());
                }
                personalCarDTO.setInsuranceNumber(insuranceNumber.toString());
            }

            // Заполним регистрационный номер для обратной совместимости
            if (osago != null && osago.registrationNumber() != null && !osago.registrationNumber().isEmpty()) {
                personalCarDTO.setRegistrationNumber(osago.registrationNumber());
            }

            // Заполним color для обратной совместимости
            if (passportTs != null && passportTs.color() != null && !passportTs.color().isEmpty()) {
                personalCarDTO.setColor(passportTs.color());
            }

            // Заполним passengerSeatsCount для обратной совместимости
            if (passportTs != null && passportTs.passengerSeatsCount() != null) {
                personalCarDTO.setPassengerSeatsCount(
                        (short) java.lang.Math.min(passportTs.passengerSeatsCount(), Short.MAX_VALUE)
                );
            }

            // Заполним марку автомобиля для обратной совместимости
            if (passportTs != null && passportTs.brandName() != null && !passportTs.brandName().isEmpty()) {
                personalCarDTO.setBrandName(passportTs.brandName());
            }

            // Заполним модель автомобиля для обратной совместимости
            if (passportTs != null && passportTs.model() != null && !passportTs.model().isEmpty()) {
                personalCarDTO.setModel(passportTs.model());
            }
        }
    }

    private EmployeeDocument getActualDocument(List<EmployeeDocument> employeeDocuments, DocumentCode documentCode) {
        return getActualDocument(employeeDocuments, documentCode, null);
    }

    private EmployeeDocument getActualDocument(List<EmployeeDocument> employeeDocuments, DocumentCode documentCode, UUID carId) {
        if (employeeDocuments == null || employeeDocuments.isEmpty()) {
            return null;
        }
        return employeeDocuments.stream()
                .filter(doc -> (carId == null || carId.equals(doc.getCarId())) && doc.getDocumentType().getDocumentCode().equals(documentCode))
                .sorted((o1, o2) -> o2.getIssueDateDocument().compareTo(o1.getIssueDateDocument()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void deletePersonalAuto(@NotNull UUID id) {
        activePersonalCarRepository.deleteByPersonalCarId(id);
        repository.deleteById(id);
    }

    @Override
    public PersonalCarDTO updatePersonalAuto(@NotNull PersonalCarDTO source, UUID currentUser) {
        var updatedAuto = mapper.dtoToPersonalCar(source);
        personalCarEnrichmentService.addUserAcceptInfo(updatedAuto);
        var personalCarToDTO = mapper.personalCarToDTO(repository.save(updatedAuto));
        saveDocuments(source, personalCarToDTO.getId(), currentUser);
        enrichWithDocumentsInfo(List.of(personalCarToDTO));
        return personalCarToDTO;
    }

    @Override
    public List<PersonalCarDTO> getPersonalCarsByUser(UUID employeeId) {
        var personalCarDTOs = repository.findAllByEmployeeId(employeeId).stream().map(mapper::personalCarToDTO).toList();
        enrichWithDocumentsInfo(personalCarDTOs);
        return personalCarDTOs;
    }

    public DriverLicDTO fillEmployeeId(DriverLicDTO driverLicDTO, UUID employeeId) {
        DriverLicDTO result = driverLicDTO == null || driverLicDTO.employeeId() != null ?
                driverLicDTO : driverLicDTO.toBuilder().employeeId(employeeId).build();
        return result;
    }

    public MarriageCertificateDTO fillEmployeeId(MarriageCertificateDTO marriageCertificateDTO, UUID employeeId) {
        MarriageCertificateDTO result = marriageCertificateDTO == null || marriageCertificateDTO.employeeId() != null ?
                marriageCertificateDTO : marriageCertificateDTO.toBuilder().employeeId(employeeId).build();
        return result;
    }

    public OsagoDTO fillEmployeeId(OsagoDTO osagoDTO, UUID employeeId) {
        OsagoDTO result = osagoDTO == null || osagoDTO.employeeId() != null ?
                osagoDTO : osagoDTO.toBuilder().employeeId(employeeId).build();
        return result;
    }

    public OsagoDTO fillCarId(OsagoDTO osagoDTO, UUID carId) {
        OsagoDTO result = osagoDTO == null || osagoDTO.carId() != null ?
                osagoDTO : osagoDTO.toBuilder().carId(carId).build();
        return result;
    }

    public PassportTsDTO fillEmployeeId(PassportTsDTO passportTsDTO, UUID employeeId) {
        PassportTsDTO result = passportTsDTO == null || passportTsDTO.employeeId() != null ?
                passportTsDTO : passportTsDTO.toBuilder().employeeId(employeeId).build();
        return result;
    }

    public PassportTsDTO fillCarId(PassportTsDTO passportTsDTO, UUID carId) {
        PassportTsDTO result = passportTsDTO == null || passportTsDTO.carId() != null ?
                passportTsDTO : passportTsDTO.toBuilder().carId(carId).build();
        return result;
    }

    public AgreementPdnDTO fillEmployeeId(AgreementPdnDTO agreementPdnDTO, UUID employeeId) {
        AgreementPdnDTO result = agreementPdnDTO == null || agreementPdnDTO.employeeId() != null ?
                agreementPdnDTO : agreementPdnDTO.toBuilder().employeeId(employeeId).build();
        return result;
    }

    public AgreementPdnDTO fillCarId(AgreementPdnDTO agreementPdnDTO, UUID carId) {
        AgreementPdnDTO result = agreementPdnDTO == null || agreementPdnDTO.carId() != null ?
                agreementPdnDTO : agreementPdnDTO.toBuilder().carId(carId).build();
        return result;
    }
}
