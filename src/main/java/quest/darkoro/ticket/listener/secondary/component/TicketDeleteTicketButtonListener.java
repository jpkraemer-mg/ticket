package quest.darkoro.ticket.listener.secondary.component;

import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.util.MessageUtil;
import quest.darkoro.ticket.util.PermissionUtil;

@SecondaryListener
@Slf4j
@RequiredArgsConstructor
@Service
public class TicketDeleteTicketButtonListener extends ListenerAdapter {

  private final PermissionUtil permissionUtil;
  private final GuildRepository guildRepository;
  private final MessageUtil messageUtil;

  @Override
  public void onButtonInteraction(@NonNull ButtonInteractionEvent e) {
    if (e.isAcknowledged() || !e.getButton().getId().equals("delete_ticket")) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var channel = e.getInteraction().getChannel();
      e.reply("Ticket will be deleted in 5 seconds.")
          .queue(c -> c.getInteraction().getChannel().delete().queueAfter(5, TimeUnit.SECONDS));

      var guild = guildRepository.findById(gid).orElse(null);
      if (guild != null) {
        if (guild.getLog() != null) {
          messageUtil.sendLogMessage(
              "Ticket deleted - `%s (%s)`\nDeleted by `%s (%s)`".formatted(
                  channel.getName(),
                  channel.getIdLong(),
                  member.getEffectiveName(),
                  member.getIdLong()
              ), e.getGuild().getTextChannelById(guild.getLog())
          );
        }
      }

    }

  }
}
