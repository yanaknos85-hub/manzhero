package ru.sberbank.ditsib.corpclient.service;

/**
 * Service for user agreement text
 */
public interface AcceptFileService {
    
    /**
     * Get MD5 hash for actual version of user agreement text
     *
     * @return MD5 hash for actual version of user agreement text
     */
    String getAgreementTextHash();

}
