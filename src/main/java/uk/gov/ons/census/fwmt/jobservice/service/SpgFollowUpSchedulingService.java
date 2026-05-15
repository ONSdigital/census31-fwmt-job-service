package uk.gov.ons.census.fwmt.jobservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * This class is a caching service that reduces the frequency of date checks
 */

@Slf4j
@Service
public class SpgFollowUpSchedulingService {
  @Value("${spg.followUpDate}")
  Long followUpDate;
  private boolean inFollowUp = false;

  public boolean isInFollowUp() {
    return inFollowUp;
  }

  @Scheduled(cron = "0 0 * * * *")
  public void checkTimeDate() {
    long unixTime = System.currentTimeMillis() / 1000L;
    log.info("The time is now {}", unixTime);
    inFollowUp = unixTime > followUpDate;
  }
}
