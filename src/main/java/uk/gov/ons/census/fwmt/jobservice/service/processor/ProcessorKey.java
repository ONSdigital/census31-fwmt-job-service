package uk.gov.ons.census.fwmt.jobservice.service.processor;

import lombok.Builder;
import lombok.Getter;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

@Getter
@Builder
public class ProcessorKey {
  private String actionInstruction;
  private String surveyName;
  private String addressType;
  private String addressLevel;

  public static ProcessorKey buildKey(FwmtCancelActionInstruction rmRequest) {
    return ProcessorKey.builder()
        .actionInstruction(rmRequest.getActionInstruction().toString())
        .surveyName(rmRequest.getSurveyName())
        .addressType(rmRequest.getAddressType())
        .addressLevel(rmRequest.getAddressLevel()).build();
  }

  public static ProcessorKey buildKey(FwmtActionInstruction rmRequest) {
    return ProcessorKey.builder()
        .actionInstruction(rmRequest.getActionInstruction().toString())
        .surveyName(rmRequest.getSurveyName())
        .addressType(rmRequest.getAddressType())
        .addressLevel(rmRequest.getAddressLevel()).build();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((actionInstruction == null) ? 0 : actionInstruction.hashCode());
    result = prime * result + ((addressLevel == null) ? 0 : addressLevel.hashCode());
    result = prime * result + ((addressType == null) ? 0 : addressType.hashCode());
    result = prime * result + ((surveyName == null) ? 0 : surveyName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ProcessorKey other = (ProcessorKey) obj;
    if (actionInstruction == null) {
      if (other.actionInstruction != null)
        return false;
    } else if (!actionInstruction.equals(other.actionInstruction))
      return false;
    if (addressLevel == null) {
      if (other.addressLevel != null)
        return false;
    } else if (!addressLevel.equals(other.addressLevel))
      return false;
    if (addressType == null) {
      if (other.addressType != null)
        return false;
    } else if (!addressType.equals(other.addressType))
      return false;
    if (surveyName == null) {
      if (other.surveyName != null)
        return false;
    } else if (!surveyName.equals(other.surveyName))
      return false;
    return true;
  }

}