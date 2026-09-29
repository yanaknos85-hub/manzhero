package ru.sberbank.ditsib.transport.approvals.integration;

import lombok.SneakyThrows;
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
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@EmbeddedPostgres
@AutoConfigureMockMvc
class SearchApprovalsTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthorizationManager<?> manager;

    @Test
    @Sql(scripts = {"/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/trip_purpose.sql",
            "/scripts/approvers.sql",
            "/scripts/approvals.sql",
            "/scripts/fraud.sql"})
    @SneakyThrows
    void getApprovals() {
        AuthorizeUtils.authorize(manager, "ROLE_ADMIN_CORP_CLIENT");
        mockMvc.perform(get("/?status=CANCELLED&page=0&size=10")
                        .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_CORP_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.numberOfElements").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.sort[0].direction").value("DESC"))
                .andExpect(jsonPath("$.sort[0].property").value("creationTime"))
                .andExpect(jsonPath("$.sort[0].ignoreCase").value(false))
                .andExpect(jsonPath("$.sort[0].nullHandling").value("NATIVE"))
                .andExpect(jsonPath("$.sort[0].descending").value(true))
                .andExpect(jsonPath("$.sort[0].ascending").value(false))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10))
                .andExpect(jsonPath("$.pageable.offset").value(0))
                .andExpect(jsonPath("$.pageable.unpaged").value(false))
                .andExpect(jsonPath("$.pageable.paged").value(true))
                .andExpect(jsonPath("$.content[0].id").value("357e5e52-150d-4bcb-875c-fa3d8e7e9025"))
                .andExpect(jsonPath("$.content[0].passenger.id").value("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                .andExpect(jsonPath("$.content[0].passenger.firstName").value("Петр"))
                .andExpect(jsonPath("$.content[0].passenger.lastName").value("Петров"))
                .andExpect(jsonPath("$.content[0].passenger.personnelNumber").value("2016497"))
                .andExpect(jsonPath("$.content[0].passenger.departmentId").value("482e6dcb-03a9-4927-90b4-c7081114a9d8"))
                .andExpect(jsonPath("$.content[0].passenger.userId").value("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                .andExpect(jsonPath("$.content[0].requestId").value("b3344762-0b7a-4659-9e03-bf240a606e69"))
                .andExpect(jsonPath("$.content[0].transportType").value("PERSONAL"))
                .andExpect(jsonPath("$.content[0].taxiClass").value("COMFORT"))
                .andExpect(jsonPath("$.content[0].desiredDate").value("2025-08-14T08:34:00"))
                .andExpect(jsonPath("$.content[0].purposeId").value("b1d10a0b-117d-46fd-832a-5c3022df6c9e"))
                .andExpect(jsonPath("$.content[0].purposeLabel").value("Первая V 1.1"))
                .andExpect(jsonPath("$.content[0].cost").value(50000.0))
                .andExpect(jsonPath("$.content[0].status").value("CANCELLED"))
                .andExpect(jsonPath("$.content[0].waypoints[0].id").value("5aec799c-37b3-48be-84bb-c350edd6f207"))
                .andExpect(jsonPath("$.content[0].waypoints[0].country").value("Россия"))
                .andExpect(jsonPath("$.content[0].waypoints[0].city").value("Москва"))
                .andExpect(jsonPath("$.content[0].waypoints[0].street").value("Онежская улица"))
                .andExpect(jsonPath("$.content[0].waypoints[0].house").value("17"))
                .andExpect(jsonPath("$.content[0].waypoints[0].latitude").value(55.85122898514877))
                .andExpect(jsonPath("$.content[0].waypoints[0].longitude").value(37.51914907476715))
                .andExpect(jsonPath("$.content[0].waypoints[1].id").value("1c812e5e-daaa-4ff8-8ab9-c32c64db02df"))
                .andExpect(jsonPath("$.content[0].waypoints[1].country").value("Россия"))
                .andExpect(jsonPath("$.content[0].waypoints[1].city").value("Москва"))
                .andExpect(jsonPath("$.content[0].waypoints[1].street").value("Ленинградское шоссе"))
                .andExpect(jsonPath("$.content[0].waypoints[1].house").value("16а ст4"))
                .andExpect(jsonPath("$.content[0].waypoints[1].latitude").value(55.82357986357845))
                .andExpect(jsonPath("$.content[0].waypoints[1].longitude").value(37.49708195981275))
                .andExpect(jsonPath("$.content[0].expectedTime").value(927000L))
                .andExpect(jsonPath("$.content[0].expectedDistance").value(5.001))
                .andExpect(jsonPath("$.content[0].passengerCount").value(1))
                .andExpect(jsonPath("$.content[0].requestHumanReadableId").value("OT-0001-00021770"))
                .andExpect(jsonPath("$.content[0].isCoopTrip").value(false))
                .andExpect(jsonPath("$.content[0].type").value("TRIP_REQUEST"))
                .andExpect(jsonPath("$.content[0].timeZone").value("GMT+03:00"))
                .andExpect(jsonPath("$.content[0].fraudComment[0].text").value("some more text"))
                .andExpect(jsonPath("$.content[0].fraudComment[1].text").value("some text"));
    }

    @Test
    @Sql(scripts = {"/scripts/cleanup_database.sql",
            "/scripts/basic_corp_structure.sql",
            "/scripts/trip_purpose.sql",
            "/scripts/approvers.sql",
            "/scripts/approvals.sql"})
    @SneakyThrows
    void getCountActiveApprovals() {
        AuthorizeUtils.authorize(manager, "ROLE_ADMIN_CORP_CLIENT");
        mockMvc.perform(get("/active/count")
                        .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN_CORP_CLIENT")))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }
}