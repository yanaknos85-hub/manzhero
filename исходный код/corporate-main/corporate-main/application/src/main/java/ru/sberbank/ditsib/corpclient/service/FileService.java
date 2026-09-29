package ru.sberbank.ditsib.corpclient.service;

import org.springframework.core.io.Resource;
import ru.sber.transport.files.grpc.model.FileMeta;
import ru.sberbank.ditsib.corpclient.dto.FileData;
import ru.sberbank.ditsib.corpclient.database.model.Organization;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Сервис по работе с файлами.
 */
public interface FileService {

    /**
     * Получение файла из ресурсов.
     *
     * @param fileName имя файла.
     * @return данные ресурса.
     * @throws IOException чтение файла завершилось с ошибкой.
     */
    Resource getFromResource(String fileName) throws IOException;

    void upload(String fileName, InputStream stream, String contentType, long length) throws IOException;

    FileMeta meta(String fileName);

    byte[] download(FileMeta meta);

    byte[] download(FileMeta meta, int start, int end);

    void delete(String fileName);
}
