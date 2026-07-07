package quest.darkoro.ticket.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.listeners.primary.AntiScamFilteringListener;
import quest.darkoro.ticket.persistence.model.ContentFilter;
import quest.darkoro.ticket.persistence.ContentFilterRepository;
import quest.darkoro.ticket.util.MessageUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilterCommandService {

  private final ContentFilterRepository contentFilterRepository;
  private final MessageUtil messageUtil;
  private final AntiScamFilteringListener antiScamFilteringListener;

  public void handleFilterAdd(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();

    var filter = e.getOption("filter").getAsString();
    log.info("Filter added: {}", filter);
    e.reply("Filter added: `%s`".formatted(filter)).setEphemeral(true).queue();
    contentFilterRepository.save(new ContentFilter().setGuildId(gid).setContent(filter));
    antiScamFilteringListener.invalidateFilters(gid);

    messageUtil.sendCommandLog(member, "/filter add",
        "ADD MESSAGE CONTENT FILTER: `%s`".formatted(filter));
  }

  public void handleFilterRemove(SlashCommandInteractionEvent e) {
    e.reply("Not yet implemented").setEphemeral(true).queue();
  }

  public void handleFilterList(SlashCommandInteractionEvent e) {
    e.reply("Not yet implemented").setEphemeral(true).queue();
  }
}
