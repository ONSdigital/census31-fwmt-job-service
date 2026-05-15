package uk.gov.ons.census.fwmt.jobservice.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import uk.gov.census.ffa.storage.utils.StorageUtils;

@SuppressFBWarnings(value="DM_EXIT", justification="App shouldnt start up")
@Configuration
public class GpgConfig {

  @Value("${decryption.pgp}")
  private String privateKeyLocation;
  
  @Autowired
  private StorageUtils storageUtils;

  private static boolean isClasspathLocation(String location) {
    return location != null && location.regionMatches(true, 0, "classpath:", 0, 10);
  }

  private static byte[] readClasspathResource(String classpathLocation) throws IOException {
    String path = classpathLocation.substring("classpath:".length());
    try (InputStream in = new ClassPathResource(path).getInputStream()) {
      return in.readAllBytes();
    }
  }

  @Bean
  public byte[] privateKeyByteArray() throws IOException {
    try {
      if (isClasspathLocation(privateKeyLocation)) {
        return readClasspathResource(privateKeyLocation);
      }
      URI privateKeyUri = URI.create(privateKeyLocation);
      try (InputStream fileInputStream = storageUtils.getFileInputStream(privateKeyUri)) {
        return fileInputStream.readAllBytes();
      }
    } catch (IOException e) {
      System.exit(128);
      throw e;
    }
  }

}
