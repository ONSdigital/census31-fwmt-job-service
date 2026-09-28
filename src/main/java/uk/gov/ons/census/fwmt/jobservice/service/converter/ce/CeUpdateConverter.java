package uk.gov.ons.census.fwmt.jobservice.service.converter.ce;

import uk.gov.ons.census.fwmt.common.data.tm.CeCasePatchRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstruction;

public final class CeUpdateConverter {

  private CeUpdateConverter() {
  }

  private static CeCasePatchRequest.CeCasePatchRequestBuilder convertCommon(ActionInstruction ffu,
      CeCasePatchRequest.CeCasePatchRequestBuilder builder, String surveyType) {

    int actualResponse = 0;
    int expectedResponse = 0;

    if (surveyType.equals("unit") || surveyType.equals("estab")  ) {
      actualResponse = ffu.getCeActualResponses();
      expectedResponse = ffu.getCeExpectedCapacity();
    }

    builder.actualResponses(actualResponse);
    builder.expectedResponses(expectedResponse);
    builder.ce1Complete(ffu.isCe1Complete());

    return builder;
  }

  public static CeCasePatchRequest convertEstab(ActionInstruction ffu) {
    return CeUpdateConverter.convertCommon(ffu, CeCasePatchRequest.builder(), "estab")
        .build();
  }

  public static CeCasePatchRequest convertSite(ActionInstruction ffu) {
    return CeUpdateConverter.convertCommon(ffu, CeCasePatchRequest.builder(), "site")
        .build();
  }

  public static CeCasePatchRequest convertUnit(ActionInstruction ffu) {
    return CeUpdateConverter.convertCommon(ffu, CeCasePatchRequest.builder(), "unit")
        .build();
  }
}

