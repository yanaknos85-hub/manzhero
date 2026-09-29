package ru.sber.transport.tariff_fleet.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.tariff_fleet.dto.EdfOperatorDto;
import ru.sber.transport.tariff_fleet.service.EdfOperatorService;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static ru.sber.transport.tariff_fleet.constant.Role.ROLE_ADMIN_CORP_CLIENT;

@SpringBootTest
@EmbeddedPostgres
@AutoConfigureMockMvc
class EdfOperatorControllerImplTest {
    public static final String CONTROLLER_URL = "/edf-operators";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AuthorizationManager<?> manager;
    @MockitoBean
    private EdfOperatorService edfOperatorService;
    
    @SneakyThrows
    @Test
    @Sql("/scripts/basic_corp_structure.sql")
    void getAllActive() {
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
        var edfOperator1 = Instancio.create(EdfOperatorDto.class);
        var edfOperator2 = Instancio.create(EdfOperatorDto.class);
        var edfOperator3 = Instancio.create(EdfOperatorDto.class);
        var expected = Set.of(edfOperator1, edfOperator2, edfOperator3);
        doReturn(expected).when(edfOperatorService).getAllActive();
        var response = mockMvc.perform(get(CONTROLLER_URL)
                                               .with(jwt().jwt(builder -> builder.jti(
                                                                  UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                                          .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                                               .contentType(MediaType.APPLICATION_JSON_VALUE))
                              .andReturn()
                              .getResponse();
        var actual = objectMapper.readValue(response.getContentAsString(StandardCharsets.UTF_8),
                                            new TypeReference<Set<EdfOperatorDto>>() {
                                            });
        assertThat(actual).usingRecursiveComparison()
                          .isEqualTo(expected);
        verify(edfOperatorService).getAllActive();
    }
}