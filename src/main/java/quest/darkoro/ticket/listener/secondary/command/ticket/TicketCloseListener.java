package quest.darkoro.ticket.listener.secondary.command.ticket;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@SecondaryListener
@Slf4j
@RequiredArgsConstructor
public class TicketCloseListener extends ListenerAdapter {

  private final PermissionUtil permissionUtil;

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("ticket") || !"close".equals(
        e.getSubcommandName())) {
      return;
    }

    if (!permissionUtil.validCategory(e.getGuild().getIdLong(), e.getChannel().asTextChannel()
        .getParentCategoryIdLong())) {
      e.reply("This channel doesn't seem to be a ticket!").setEphemeral(true).queue();
      return;
    }
    var channel = e.getChannel().asTextChannel();
    channel.getMemberPermissionOverrides().forEach(p -> p.delete().queue());
    channel.getManager().setName("closed-%s".formatted(channel.getName())).queue();
    e.reply("Ticket succesfully closed by **%s** and all non-role members removed."
        .formatted(e.getMember().getEffectiveName())).queue();
  }
}
