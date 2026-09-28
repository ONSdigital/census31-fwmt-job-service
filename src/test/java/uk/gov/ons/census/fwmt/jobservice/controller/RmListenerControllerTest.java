package uk.gov.ons.census.fwmt.jobservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RmListenerControllerTest {

  private PubSubInboundChannelAdapter externalAdapter;
  private PubSubInboundChannelAdapter internalAdapter;
  private RmListenerController controller;

  @BeforeEach
  void setUp() {
    externalAdapter = mock(PubSubInboundChannelAdapter.class);
    internalAdapter = mock(PubSubInboundChannelAdapter.class);
    controller = new RmListenerController(externalAdapter, internalAdapter);
  }

  @Test
  void stopExternalAdapterDoesNotStopInternalAdapterAndIsIdempotent() {
    when(externalAdapter.isRunning()).thenReturn(true, false);

    ResponseEntity<Void> first = controller.stopExternalActionInstructionListener();
    ResponseEntity<Void> repeated = controller.stopExternalActionInstructionListener();

    assertThat(first.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(repeated.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(externalAdapter).stop();
    verify(internalAdapter, never()).stop();
  }

  @Test
  void startExternalAdapterDoesNotStartInternalAdapterAndIsIdempotent() {
    when(externalAdapter.isRunning()).thenReturn(false, true);

    ResponseEntity<Void> first = controller.startExternalActionInstructionListener();
    ResponseEntity<Void> repeated = controller.startExternalActionInstructionListener();

    assertThat(first.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(repeated.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(externalAdapter).start();
    verify(internalAdapter, never()).start();
  }

  @Test
  void stopAndStartInternalAdapterDoNotChangeExternalAdapter() {
    when(internalAdapter.isRunning()).thenReturn(true, false, false, true);

    controller.stopInternalActionInstructionListener();
    controller.stopInternalActionInstructionListener();
    controller.startInternalActionInstructionListener();
    controller.startInternalActionInstructionListener();

    verify(internalAdapter).stop();
    verify(internalAdapter).start();
    verify(externalAdapter, never()).stop();
    verify(externalAdapter, never()).start();
  }

  @Test
  void legacyRmRoutesControlOnlyExternalAdapter() {
    when(externalAdapter.isRunning()).thenReturn(true, false);

    ResponseEntity<String> stopped = controller.stopListener();
    ResponseEntity<String> started = controller.startListener();

    assertThat(stopped.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(stopped.getBody()).isEqualTo("RM listener stopped.");
    assertThat(started.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(started.getBody()).isEqualTo("RM listener started.");
    verify(externalAdapter).stop();
    verify(externalAdapter).start();
    verify(internalAdapter, never()).stop();
    verify(internalAdapter, never()).start();
  }
}