package uk.gov.ons.census.fwmt.jobservice.helper;

import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsEventDTO;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsEventHardRefusal;
import uk.gov.ons.census.fwmt.common.data.nc.RefusalContact;
import uk.gov.ons.census.fwmt.common.data.nc.RefusalTypeDTO;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NcCaseDetailsDtoBuilder {

  public CaseDetailsDTO createNcCaseDetailsDto() {
    List<CaseDetailsEventDTO> caseDetailsEvenDtoList = new ArrayList<>();
    CaseDetailsEventDTO caseDetailsEventDTO = new CaseDetailsEventDTO();
    caseDetailsEventDTO.setEventDate(OffsetDateTime.now());
    caseDetailsEventDTO.setEventType("REFUSAL_RECEIVED");
    caseDetailsEventDTO.setEventPayload("{\"type\": \"HARD_REFUSAL\", \"agentId\": \"HH-TWH1-ZM-01\", \"contact\": {\"title\": \"\", \"surname\": \"\", \"forename\": \"\"}, \"isHouseholder\": false, \"collectionCase\": {\"id\": \"75d77e53-bdbc-4758-8a6b-06fa83079417\", \"skeleton\": false, \"handDelivery\": false, \"surveyLaunched\": false, \"ceActualResponses\": 0}}");
    caseDetailsEvenDtoList.add(caseDetailsEventDTO);
    CaseDetailsDTO caseDetailsDTO = new CaseDetailsDTO();
    caseDetailsDTO.setCaseId(UUID.randomUUID());
    caseDetailsDTO.setRefusalReceived(RefusalTypeDTO.HARD_REFUSAL);
    caseDetailsDTO.setEvents(caseDetailsEvenDtoList);
    return caseDetailsDTO;
  }

  public CaseDetailsEventHardRefusal createCaseDetailsEventHardRefusal() {
    CaseDetailsEventHardRefusal caseDetailsEventHardRefusal = new CaseDetailsEventHardRefusal();
    RefusalContact refusalContact = new RefusalContact();
    refusalContact.setForename("");
    refusalContact.setSurname("");
    refusalContact.setTitle("");
    caseDetailsEventHardRefusal.setIsHouseholder("false");
    caseDetailsEventHardRefusal.setContact(refusalContact);
    return caseDetailsEventHardRefusal;
  }
}
