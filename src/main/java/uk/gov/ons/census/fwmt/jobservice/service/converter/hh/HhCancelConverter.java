package uk.gov.ons.census.fwmt.jobservice.service.converter.hh;

import uk.gov.ons.census.fwmt.common.data.tm.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

;

public final class HhCancelConverter {

  private HhCancelConverter(){
  }

  public static CasePauseRequest buildCancel(FwmtCancelActionInstruction ffu) {
    String currentDate = "";
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
    currentDate = dateFormat.format(new Date(System.currentTimeMillis()));
    
    return CasePauseRequest.builder()
          .code("inf")
          .effectiveFrom(currentDate)
          .build();
  }
}
