package uk.gov.ons.census.fwmt.jobservice.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * This class is a caching service that reduces the frequency of date checks
 */

@Slf4j
@Service
public class CeFollowUpSchedulingService {

  @Value("${ce.followUpDate}")
  Date followUpDate;

  @Value("${ce.startDate}")
  Date startDate;


  public boolean isInFollowUp() {
    Date todaysDate = new Date();

    return (todaysDate.after(startDate) && todaysDate.after(followUpDate));

  }

}
