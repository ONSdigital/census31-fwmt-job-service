package uk.gov.ons.census.fwmt.jobservice.http;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
public class RestLogger implements ClientHttpRequestInterceptor {

  @Override
  @NonNull
  public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body,
      ClientHttpRequestExecution execution) throws IOException {
    ClientHttpResponse response = execution.execute(request, body);

    log.debug(
        "request method: {}, request URI: {}, request headers: {}, request body: {}, response status code: {}, response headers: {}, response body: {}",
        request.getMethod(),
        request.getURI(),
        request.getHeaders(),
        new String(body, StandardCharsets.UTF_8),
        response.getStatusCode(),
        response.getHeaders(),
        CharStreams.toString(new InputStreamReader(response.getBody(), Charsets.UTF_8)));

    return response;
  }

}