package uk.gov.ons.census.fwmt.jobservice.config;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.data.tm.Case;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;

@Component
public class MapperConfig extends ConfigurableMapper {

  @Override
  protected void configure(final MapperFactory factory) {
    factory.classMap(CaseRequest.class, Case.class).byDefault().register();
  }
}
