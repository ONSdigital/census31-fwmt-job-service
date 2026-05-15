package uk.gov.ons.census.fwmt.jobservice.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "message_cache")
public class MessageCache {
  @Id
  @Column(name = "case_id", unique = true, nullable = false)
  public String caseId;

  @Column(name = "message_type")
  public String messageType;

  @Column(name = "message")
  public String message;
}
