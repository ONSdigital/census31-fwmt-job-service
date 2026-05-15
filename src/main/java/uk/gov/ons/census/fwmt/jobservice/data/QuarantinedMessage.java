package uk.gov.ons.census.fwmt.jobservice.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCommonInstruction;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP")
public class QuarantinedMessage implements FwmtCommonInstruction {
  @Id
  @GeneratedValue
  private UUID id;
  @Column
  private String addressType;
  @Column
  private String addressLevel;
  @Column
  private boolean nc;
  @Column
  @Enumerated(EnumType.ORDINAL)
  private ActionInstructionType actionInstruction;
  @Column
  private String surveyName;
  @Column
  private String caseId;

  @Column(columnDefinition = "timestamp with time zone")
  @CreationTimestamp
  private OffsetDateTime skippedTimestamp;

  @Lob
  @Type(type = "org.hibernate.type.BinaryType")
  @Column
  private byte[] messagePayload;

  @Column
  private String queue;

  @Column
  private String routingKey;
  @Column
  @Convert(converter = HeadersConverter.class)
  private Map<String, Object> headers;

}