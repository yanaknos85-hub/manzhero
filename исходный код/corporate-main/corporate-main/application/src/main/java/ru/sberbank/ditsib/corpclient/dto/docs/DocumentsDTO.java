package ru.sberbank.ditsib.corpclient.dto.docs;

public record DocumentsDTO(
    // DRIVER_LIC - Водительское удостоверение
    DriverLicDTO driverLic,

    // OSAGO - Полис ОСАГО
    OsagoDTO osago,

    // PASSPORT_TS - ПТС
    PassportTsDTO passportTs,

    //MARRIAGE_CERTIFICATE - Свидетельство о браке
    MarriageCertificateDTO marriageCertificate,

    // AGREEMENT_PDN - Согласие на обработку ПДН
    AgreementPdnDTO agreementPdn)
{}
