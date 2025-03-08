package quest.darkoro.ticket.listeners.secondary.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketRoleRemoveService {

  private final PermissionUtil permissionUtil;

  public void handleTicketRoleRemove(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      if (!permissionUtil.validCategory(gid, e.getChannel().asTextChannel()
          .getParentCategoryIdLong())) {
        e.reply("This channel doesn't seem to be a ticket!").setEphemeral(true).queue();
        return;
      }
      var type = e.getChannel().getType();
      switch (type) {
        case TEXT -> {
          var role = e.getOption("role").getAsRole();
          e.getChannel().asTextChannel().getManager().removePermissionOverride(role.getIdLong())
              .queue();
          e.reply("Role `%s` removed from ticket by `%s (%s)`".formatted(role.getAsMention(),
              member.getEffectiveName(), member.getIdLong())).queue();
        }
        default ->
            e.reply("This command can only be used in a text channel").setEphemeral(true).queue();
      }
    }
  }
}
