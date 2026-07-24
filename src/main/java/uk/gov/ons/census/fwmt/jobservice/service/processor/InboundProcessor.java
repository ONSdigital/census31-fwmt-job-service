package uk.gov.ons.census.fwmt.jobservice.service.processor;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;

import java.time.Instant;

public interface InboundProcessor<T> {
    ProcessorKey getKey();

    boolean isValid(T rmRequest, GatewayCaseRecord cache);

    void process(T rmRequest, GatewayCaseRecord cache, Instant messageReceivedTime) throws GatewayException;

}