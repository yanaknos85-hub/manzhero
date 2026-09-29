package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.corpclient.database.model.PersonalCar;
import ru.sberbank.ditsib.corpclient.service.AcceptFileService;
import ru.sberbank.ditsib.corpclient.service.PersonalCarEnrichmentService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalCarEnrichmentServiceImpl implements PersonalCarEnrichmentService {

    private final AcceptFileService acceptFileService;

    @Override
    public void addUserAcceptInfo(PersonalCar personalCar) {
        personalCar.setPersDataAccept(acceptFileService.getAgreementTextHash());
    }
}