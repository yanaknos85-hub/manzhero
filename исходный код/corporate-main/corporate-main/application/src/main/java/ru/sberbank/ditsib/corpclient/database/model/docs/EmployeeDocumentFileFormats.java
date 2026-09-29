package ru.sberbank.ditsib.corpclient.database.model.docs;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum EmployeeDocumentFileFormats {
    JPG(MediaType.IMAGE_JPEG_VALUE, "jpg"),
    JPEG(MediaType.IMAGE_JPEG_VALUE, "jpeg"),
    GIF(MediaType.IMAGE_GIF_VALUE, "gif"),
    PNG(MediaType.IMAGE_PNG_VALUE, "png"),
    TIF("image/tiff", "tif"),
    TIFF("image/tiff", "tiff"),
    PDF(MediaType.APPLICATION_PDF_VALUE, "pdf"),
    HEIC("image/heic", "heic"),
    HEIF("image/heif", "heif");

    private final String mediaType;
    private final String fileFormat;

    /**
     * @param   fileFormat Расширение файла
     * @return  медиа-тип соответствующий расширению файла, если мобильное устройство способно его отобразить,
     *          иначе пусто
     *          image/tiff для TIFF -   Tagged Image File Format - формат файлов для цифровых изображений
     *          image/heic для HEIC -   High Efficiency Image Coding - современный формат изображений, используется,
     *                                  в частности, в iOS
     *          image/heif для HEIF -   High Efficiency Image File Format -  формат файлов для хранения отдельных
     *                                  изображений или их последовательностей, используется в iOS, Android, Windows, Canon
     */
    public static Optional<EmployeeDocumentFileFormats> getByFileFormat(String fileFormat) {
        for (EmployeeDocumentFileFormats value : EmployeeDocumentFileFormats.values()) {
            if (value.getFileFormat().equals(fileFormat)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
