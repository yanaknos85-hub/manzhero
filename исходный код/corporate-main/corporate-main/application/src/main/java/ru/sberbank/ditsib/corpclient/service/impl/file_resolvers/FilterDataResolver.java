package ru.sberbank.ditsib.corpclient.service.impl.file_resolvers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.lang.NonNull;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sberbank.ditsib.corpclient.dto.FileExportFilterDTO;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Резолвер данных с получением фильтров.
 *
 * @param <T> Тип данных
 */
@Transactional
@RequiredArgsConstructor
public abstract class FilterDataResolver<T> implements DataExporter<T> {

    protected final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public @NonNull List<T> exportData(Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
        if (!parameters.containsKey("filters")) {
            return List.of();
        }

        var encodedFilters = parameters.get("filters").toString();
        var stringFilters = new String(Base64.getDecoder().decode(encodedFilters), StandardCharsets.UTF_8);
        var filters = objectMapper.readValue(stringFilters, FileExportFilterDTO.class);

        return exportFilteredData(filters, parameters, authentication);
    }

    /// Экспорт отфильтрованных данных
    /// @param filter Фильтр
     /// @param parameters Параметры
     /// @param authentication Токен авторизации
    protected abstract List<T> exportFilteredData(FileExportFilterDTO filter, Map<String, ?> parameters,
                                                  JwtAuthenticationToken authentication);
}
