package uk.gov.ons.census.fwmt.jobservice.service.converter;

import uk.gov.ons.census.fwmt.common.error.GatewayException;

public class ConverterUtils {

  private ConverterUtils() {
  }

  public static Long parseLong(String input) throws GatewayException {
    if (input == null) {
      return null;
    } else {
      try {
        return Long.parseLong(input);
      } catch (NumberFormatException e) {
        throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Problem converting SPG Site Case", e);
      }
    }
  }

  public static Float parseFloat(String input) throws GatewayException {
    if (input == null) {
      return null;
    } else {
      try {
        return Float.parseFloat(input);
      } catch (NumberFormatException e) {
        throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Problem converting SPG Site Case", e);
      }
    }
  }
}
