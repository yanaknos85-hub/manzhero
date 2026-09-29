package ru.sberbank.ditsib.corpclient.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.client.inject.GrpcClientBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import ru.sber.transport.files.grpc.exchange.Deleter;
import ru.sber.transport.files.grpc.exchange.Downloader;
import ru.sber.transport.files.grpc.exchange.Uploader;
import ru.sber.transport.files.grpc.model.FileMeta;
import ru.sber.transport.files.grpc.model.Model;
import ru.sber.transport.files.grpc.service.DeleteServiceGrpc;
import ru.sber.transport.files.grpc.service.DownloadServiceGrpc;
import ru.sber.transport.files.grpc.service.UploadServiceGrpc;
import ru.sberbank.ditsib.corpclient.service.FileService;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of file service.
 */
@Component
@Slf4j
@RequiredArgsConstructor
class FileServiceImpl implements FileService {

    private final ResourceLoader resourceLoader;
    private final Deleter deleter;
    private final Downloader downloader;
    private final Uploader uploader;

    @Setter
    @Value("${spring.application.name}")
    private String appName;

    @Override
    public Resource getFromResource(String fileName) {
        return resourceLoader.getResource(fileName);
    }

    @Override
    public void upload(String fileName, InputStream stream, String contentType, long length) throws IOException {
        try (stream) {
            uploader.upload(Model.MetaRequest.Type.REGULAR, stream, fileName, contentType, length);
        }
    }

    @Override
    public FileMeta meta(String fileName) {
        return downloader.meta(Model.MetaRequest.Type.REGULAR, fileName);
    }

    @Override
    public byte[] download(FileMeta meta) {
        var result = new byte[(int) meta.size()];
        var downloaded = 0;
        while (downloaded < meta.size()) {
            var bytes = downloader.download(meta, downloaded, downloaded + DataSize.ofMegabytes(3).toBytes());
            System.arraycopy(bytes, 0, result, downloaded, bytes.length);
            downloaded += bytes.length;
        }
        return result;
    }

    @Override
    public byte[] download(FileMeta meta, int start, int end) {
        return downloader.download(meta, start, end);
    }

    @Override
    public void delete(String fileName) {
        deleter.invoke(Model.MetaRequest.Type.REGULAR, fileName);
    }
}
