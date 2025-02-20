package quest.darkoro.ticket.listener.primary;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.PrimaryListener;
import quest.darkoro.ticket.persistence.repository.ContentFilterRepository;

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
    var regExFilter = "([\\^\\-\\\\#'`´%\\[\\]\"()$&*+/])";

    for (var filter: filters) {
      var content = e.getMessage().getContentRaw();
      var sanitized = filter.getContent().replaceAll(regExFilter, "\\\\$1");
      if (content.toLowerCase().contains(filter.getContent().toLowerCase()) ||
          content.toLowerCase().contains(sanitized.toLowerCase()) ||
          content.toLowerCase().replaceAll(regExFilter, "").contains(sanitized.toLowerCase()) ||
          content.toLowerCase().replaceAll(regExFilter, "").contains(filter.getContent().toLowerCase())) {
        e.getMessage().delete().queue();
        e.getChannel().sendMessage("Fuck off, no scamming").queue();
      }
    }
  }
}
