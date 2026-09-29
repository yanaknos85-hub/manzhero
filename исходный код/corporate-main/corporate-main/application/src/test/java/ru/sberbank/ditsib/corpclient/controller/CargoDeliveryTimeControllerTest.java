package ru.sberbank.ditsib.corpclient.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sberbank.ditsib.corpclient.database.dao.CargoDeliveryTimeRepository;
import ru.sberbank.ditsib.corpclient.database.model.CargoDeliveryTime;
import ru.sberbank.ditsib.corpclient.database.model.CargoDeliveryTimeUrgency;
import ru.sberbank.ditsib.corpclient.dto.cargo.CargoDeliveryTimeDto;
import ru.sberbank.ditsib.corpclient.shared.SharedData;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.CargoDeliveryTimeMessage;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unused")
@UnitTest
@IsolatedTest
@Feature("app_platform_corporate")
@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера сроков доставки")
@Transactional
class CargoDeliveryTimeControllerTest extends SharedData {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CargoDeliveryTimeRepository repository;

    @MockitoBean(name = "cargoDeliveryTimeOutput")
    private OutputBridge cargoDeliveryTimeOutput;

    @MockitoBean
    private AuthorizationManager<?> roleCheckService;

    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }

    @Test
    @DisplayName("Изменение")
    void update() throws Exception {
        CargoDeliveryTime deliveryTime = repository.save(createDefault("new delivery time"));

        CargoDeliveryTimeDto updateDTO = CargoDeliveryTimeDto.builder()
            .id(deliveryTime.getId())
            .label(deliveryTime.getLabel())
            .start(deliveryTime.getStart())
            .end(deliveryTime.getEnd())
            .urgency(deliveryTime.getUrgency())
            .defaultValue(deliveryTime.getDefaultValue())
            .value(999999)
            .build();

        var request = objectMapper.writeValueAsString(Collections.singletonList(updateDTO));
        mockMvc.perform(
                post("/cargo/deliverytime")
                    .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER")))
                    .contentType(MediaType.APPLICATION_JSON).content(request)).
            andExpect(status().isOk()).andReturn();

        var editedDeliveryTime = repository.findCargoDeliveryTimeByLabel("new delivery time").orElseThrow();
        assertNotEquals(updateDTO.getId(), editedDeliveryTime.getId());
        assertEquals(updateDTO.getValue(), editedDeliveryTime.getValue());


        var messages = getMessages(cargoDeliveryTimeOutput, 2, CargoDeliveryTimeMessage.class);

        var deleteMessage = messages.getFirst();
        assertTrue(deleteMessage.isDeleted());
        assertEquals(deleteMessage.getId(), deliveryTime.getId());

        var addMessage = messages.get(1);
        assertFalse(addMessage.isDeleted());
        assertEquals(addMessage.getId(), editedDeliveryTime.getId());
        assertEquals(999999, addMessage.getValue());
    }

    @Test
    @DisplayName("Получение всех сроков доставки")
    void getAll() throws Exception {
        repository.save(createDefault("delivery time 1"));

        var result = mockMvc.perform(get("/cargo/deliverytime")
                .with(jwt().jwt(builder -> builder.jti(USER1_ID)).authorities(new SimpleGrantedAuthority("ROLE_USER"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(8))
            .andReturn();

        List<Map<String, Object>> objectMap =
            new ObjectMapper().readValue(result.getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeReference<>() {
                });

        for (Map<String, Object> map : objectMap) {
            UUID id = UUID.fromString(String.valueOf(map.get("id")));
            CargoDeliveryTime expected = repository.findById(id).orElseThrow();
            assertThat(map).containsEntry("label", expected.getLabel());
        }
    }

    private CargoDeliveryTime createDefault(String label) {
        return CargoDeliveryTime.builder()
            .label(label)
            .start(0)
            .end(100)
            .defaultValue(10)
            .value(10)
            .urgency(CargoDeliveryTimeUrgency.STANDART)
            .build();
    }
}
