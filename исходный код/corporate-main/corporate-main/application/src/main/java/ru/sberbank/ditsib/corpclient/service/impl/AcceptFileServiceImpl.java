package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.corpclient.service.AcceptFileService;
import ru.sberbank.ditsib.corpclient.service.FileService;

import java.io.IOException;


/**
 * Implementation of service for working with addresses.
 */
@RequiredArgsConstructor
@Service
@Slf4j
class AcceptFileServiceImpl implements AcceptFileService {

    private final FileService fileService;

    private static final String FILE_NAME = "classpath:file/agreement.txt";
    private String currentAgreementHash;

    @Override
    public String getAgreementTextHash() {
        if (StringUtils.isBlank(currentAgreementHash)) {
            calculateMd5HashForFile();
        }
        return currentAgreementHash;
    }

    private void calculateMd5HashForFile() {
        try (var is = fileService.getFromResource(FILE_NAME).getInputStream()) {
            currentAgreementHash = DigestUtils.md5Hex(is);
        } catch (IOException e) {
            log.error("Calculation Md5 Hash For File agreement failed ", e);
        }
    }
}
