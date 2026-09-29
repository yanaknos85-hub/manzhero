package ru.sberbank.ditsib.transport.approvals.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.approvals.database.dao.FraudDataRepository;
import ru.sberbank.ditsib.transport.approvals.database.model.FraudData;
import ru.sberbank.ditsib.transport.approvals.database.model.TripRequestApproval;
import ru.sberbank.ditsib.transport.approvals.mappers.FraudDataMapper;
import ru.sberbank.ditsib.transport.approvals.messaging.message.RequestMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FraudDataServiceImplTest {
    @Mock
    private FraudDataMapper fraudDataMapper;
    @Mock
    private FraudDataRepository fraudDataRepository;

    @InjectMocks
    private FraudDataServiceImpl fraudDataService;


    private TripRequestApproval tripRequestApproval;
    private RequestMessage.Fraud fraudDto;
    private FraudData fraudEntity;

    @BeforeEach
    void setUp() {
        tripRequestApproval = new TripRequestApproval();
        tripRequestApproval.setId(UUID.randomUUID());

        fraudDto = new RequestMessage.Fraud("RADIUS","Test fraud",UUID.randomUUID());

        fraudEntity = new FraudData();
        fraudEntity.setType("RADIUS");
        fraudEntity.setComment("Test fraud");
        fraudEntity.setRequestId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Должен пропускать обновление, если fraudData null")
    void shouldSkipUpdate_WhenFraudDataIsEmptyOrNull() {
        fraudDataService.updateFraudDataForApproval(tripRequestApproval, null);

        verify(fraudDataMapper, never()).toEntities(any());
        assertTrue(tripRequestApproval.getFraudData().isEmpty());
    }

    @Test
    @DisplayName("Должен установить пустой список, если пришёл пустой fraudData")
    void shouldSetEmptyList_WhenFraudDataIsEmpty() {
        final List<RequestMessage.Fraud> emptyList = Collections.emptyList();

        fraudDataService.updateFraudDataForApproval(tripRequestApproval, emptyList);

        verify(fraudDataMapper, never()).toEntities(any());
        final var result = tripRequestApproval.getFraudData();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Должен сконвертировать и установить fraudData, если данные переданы")
    void shouldConvertAndSetFraudData_WhenDataProvided() {
        final var fraudDtos = Collections.singletonList(fraudDto);
        final var fraudEntities = Collections.singletonList(fraudEntity);

        when(fraudDataMapper.toEntities(fraudDtos)).thenReturn(fraudEntities);

        fraudDataService.updateFraudDataForApproval(tripRequestApproval, fraudDtos);

        verify(fraudDataMapper).toEntities(fraudDtos);
        final var result = tripRequestApproval.getFraudData();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(fraudEntity, result.get(0));
        assertSame(tripRequestApproval, fraudEntity.getApproval());
    }

    @Test
    @DisplayName("Должен установить пустой список, если mapper вернул null")
    void shouldSetEmptyList_WhenMapperReturnsNull() {
        final var fraudDtos = Collections.singletonList(fraudDto);

        when(fraudDataMapper.toEntities(fraudDtos)).thenReturn(null);

        fraudDataService.updateFraudDataForApproval(tripRequestApproval, fraudDtos);

        verify(fraudDataMapper).toEntities(fraudDtos);
        final var result = tripRequestApproval.getFraudData();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Должен установить пустой список, если mapper вернул пустой список")
    void shouldSetEmptyList_WhenMapperReturnsEmptyList() {
        final var fraudDtos = Collections.singletonList(fraudDto);

        when(fraudDataMapper.toEntities(fraudDtos)).thenReturn(Collections.emptyList());

        fraudDataService.updateFraudDataForApproval(tripRequestApproval, fraudDtos);

        verify(fraudDataMapper).toEntities(fraudDtos);
        final var result = tripRequestApproval.getFraudData();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Должен очистить существующий список только если он был и есть ID сущности")
    void shouldClearExistingList_WhenIdAndDataPresent() {
        final var approval = new TripRequestApproval();
        approval.setId(UUID.randomUUID());
        approval.setFraudData(new ArrayList<>(Collections.singletonList(new FraudData())));

        final var fraudDtos = Collections.singletonList(fraudDto);
        final var fraudEntities = Collections.singletonList(fraudEntity);

        when(fraudDataMapper.toEntities(fraudDtos)).thenReturn(fraudEntities);

        fraudDataService.updateFraudDataForApproval(approval, fraudDtos);

        verify(fraudDataMapper).toEntities(fraudDtos);
        final var result = approval.getFraudData();
        assertEquals(1, result.size());
        assertSame(fraudEntity, result.get(0));
        assertSame(approval, fraudEntity.getApproval());
    }

    @Test
    @DisplayName("Не должен очищать список, если у сущности нет ID")
    void shouldNotClearList_WhenEntityHasNoId() {
        final var approval = new TripRequestApproval();
        approval.setFraudData(new ArrayList<>(Collections.singletonList(new FraudData())));

        final var fraudDtos = Collections.singletonList(fraudDto);
        final var fraudEntities = Collections.singletonList(fraudEntity);

        when(fraudDataMapper.toEntities(fraudDtos)).thenReturn(fraudEntities);

        fraudDataService.updateFraudDataForApproval(approval, fraudDtos);

        verify(fraudDataMapper).toEntities(fraudDtos);
        final var result = approval.getFraudData();
        assertEquals(1, result.size());
        assertSame(fraudEntity, result.get(0));
        assertSame(approval, fraudEntity.getApproval());
    }
}