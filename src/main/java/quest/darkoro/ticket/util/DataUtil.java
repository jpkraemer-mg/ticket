package quest.darkoro.ticket.util;

import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataUtil {

  private final RestTemplate restTemplate = createRestTemplate();

  private static RestTemplate createRestTemplate() {
    var factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(3000);
    factory.setReadTimeout(3000);
    return new RestTemplate(factory);
  }

  public Map fetchProfile(String username) {
    String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
    try {
      ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return response.getBody();
      }
    } catch (Exception ex) {
      log.error("Failed to fetch profile data for name {}: {}", username, ex.getMessage());
    }
    return null;
  }

  public String fixUUID(String uuid) {
    return UUID.fromString(uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")).toString();
  }
}
