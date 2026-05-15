package uk.gov.ons.census.fwmt.jobservice.http.comet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.IOException;

@Slf4j
@Component
public class CometPerfRequestInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    final StopWatch stopwatch = new StopWatch();
    stopwatch.start();
    final ClientHttpResponse response = execution.execute(request, body);
    stopwatch.stop();
    log.info("TM Request tm_request_uri= {} , tm_request_type={} , tm_response_time = {}(ms) ,tm_response_code = {}", request.getURI(), request.getMethod(), stopwatch.getTotalTimeMillis());
    return response;
  }
}
