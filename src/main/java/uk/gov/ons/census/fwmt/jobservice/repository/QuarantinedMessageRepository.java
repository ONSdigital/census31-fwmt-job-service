package uk.gov.ons.census.fwmt.jobservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.ons.census.fwmt.jobservice.data.QuarantinedMessage;

import java.util.UUID;

public interface QuarantinedMessageRepository extends JpaRepository<QuarantinedMessage, UUID> {

}
