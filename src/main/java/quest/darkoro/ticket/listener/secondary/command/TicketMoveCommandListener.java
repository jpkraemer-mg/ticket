package quest.darkoro.ticket.listener.secondary.command;

import static net.dv8tion.jda.api.entities.channel.ChannelType.CATEGORY;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@Slf4j
@RequiredArgsConstructor
@SecondaryListener
public class TicketMoveCommandListener extends ListenerAdapter {

  private final PermissionUtil permissionUtil;

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("ticket") || !"move".equals(
        e.getSubcommandName())) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      if (!permissionUtil.validCategory(gid, e.getChannel().asTextChannel()
          .getParentCategoryIdLong())) {
        e.reply("This channel doesn't seem to be a ticket!").setEphemeral(true).queue();
        return;
      }
      var category = e.getOption("category").getAsChannel();
      var manager = e.getChannel().asTextChannel().getManager();
      if (!category.getType().equals(CATEGORY)) {
        e.reply("You must select a **category** type channel to move this ticket to!")
            .setEphemeral(true).queue();
        return;
      }
      manager.setParent(category.asCategory()).queue();
      e.reply("Ticket moved to %s".formatted(category.getName())).setEphemeral(true).queue();
    }
  }
}
