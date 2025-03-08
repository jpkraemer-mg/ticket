package quest.darkoro.ticket.listener.secondary.command;

import static net.dv8tion.jda.api.entities.channel.ChannelType.CATEGORY;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketMoveService {

  private final PermissionUtil permissionUtil;

  public void handleTicketMove(SlashCommandInteractionEvent e) {
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
      e.reply("Ticket moved to `%s` by `%s (%s)`".formatted(category.getName(),
          member.getEffectiveName(), member.getIdLong())).queue();
    }
  }
}
