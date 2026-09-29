package ru.sber.transport.tariff_fleet.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.humanreadableid.service.SQGenerator;
import ru.sber.transport.tariff_fleet.human_readable_id.constant.Prefix;
import ru.sber.transport.tariff_fleet.service.OrganizationService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class HumanReadableIdServiceTest {

    @InjectMocks
    private HumanReadableIdServiceImpl humanReadableIdService;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private SQGenerator sqGenerator;

    @Test
    void createHumanReadableIdByUserId() {
        var userId = UUID.randomUUID();
        var humanReadableId = "TF-0008-00000002";
        doReturn(8L).when(organizationService).getDigitIdByUserId(userId);
        doReturn(humanReadableId).when(sqGenerator).getNextId(any(Prefix.class), anyLong());
        assertThat(humanReadableIdService.createHumanReadableIdByUserId(userId)).isEqualTo(humanReadableId);
    }

    @Test
    void createHumanReadableIdByDigitId() {
        var digitId = 100L;
        var humanReadableId = "TF-0008-00000002";
        doReturn(humanReadableId).when(sqGenerator).getNextId(any(Prefix.class), anyLong());
        assertThat(humanReadableIdService.createHumanReadableIdByDigitId(digitId)).isEqualTo(humanReadableId);
    }
}