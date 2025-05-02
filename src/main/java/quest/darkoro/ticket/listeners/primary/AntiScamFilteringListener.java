package quest.darkoro.ticket.listeners.primary;

import java.util.concurrent.TimeUnit;
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

  private final ContentFilterRepository contentFilterRepository;

  @Override
  public void onMessageReceived(@NonNull MessageReceivedEvent e) {
    if (e.getAuthor().isBot()) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var filters = contentFilterRepository.findByGuildId(gid);
    var regExFilter = "([\\^\\-\\\\#'`´%\\[\\]\"()$&*+/{}])";

    for (var f : filters) {
      var content = e.getMessage().getContentRaw().toLowerCase();
      var filter = f.getContent().toLowerCase();
      var sanitized = filter.replaceAll(regExFilter, "\\\\$1");
      if (content.contains(filter) || content.contains(sanitized) ||
          content.replaceAll(regExFilter, "").contains(sanitized) ||
          content.replaceAll(regExFilter, "").contains(filter)) {
        e.getMessage().delete().queue();
        e.getChannel()
            .sendMessage("Fuck off, no scamming")
            .queue(m -> m.delete().queueAfter(10, TimeUnit.SECONDS));
      }
    }
  }
}
