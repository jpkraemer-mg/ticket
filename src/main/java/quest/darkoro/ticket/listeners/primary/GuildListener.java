package quest.darkoro.ticket.listeners.primary;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.interfaces.BaseCommand;
import quest.darkoro.ticket.persistence.GuildRepository;
import quest.darkoro.ticket.util.MessageUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class GuildListener extends ListenerAdapter {

  private final List<BaseCommand> commands;
  private final MessageUtil messageUtil;
  private final GuildRepository guildRepository;

  @Override
  public void onGuildReady(@NotNull GuildReadyEvent e) {
    registerCommands(e.getGuild());
    var guild = guildRepository.findById(e.getGuild().getIdLong());
    if (guild.isPresent()) {
      if (guild.get().getBase() != null) {
        messageUtil.sendTicketMessage(e.getGuild().getTextChannelById(guild.get().getBase()),
            e.getJDA());
      }
      if (guild.get().getRole() != null) {
        messageUtil.sendRoleMessage(e.getGuild().getTextChannelById(guild.get().getRole()),
            e.getJDA());
      }
    }
  }

  @Override
  public void onGuildJoin(@NotNull GuildJoinEvent e) {
    registerCommands(e.getGuild());
  }

  private void registerCommands(Guild g) {
    g.updateCommands()
        .addCommands(commands
            .stream()
            .map(BaseCommand::create)
            .toList()
        )
        .queue(
            s -> log.info("Registered commands for guild {} successfully: {}",
                g.getName(),
                commands
                    .stream()
                    .map(c -> c.create().getName())
                    .toList()
            ),
            error -> log.error("Failed to register commands", error)
        );
  }
}
