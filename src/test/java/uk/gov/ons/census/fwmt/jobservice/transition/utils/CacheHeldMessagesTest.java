package uk.gov.ons.census.fwmt.jobservice.transition.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.helper.FwmtCancelJobRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;

import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CacheHeldMessagesTest {

    @InjectMocks
    private CacheHeldMessages cacheHeldMessages;

    @Mock
    private GatewayEventManager eventManager;

    @Mock
    private GatewayCache gatewayCache;

    @Mock
    private GatewayCacheService cacheService;

    @Mock
    private MessageCache messageCache;

    @Mock
    private MessageCacheService messageCacheService;

    @Captor
    private ArgumentCaptor<GatewayCache> spiedCache;

    @Test
    @DisplayName("Should not save existsInFwmt to true in cache")
    public void shouldNotSaveExistsInFwmtToTrueInCache() {
        final FwmtCancelActionInstruction cancelActionInstruction = new FwmtCancelJobRequestBuilder().cancelActionInstruction();
        long epochTimeStamp = Long.parseLong("1613035113");
        final Instant receivedMessageTime = Instant.ofEpochMilli(epochTimeStamp);
        GatewayCache gatewayCache = null;
        MessageCache messageCache = MessageCache.builder().message("Test message").messageType("Cancel")
            .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").build();
        cacheHeldMessages.cacheMessage(messageCache, gatewayCache, cancelActionInstruction, receivedMessageTime);
        verify(cacheService).save(spiedCache.capture());
        boolean existsInFwmt = spiedCache.getValue().existsInFwmt;
        Assertions.assertFalse(existsInFwmt);
    }

}