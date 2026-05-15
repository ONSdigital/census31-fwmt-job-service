package uk.gov.ons.census.fwmt.jobservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.repository.MessageCacheRepository;

/**
 * This class is bare-bones because it's a simple connector between the rest of the code and the caching implementation
 * Please don't subvert this class by touching the GatewayCacheRepository
 * If we ever change from a database to redis, this class will form the breaking point
 */

@Slf4j
@Service
public class MessageCacheService {
  public final MessageCacheRepository repository;

  public MessageCacheService(MessageCacheRepository repository) {
    this.repository = repository;
  }

  public MessageCache getById(String caseId) {
    return repository.findByCaseId(caseId);
  }

  public MessageCache getByIdAndMessageType(String caseId, String messageType) {
    return repository.findByCaseIdAndAndMessageType(caseId, messageType);
  }

  public boolean doesCaseIdAndMessageTypeExist(String caseId, String messageType) {
    return repository.existsByCaseIdAndMessageType(caseId, messageType);
  }

  public String getMessageTypeForId(String caseId) {
    String messageType = "";
    MessageCache messageCache = repository.findByCaseId(caseId);
    if (messageCache != null) {
      return messageCache.messageType;
    }
    return messageType;
  }

  public boolean doesCaseExist(String caseId) {
    return repository.existsByCaseId(caseId);
  }

  public MessageCache save(MessageCache cache) {
    return repository.save(cache);
  }

  public void delete(MessageCache cache) {
     repository.delete(cache);
  }

}
