package uk.gov.ons.census.fwmt.jobservice.data;

public enum ActionInstructionType {
  CANCEL("Cancel"),
  CREATE("Create"),
  UPDATE("Update");

  public final String name;

  ActionInstructionType(String name) {
    this.name = name;
  }

  @Override public String toString() {
    return name;
  }
}
