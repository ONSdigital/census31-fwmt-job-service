package uk.gov.ons.census.fwmt.jobservice.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class HeadersConverter implements AttributeConverter<Map<String, Object>, String> {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Map<String, Object> headers) {
    String headersJson = null;
    try {
      headersJson = objectMapper.writeValueAsString(headers);
    } catch (final JsonProcessingException e) {
      log.error("JSON writing error ,unable to convert headers", e);
    }
    return headersJson;
  }

  @Override
  public Map<String, Object> convertToEntityAttribute(String headers) {
    Map<String, Object> customerInfo = null;
    try {
      customerInfo = objectMapper.readValue(headers, Map.class);
    } catch (final IOException e) {
      log.error("JSON reading error, unable to convert json to a header", e);
    }

    return customerInfo;
  }
}
