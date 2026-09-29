package ru.sberbank.ditsib.corpclient.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EmbeddedPostgres(liquibase = "../providers-database/src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TripPurposeTest {

    public static final String CONTROLLER_URL = "/%s/purposes";
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;

    @SneakyThrows
    @Test
    @Sql({"/scripts/db_cleanup.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/trip_purpose.sql"})
    void getTripPurposesByEmployee() {
        AuthorizeUtils.authorize(manager, "ROLE_ADMIN_CORP_CLIENT");
        mockMvc.perform(get(CONTROLLER_URL.formatted("cb9f17e7-f658-43ca-a70f-40c1c93ad0a6") + "/search_by_employee")
                        .with(jwt().jwt(builder -> builder.jti(UUID.fromString("3cd35c19-fd39-413c-99a0-30f35bd642a8").toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_CORP_CLIENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(37))
                .andExpect(jsonPath("$.[0].id").value("274252ef-ab86-402b-990c-f12baea7928b"))
                .andExpect(jsonPath("$.[0].label").value("Встречи с контрагентами"))
                .andExpect(jsonPath("$.[0].purposeType").value("CORPORATE"))
                .andExpect(jsonPath("$.[0].tripPurposeAttributes").isEmpty())
                .andExpect(jsonPath("$.[0].tripPurposeDepartments").isEmpty())
                .andExpect(jsonPath("$.[0].tripPurposeDates").isEmpty())
                .andExpect(jsonPath("$.[0].tripPurposeTimes").isEmpty())
                .andExpect(jsonPath("$.[0].tripPurposeWeekdays").isEmpty())
                .andExpect(jsonPath("$.[0].icon").value("PURPOSE_M_CONTR"))

                .andExpect(jsonPath("$.[1].id").value("1a35bc75-4d83-472d-a02f-424057894c9a"))
                .andExpect(jsonPath("$.[1].label").value("Доставка работников в ночное время 19-03"))
                .andExpect(jsonPath("$.[1].purposeType").value("CORPORATE"))
                .andExpect(jsonPath("$.[1].tripPurposeAttributes").isEmpty())
                .andExpect(jsonPath("$.[1].tripPurposeDepartments").isEmpty())
                .andExpect(jsonPath("$.[1].tripPurposeDates").isEmpty())
                .andExpect(jsonPath("$.[1].tripPurposeTimes[0].startTime").value("2022-11-17T19:00:00"))
                .andExpect(jsonPath("$.[1].tripPurposeTimes[0].endTime").value("2022-11-17T03:00:00"))
                .andExpect(jsonPath("$.[1].tripPurposeWeekdays").isEmpty())

                .andExpect(jsonPath("$.[2].id").value("dac2ed14-27f1-11eb-9a28-305a3a7d9fe6"))
                .andExpect(jsonPath("$.[2].label").value("Выезды в государственные органы"))
                .andExpect(jsonPath("$.[2].purposeType").value("CORPORATE"))
                .andExpect(jsonPath("$.[2].tripPurposeAttributes").isEmpty())
                .andExpect(jsonPath("$.[2].tripPurposeDepartments").isEmpty())
                .andExpect(jsonPath("$.[2].tripPurposeDates").isEmpty())
                .andExpect(jsonPath("$.[2].tripPurposeTimes").isEmpty())
                .andExpect(jsonPath("$.[2].tripPurposeWeekdays").isEmpty())
                .andExpect(jsonPath("$.[2].icon").value("PURPOSE_V_GOV"))

                .andExpect(jsonPath("$.[3].id").value("dac2ed27-27f1-11eb-9a28-305a3a7d9fe6"))
                .andExpect(jsonPath("$.[3].label").value("Выезды для участия в коллегиальных органах/рабочих группах"))
                .andExpect(jsonPath("$.[3].purposeType").value("CORPORATE"))
                .andExpect(jsonPath("$.[3].tripPurposeAttributes").isEmpty())
                .andExpect(jsonPath("$.[3].tripPurposeDepartments[0].department.id").value("489a0090-1819-4c60-a611-572ea115c6a4"))
                .andExpect(jsonPath("$.[3].tripPurposeDepartments[0].department.code").value("4"))
                .andExpect(jsonPath("$.[3].tripPurposeDepartments[0].department.location").value("Костромская область"))
                .andExpect(jsonPath("$.[3].tripPurposeDates").isEmpty())
                .andExpect(jsonPath("$.[3].tripPurposeTimes").isEmpty())
                .andExpect(jsonPath("$.[3].tripPurposeWeekdays").isEmpty())
                .andExpect(jsonPath("$.[3].icon").value("PURPOSE_V_GROUPS"))

                .andExpect(jsonPath("$.[4].id").value("c12209e9-4b7b-423d-aae0-29eab6a33c07"))
                .andExpect(jsonPath("$.[4].label").value("Доставка подменного фонда работников в ВСП и обратно"))
                .andExpect(jsonPath("$.[4].purposeType").value("CORPORATE"))
                .andExpect(jsonPath("$.[4].tripPurposeAttributes").isEmpty())
                .andExpect(jsonPath("$.[4].tripPurposeDepartments").isEmpty())
                .andExpect(jsonPath("$.[4].tripPurposeDates[0].startDate").value("2025-10-09T10:22:24.232"))
                .andExpect(jsonPath("$.[4].tripPurposeDates[0].endDate").value("2025-10-10T10:22:24.232"))
                .andExpect(jsonPath("$.[4].tripPurposeTimes").isEmpty())
                .andExpect(jsonPath("$.[4].tripPurposeWeekdays[0].weekday").value("TUESDAY"))
                .andExpect(jsonPath("$.[4].tripPurposeWeekdays[1].weekday").value("WEDNESDAY"))
                .andExpect(jsonPath("$.[4].tripPurposeWeekdays[2].weekday").value("THURSDAY"))
                .andExpect(jsonPath("$.[4].icon").value("PURPOSE_D_VSP"));

    }
}
