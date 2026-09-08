package uk.gov.ons.census.fwmt.jobservice.service.converter.hh;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCaseRecord;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HhCreateConverterTest {

  private static final String CASE_ID = "550e8400-e29b-41d4-a716-446655440000";
  private static final String CASE_REF = "12345678901";
  private static final String ESTAB_TYPE = "Residential Property";
  private static final String FIELD_OFFICER_ID = "FO12345";
  private static final String ADDRESS_LINE_1 = "1 High Street";
  private static final String ADDRESS_LINE_2 = "Business Park";
  private static final String ADDRESS_LINE_3 = "Unit 5";
  private static final String TOWN_NAME = "Newport";
  private static final String POSTCODE = "NP10 8XG";
  private static final String OA = "W00001234";
  private static final Double LATITUDE = 51.5842;
  private static final Double LONGITUDE = -2.9977;

  /**
   * Creates a standard HH CREATE FwmtActionInstruction with all required fields
   */
  private FwmtActionInstruction createStandardHhInstruction() {
    return FwmtActionInstruction.builder()
        .actionInstruction(ActionInstructionType.CREATE)
        .surveyName("CENSUS")
        .addressType("HH")
        .caseId(CASE_ID)
        .caseRef(CASE_REF)
        .estabType(ESTAB_TYPE)
        .fieldOfficerId(FIELD_OFFICER_ID)
        .addressLine1(ADDRESS_LINE_1)
        .addressLine2(ADDRESS_LINE_2)
        .addressLine3(ADDRESS_LINE_3)
        .townName(TOWN_NAME)
        .postcode(POSTCODE)
        .oa(OA)
        .latitude(LATITUDE)
        .longitude(LONGITUDE)
        .blankFormReturned(false)
        .undeliveredAsAddress(false)
        .build();
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - converts basic HH instruction to CaseRequest")
  void test_convertHhEnglandAndWales_basicConversion() {
    FwmtActionInstruction instruction = createStandardHhInstruction();

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    assertNotNull(result);
    assertEquals(CaseType.HH, result.getType());
    assertEquals(SurveyType.HH, result.getSurveyType());
    assertEquals("HH", result.getCategory());
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - populates case reference correctly")
  void test_convertHhEnglandAndWales_caseReference() {
    FwmtActionInstruction instruction = createStandardHhInstruction();

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    assertEquals(CASE_REF, result.getReference());
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - sets estabType correctly")
  void test_convertHhEnglandAndWales_estabType() {
    FwmtActionInstruction instruction = createStandardHhInstruction();

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    assertEquals(ESTAB_TYPE, result.getEstabType());
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - builds address with all lines")
  void test_convertHhEnglandAndWales_addressLines() {
    FwmtActionInstruction instruction = createStandardHhInstruction();

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    Address address = result.getAddress();
    assertNotNull(address);
    assertEquals(3, address.getLines().size());
    assertEquals(ADDRESS_LINE_1, address.getLines().get(0));
    assertEquals(ADDRESS_LINE_2, address.getLines().get(1));
    assertEquals(ADDRESS_LINE_3, address.getLines().get(2));
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - builds address with town and postcode")
  void test_convertHhEnglandAndWales_addressTownPostcode() {
    FwmtActionInstruction instruction = createStandardHhInstruction();

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    Address address = result.getAddress();
    assertNotNull(address);
    assertEquals(TOWN_NAME, address.getTown());
    assertEquals(POSTCODE, address.getPostcode());
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - sets geography OA")
  void test_convertHhEnglandAndWales_geographyOa() {
    FwmtActionInstruction instruction = createStandardHhInstruction();

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    Address address = result.getAddress();
    assertNotNull(address);
    Geography geography = address.getGeography();
    assertNotNull(geography);
    assertEquals(OA, geography.getOa());
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - sets location latitude and longitude")
  void test_convertHhEnglandAndWales_location() {
    FwmtActionInstruction instruction = createStandardHhInstruction();

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    assertNotNull(result.getLocation());
    assertEquals(LATITUDE, result.getLocation().getLat());
    assertEquals(LONGITUDE, result.getLocation().get_long());
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - sets blankFormReturned flag")
  void test_convertHhEnglandAndWales_blankFormReturned() {
    FwmtActionInstruction instruction = createStandardHhInstruction();
    instruction.setBlankFormReturned(true);

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    assertTrue(result.isBlankFormReturned());
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - sets undeliveredAsAddress (UAA) flag")
  void test_convertHhEnglandAndWales_uaa() {
    FwmtActionInstruction instruction = createStandardHhInstruction();
    instruction.setUndeliveredAsAddress(true);

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    assertTrue(result.isUaa());
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - SAI is false for non-Sheltered Accommodation")
  void test_convertHhEnglandAndWales_saiNotSheltered() {
    FwmtActionInstruction instruction = createStandardHhInstruction();
    instruction.setEstabType("Residential Property");

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    assertFalse(result.isSai());
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - SAI is true for Sheltered Accommodation")
  void test_convertHhEnglandAndWales_saiShelteredAccommodation() {
    FwmtActionInstruction instruction = createStandardHhInstruction();
    instruction.setEstabType("Sheltered Accommodation");

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    assertTrue(result.isSai());
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - handles null addressLine2")
  void test_convertHhEnglandAndWales_nullAddressLine2() {
    FwmtActionInstruction instruction = createStandardHhInstruction();
    instruction.setAddressLine2(null);

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    Address address = result.getAddress();
    assertEquals(ADDRESS_LINE_1, address.getLines().get(0));
    assertEquals("", address.getLines().get(1));
    assertEquals(ADDRESS_LINE_3, address.getLines().get(2));
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - handles null addressLine3")
  void test_convertHhEnglandAndWales_nullAddressLine3() {
    FwmtActionInstruction instruction = createStandardHhInstruction();
    instruction.setAddressLine3(null);

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    Address address = result.getAddress();
    assertEquals(ADDRESS_LINE_1, address.getLines().get(0));
    assertEquals(ADDRESS_LINE_2, address.getLines().get(1));
    assertEquals("", address.getLines().get(2));
  }

  @Test
  @DisplayName("convertHhNisra - converts NISRA HH instruction to CaseRequest")
  void test_convertHhNisra_basicConversion() {
    FwmtActionInstruction instruction = createStandardHhInstruction();
    GatewayCaseRecord cache = null;

    CaseRequest result = HhCreateConverter.convertHhNisra(instruction, cache);

    assertNotNull(result);
    assertEquals(CaseType.HH, result.getType());
    assertEquals(SurveyType.HH, result.getSurveyType());
    assertEquals("HH", result.getCategory());
  }

  @Test
  @DisplayName("convertHhNisra - sets requiredOfficer field")
  void test_convertHhNisra_requiredOfficer() {
    FwmtActionInstruction instruction = createStandardHhInstruction();

    CaseRequest result = HhCreateConverter.convertHhNisra(instruction, null);

    assertEquals(FIELD_OFFICER_ID, result.getRequiredOfficer());
  }

  @Test
  @DisplayName("convertHhNisra - sets SAI flag for Sheltered Accommodation")
  void test_convertHhNisra_saiShelteredAccommodation() {
    FwmtActionInstruction instruction = createStandardHhInstruction();
    instruction.setEstabType("Sheltered Accommodation");

    CaseRequest result = HhCreateConverter.convertHhNisra(instruction, null);

    assertTrue(result.isSai());
  }

  @Test
  @DisplayName("convertHhNisra - sets blankFormReturned flag")
  void test_convertHhNisra_blankFormReturned() {
    FwmtActionInstruction instruction = createStandardHhInstruction();
    instruction.setBlankFormReturned(true);

    CaseRequest result = HhCreateConverter.convertHhNisra(instruction, null);

    assertTrue(result.isBlankFormReturned());
  }

  @Test
  @DisplayName("convertHhNisra - handles undeliveredAsAddress flag")
  void test_convertHhNisra_uaa() {
    FwmtActionInstruction instruction = createStandardHhInstruction();
    instruction.setUndeliveredAsAddress(true);

    CaseRequest result = HhCreateConverter.convertHhNisra(instruction, null);

    assertTrue(result.isUaa());
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - builds complete address structure")
  void test_convertHhEnglandAndWales_completeAddressStructure() {
    FwmtActionInstruction instruction = createStandardHhInstruction();

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    Address address = result.getAddress();
    assertNotNull(address);
    assertNotNull(address.getLines());
    assertEquals(3, address.getLines().size());
    assertEquals(TOWN_NAME, address.getTown());
    assertEquals(POSTCODE, address.getPostcode());
    assertNotNull(address.getGeography());
    assertEquals(OA, address.getGeography().getOa());
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - with cache includes description from cache")
  void test_convertHhEnglandAndWales_withCacheDescription() {
    FwmtActionInstruction instruction = createStandardHhInstruction();
    GatewayCaseRecord cache = GatewayCaseRecord.builder()
        .careCodes("careCode1")
        .accessInfo("access info")
        .build();

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, cache);

    assertNotNull(result.getDescription());
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - coordinates field population from instruction")
  void test_convertHhEnglandAndWales_coordCode() {
    FwmtActionInstruction instruction = createStandardHhInstruction();
    instruction.setFieldCoordinatorId("COORD123");

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    assertEquals("COORD123", result.getCoordCode());
  }

  @Test
  @DisplayName("convertHhEnglandAndWales - handles both false boolean flags correctly")
  void test_convertHhEnglandAndWales_bothFlagsTrue() {
    FwmtActionInstruction instruction = createStandardHhInstruction();
    instruction.setBlankFormReturned(true);
    instruction.setUndeliveredAsAddress(true);

    CaseRequest result = HhCreateConverter.convertHhEnglandAndWales(instruction, null);

    assertTrue(result.isBlankFormReturned());
    assertTrue(result.isUaa());
  }

  @Test
  @DisplayName("convertHhNisra - builds complete HH structure for NISRA")
  void test_convertHhNisra_completeStructure() {
    FwmtActionInstruction instruction = createStandardHhInstruction();
    instruction.setFieldCoordinatorId("NISRA_COORD");

    CaseRequest result = HhCreateConverter.convertHhNisra(instruction, null);

    assertEquals(CaseType.HH, result.getType());
    assertEquals(SurveyType.HH, result.getSurveyType());
    assertEquals("HH", result.getCategory());
    assertEquals(FIELD_OFFICER_ID, result.getRequiredOfficer());
    assertNotNull(result.getAddress());
    assertNotNull(result.getLocation());
  }
}