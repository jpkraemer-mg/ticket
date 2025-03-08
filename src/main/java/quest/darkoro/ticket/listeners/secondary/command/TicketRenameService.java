package quest.darkoro.ticket.listeners.secondary.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class TicketRenameService {

  private final PermissionUtil permissionUtil;

  public void handleTicketRename(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    boolean isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      if (!permissionUtil.validCategory(e.getGuild().getIdLong(), e.getChannel().asTextChannel()
          .getParentCategoryIdLong())) {
        e.reply("This channel doesn't seem to be a ticket!").setEphemeral(true).queue();
        return;
      }
      e.getChannel().asTextChannel().getManager().setName(e.getOption("new_name").getAsString())
          .queue();
      e.reply("Ticket renamed to `%s` by `%s (%s)`".formatted(e.getOption("new_name").getAsString(),
          member.getEffectiveName(), member.getIdLong())).queue();
    }
  }
}
