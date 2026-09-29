package ru.sberbank.ditsib.transport.limits.mapper;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sberbank.ditsib.transport.limits.dto.BonusDTO;
import ru.sberbank.ditsib.transport.limits.dto.BonusRequestDTO;
import ru.sberbank.ditsib.transport.limits.model.bonus.Bonus;
import ru.sberbank.ditsib.transport.limits.model.bonus.BonusOperation;
import ru.sberbank.ditsib.transport.limits.model.bonus.BonusRequest;
import ru.sberbank.ditsib.transport.limits.model.bonus.BonusRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("app_platform_limits")
@DisplayName("Проверка маппера бонусного счёта")
class BonusMapperTest {

    private final BonusMapper mapper = new BonusMapperImpl();
    
    @Test
    @DisplayName("Маппинг бонусного счёта")
    void toBonusDTO() {
        var bonus = Bonus.builder()
                         .ownerId(UUID.randomUUID())
                         .balance(BigDecimal.valueOf(100000))
                         .sum(BigDecimal.valueOf(120000L))
                         .build();
        var request = List.of(
                BonusRequest.builder()
                            .id(UUID.randomUUID())
                            .bonus(bonus)
                            .creationTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .sum(BigDecimal.valueOf(100L))
                            .operation(BonusOperation.DEPOSIT)
                            .status(BonusRequestStatus.DONE)
                            .build(),
                BonusRequest.builder()
                            .id(UUID.randomUUID())
                            .bonus(bonus)
                            .creationTime(LocalDateTime.now())
                            .updateTime(LocalDateTime.now())
                            .sum(BigDecimal.valueOf(90L))
                            .operation(BonusOperation.SPEND)
                            .status(BonusRequestStatus.RESERVED)
                            .build()
                             );
        bonus.setRequests(request);
        deepCompareBonusToBonusDTOMapping(mapper.toBonusDTO(bonus), bonus);
    }
    
    private void deepCompareBonusToBonusDTOMapping(BonusDTO dto, Bonus bonus) {
        assertThat(dto.getOwnerId()).isEqualTo(bonus.getOwnerId());
        assertThat(dto.getBalance()).isEqualTo(bonus.getBalance());
        assertThat(dto.getSum()).isEqualTo(bonus.getSum());
        assertThat(dto.getRequests()).hasSameSizeAs(bonus.getRequests());
        for (var requestDTO : dto.getRequests()) {
            compareBonusRequestToBonusRequestDTOMapping(requestDTO,
                                                        bonus.getRequests()
                                                             .stream().filter(request -> requestDTO.getId().equals(request.getId())).findAny().orElseThrow());
        }
    }
    
    private void compareBonusRequestToBonusRequestDTOMapping(BonusRequestDTO dto, BonusRequest request) {
        assertThat(dto.getId()).isEqualTo(request.getId());
        assertThat(dto.getSum()).isEqualTo(request.getSum());
        assertThat(dto.getOperation()).isEqualTo(request.getOperation());
        assertThat(dto.getStatus()).isEqualTo(request.getStatus());
        assertThat(dto.getUpdateTime()).isEqualTo(request.getUpdateTime());
        assertThat(dto.getReason()).isEqualTo(request.getReason());
        
    }
}
