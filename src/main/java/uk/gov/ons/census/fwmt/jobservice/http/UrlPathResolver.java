package uk.gov.ons.census.fwmt.jobservice.http;

public final class UrlPathResolver {

  private UrlPathResolver() {
  }

  public static String join(String baseUrl, String path) {
    String normalizedBaseUrl = normalizeBaseUrl(baseUrl);
    String normalizedPath = normalizePath(path);
    return normalizedBaseUrl + normalizedPath;
  }

  private static String normalizeBaseUrl(String baseUrl) {
    if (baseUrl == null || baseUrl.isEmpty()) {
      return "";
    }
    return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
  }

  private static String normalizePath(String path) {
    if (path == null || path.isEmpty()) {
      return "";
    }
    int index = 0;
    while (index < path.length() && path.charAt(index) == '/') {
      index++;
    }
    return path.substring(index);
  }
}