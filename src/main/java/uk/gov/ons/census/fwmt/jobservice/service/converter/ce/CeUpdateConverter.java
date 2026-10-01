package uk.gov.ons.census.fwmt.jobservice.service.converter.ce;

import uk.gov.ons.census.fwmt.common.data.tm.CeCasePatchRequest;
import uk.gov.ons.census.fwmt.common.dto.rm.ActionInstruction;

public final class CeUpdateConverter {

  private CeUpdateConverter() {
  }

  private static CeCasePatchRequest.CeCasePatchRequestBuilder convertCommon(ActionInstruction actionInstruction,
      CeCasePatchRequest.CeCasePatchRequestBuilder builder, String surveyType) {

    int actualResponse = 0;
    int expectedResponse = 0;

    if (surveyType.equals("unit") || surveyType.equals("estab")  ) {
      actualResponse = actionInstruction.getCeActualResponses();
      expectedResponse = actionInstruction.getCeExpectedCapacity();
    }

    builder.actualResponses(actualResponse);
    builder.expectedResponses(expectedResponse);
    builder.ce1Complete(actionInstruction.isCe1Complete());

    return builder;
  }

  public static CeCasePatchRequest convertEstab(ActionInstruction actionInstruction) {
    return CeUpdateConverter.convertCommon(actionInstruction, CeCasePatchRequest.builder(), "estab")
        .build();
  }

  public static CeCasePatchRequest convertSite(ActionInstruction actionInstruction) {
    return CeUpdateConverter.convertCommon(actionInstruction, CeCasePatchRequest.builder(), "site")
        .build();
  }

  public static CeCasePatchRequest convertUnit(ActionInstruction actionInstruction) {
    return CeUpdateConverter.convertCommon(actionInstruction, CeCasePatchRequest.builder(), "unit")
        .build();
  }
}

