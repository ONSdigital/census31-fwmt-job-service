package uk.gov.ons.census.fwmt.jobservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.rabbit.QueueMigrator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RabbitQueueControllerTest {

  private MockMvc mockMvc;

  @InjectMocks
  private RabbitQueueController controller;

  @Mock
  private QueueMigrator migrator;

  @BeforeEach
  public void setup() {
    mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
  }

  @Test
  public void shouldMigrateTransientQueueToGWLinkQueueByDefault() throws Exception {
    this.mockMvc.perform(get("/jobs/migratetransients")).andExpect(status().isOk());
    verify(migrator).migrate(eq(RabbitQueueController.originQ), eq(RabbitQueueController.destRoute));
  }

  @Test
  public void shouldMigrateTransientQueueToGWLinkQueue() throws Exception {
    this.mockMvc.perform(
        get("/jobs/migratetransients")
            .param("originQ", "originTest")
            .param("destRoute", "destTest")

    ).andExpect(status().isOk());
    verify(migrator).migrate(eq("originTest"), eq("destTest"));
  }

  @Test
  public void shouldReturnBadRequestIfQueueOrRouteIsInvalid() throws Exception {
    doThrow(GatewayException.class).when(migrator).migrate(any(), any());
    this.mockMvc.perform(
        get("/jobs/migratetransients")
            .param("originQ", "originTest")
            .param("destRoute", "destTest")

    ).andExpect(status().isBadRequest());

  }
}