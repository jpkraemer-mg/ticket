package quest.darkoro.ticket.listener.secondary.command;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.model.ContentFilter;
import quest.darkoro.ticket.persistence.model.Guild;
import quest.darkoro.ticket.persistence.repository.ContentFilterRepository;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.util.MessageUtil;

@SecondaryListener
@Slf4j
@RequiredArgsConstructor
@Service
public class FilterAddCommandListener extends ListenerAdapter {

  private final ContentFilterRepository contentFilterRepository;
  private final GuildRepository guildRepository;
  private final MessageUtil messageUtil;

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("filter") || !"add".equals(e.getSubcommandName())) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();

    var filter = e.getOption("filter").getAsString().replaceAll("([\\^\\-\\\\#'`´%\\[\\]\"])", "\\$1");
    log.info("Filter added: {}", filter);
    e.reply("Filter added: `%s`".formatted(filter)).setEphemeral(true).queue();
//    contentFilterRepository.save(new ContentFilter().setGuildId(gid).setContent(filter));
//
    var guild = guildRepository.findById(gid).orElse(new Guild());
    if (guild.getLog() != null) {
      messageUtil.sendLogMessage("Command `%s` executed by `%s (%s)`\nADD MESSAGE CONTENT FILTER: `%s`".formatted(
          "/filter add",
          member.getEffectiveName(),
          member.getIdLong(),
          filter), e.getGuild().getTextChannelById(guild.getLog())
      );
    }
  }
}
