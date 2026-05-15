package uk.gov.ons.census.fwmt.jobservice.nc.utils;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.DECRYPTED_HH_NAMES;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.UNABLE_TO_READ_EVENT_PAYLOAD;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsEventDTO;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsEventHardRefusal;
import uk.gov.ons.census.fwmt.common.data.nc.RefusalContact;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

@Service
public class NamedHouseholderRetrieval {

  @Autowired
  private GatewayEventManager eventManager;

  @Value("${decryption.password}")
  private String privateKeyPassword;
  
  @Autowired
  private ObjectMapper objectMapper;
  
  @Autowired
  private byte[] privateKeyByteArray;

  public String getAndSortRmRefusalCases(String caseId, CaseDetailsDTO houseHolder) throws GatewayException {
    StringBuilder contact = new StringBuilder();
    OffsetDateTime previousDate = null;

    List<CaseDetailsEventDTO> caseEventDetails;
    caseEventDetails = houseHolder.getEvents();

    CaseDetailsEventDTO currentRefusal = new CaseDetailsEventDTO();

    for (CaseDetailsEventDTO checkForRefusal : caseEventDetails) {
      OffsetDateTime currentDate = checkForRefusal.getEventDate();
      boolean isRefusal = checkForRefusal.getEventType().equals("REFUSAL_RECEIVED");
      if (isRefusal && (previousDate == null || currentDate.compareTo(previousDate) > 0)) {
        currentRefusal = checkForRefusal;
        previousDate = checkForRefusal.getEventDate();
      }
    }

    CaseDetailsEventHardRefusal householdContact;

    try {
      householdContact = objectMapper.readValue(currentRefusal.getEventPayload(), CaseDetailsEventHardRefusal.class);
    } catch (JsonProcessingException e) {
      eventManager.triggerErrorEvent(this.getClass(), "Unable to read eventPayload", String.valueOf(caseId), UNABLE_TO_READ_EVENT_PAYLOAD,
          "eventPayload", currentRefusal.getEventPayload());
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, e, "Unable to read eventPayload");
    }

    RefusalContact refusalContact = householdContact.getContact();

    String decryptedFirstname;
    String decryptedSurname;
    String decryptedTitle;
    String isHouseHolder;

    isHouseHolder = householdContact.getIsHouseholder().equals("true") ? "Yes" : "No";

    if (refusalContact.getTitle() != null && !refusalContact.getTitle().equals("")) {
      decryptedTitle = DecryptNames.decryptFile(privateKeyByteArray, refusalContact.getTitle(),
          privateKeyPassword.toCharArray());
      contact.append(" ").append(decryptedTitle);
    }
    if (refusalContact.getForename() != null && !refusalContact.getForename().equals("")) {
      decryptedFirstname = DecryptNames.decryptFile(privateKeyByteArray,  refusalContact.getForename(),
          privateKeyPassword.toCharArray());
      contact.append(" ").append(decryptedFirstname);
    }
    if (refusalContact.getSurname() != null && !refusalContact.getSurname().equals("")) {
      decryptedSurname = DecryptNames.decryptFile(privateKeyByteArray, refusalContact.getSurname(),
          privateKeyPassword.toCharArray());
      contact.append(" ").append(decryptedSurname);
    }
    if(!contact.toString().equals("")) {
      contact.insert(0, "Name =");
      contact.append("\n");
    }
    contact.append("Named householder = ").append(isHouseHolder);
    eventManager.triggerEvent(caseId,DECRYPTED_HH_NAMES);
    return contact.toString();
  }
}
