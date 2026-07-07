package quest.darkoro.ticket.listeners.primary;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.PrimaryListener;
import quest.darkoro.ticket.persistence.ContentFilterRepository;

@Slf4j
@RequiredArgsConstructor
@PrimaryListener
@Service
public class AntiScamFilteringListener extends ListenerAdapter {

  private static final Pattern SPECIAL_CHARS = Pattern.compile(
      "[\\^\\-\\\\#'`´%\\[\\]\"()$&*+/{}]");

  private final ContentFilterRepository contentFilterRepository;
  private final Map<Long, List<FilterPattern>> filterCache = new ConcurrentHashMap<>();

  private record FilterPattern(String raw, String stripped) {}

  public void invalidateFilters(Long guildId) {
    filterCache.remove(guildId);
  }

  @Override
  public void onMessageReceived(@NonNull MessageReceivedEvent e) {
    if (!e.isFromGuild() || e.getAuthor().isBot()) {
      return;
    }

    var filters = filterCache.computeIfAbsent(e.getGuild().getIdLong(),
        gid -> contentFilterRepository.findByGuildId(gid)
            .stream()
            .map(f -> {
              var raw = f.getContent().toLowerCase();
              return new FilterPattern(raw, SPECIAL_CHARS.matcher(raw).replaceAll(""));
            })
            .toList());
    if (filters.isEmpty()) {
      return;
    }

    var content = e.getMessage().getContentRaw().toLowerCase();
    var stripped = SPECIAL_CHARS.matcher(content).replaceAll("");

    for (var f : filters) {
      if (content.contains(f.raw())
          || (!f.stripped().isEmpty() && stripped.contains(f.stripped()))) {
        e.getMessage().delete().queue();
        e.getChannel()
            .sendMessage("Fuck off, no scamming")
            .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
        break;
      }
    }
  }
}
