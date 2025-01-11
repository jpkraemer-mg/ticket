package quest.darkoro.ticket.util;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataUtil {

  public Map fetchProfile(String username) {
    String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
    try {
      RestTemplate restTemplate = new RestTemplate();
      ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        return response.getBody();
      }
    } catch (Exception ex) {
      log.error("Failed to fetch profile data for name {}: {}", username, ex.getMessage());
    }
    return null;
  }
}
