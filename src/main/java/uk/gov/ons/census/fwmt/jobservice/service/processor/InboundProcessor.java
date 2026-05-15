package uk.gov.ons.census.fwmt.jobservice.service.processor;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

import java.time.Instant;

public interface InboundProcessor<T> {
    ProcessorKey getKey();

    boolean isValid(T rmRequest, GatewayCache cache);

    void process(T rmRequest, GatewayCache cache, Instant messageReceivedTime) throws GatewayException;

}