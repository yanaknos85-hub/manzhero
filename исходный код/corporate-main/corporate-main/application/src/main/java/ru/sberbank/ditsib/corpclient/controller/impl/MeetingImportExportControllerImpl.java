package ru.sberbank.ditsib.corpclient.controller.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import ru.sberbank.ditsib.corpclient.controller.ForwardRequest;
import ru.sberbank.ditsib.corpclient.controller.MeetingImportExportController;

@RequiredArgsConstructor
@RestController
public class MeetingImportExportControllerImpl
        implements MeetingImportExportController, ForwardRequest {

    @Getter
    private final RestTemplate restTemplate;

    @Setter
    @Value("${addresses.host}")
    private String addressesHost;

    @Override
    public ResponseEntity<byte[]> exportFile(HttpServletRequest request) {
        return forwardRequest(request, "%s/files/meeting".formatted(addressesHost));
    }

    @Override
    public ResponseEntity<byte[]> exportFileWithName(HttpServletRequest request) {
        return forwardRequest(request, "%s/files/meeting".formatted(addressesHost));
    }

    @Override
    public ResponseEntity<byte[]> exportEmptyBook(HttpServletRequest request) {
        return forwardRequest(request, "%s/files/meeting".formatted(addressesHost));
    }

    @Override
    public ResponseEntity<byte[]> exportBook(HttpServletRequest request) {
        return forwardRequest(request, "%s/files/meeting".formatted(addressesHost));
    }

    @Override
    public ResponseEntity<byte[]> importFile(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        return forwardRequest(request, "%s/files/meeting".formatted(addressesHost), file);
    }

    @Override
    public ResponseEntity<byte[]> getResult(HttpServletRequest request) {
        return forwardRequest(request, "%s/files/meeting".formatted(addressesHost));
    }
}
