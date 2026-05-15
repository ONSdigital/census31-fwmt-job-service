package uk.gov.ons.census.fwmt.jobservice.repository;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;

@Repository
public interface MessageCacheRepository extends JpaRepository<MessageCache, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  MessageCache findByCaseIdAndAndMessageType(String caseId, String messageType);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  boolean existsByCaseId(String caseId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  boolean existsByCaseIdAndMessageType(String caseId, String messageType);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  MessageCache findByCaseId(String caseId);

}
