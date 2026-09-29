package ru.sber.transport.tariff_fleet.controller.impl;

import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.sber.transport.tariff_fleet.TestData.EMPLOYEE_1_ID;
import static ru.sber.transport.tariff_fleet.constant.Role.ROLE_ADMIN_CORP_CLIENT;

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedPostgres
@DisplayName("Проверка контроллера по работе с точками обслуживания")
class ServicePointControllerImplTest {
    
    public static final String SERVICE_POINT_URL = "/service-points";
    
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;
    
    @Test
    @SneakyThrows
    @DisplayName("Проверка загрузки точек обслуживания")
    void validateUploadServicePoints() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var excelFile = new MockMultipartFile(
                "file",
                "service_points.xlsx",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                Files.readAllBytes(new ClassPathResource("/excel/service_points.xlsx").getFile().toPath())
        );
        mockMvc.perform(multipart(SERVICE_POINT_URL)
                                .part(new MockPart("documentType", "REPAIR_AND_MAINTENANCE".getBytes(StandardCharsets.UTF_8)))
                                .file(excelFile)
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.servicePoints[*].number", Matchers.contains(1, 2, 3)))
               .andExpect(jsonPath("$.servicePoints[*].address", Matchers.contains("улица Пушкина", "дом Колотушкина", "адрес")))
               .andExpect(jsonPath("$.servicePoints[*].latitude", Matchers.contains(1.111, 2.111, 2.111)))
               .andExpect(jsonPath("$.servicePoints[*].longitude", Matchers.contains(1.111, 1.111, 1.111)))
               .andExpect(jsonPath("$.servicePoints[*].error", Matchers.contains(null, null, "Дубликат строки 2 по широте и долготе")))
               .andExpect(jsonPath("$.file").isNotEmpty());
    }
    
    @Test
    @SneakyThrows
    @DisplayName("Проверка загрузки файла шаблона для точек обслуживания")
    void downloadServicePointsTemplate() {
        byte[] expectedBytes = Files.readAllBytes(new ClassPathResource("/excel/upload_service_points_template.xlsx").getFile().toPath());
        mockMvc.perform(get(SERVICE_POINT_URL + "/file")
                                .with(jwt().jwt(builder -> builder.jti(EMPLOYEE_1_ID.toString()))
                                           .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                .accept(MediaType.APPLICATION_OCTET_STREAM))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }
    
}
