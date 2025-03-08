package quest.darkoro.ticket.listener.secondary.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketUserRemoveService {

  private final PermissionUtil permissionUtil;

  public void handleTicketUserRemove(SlashCommandInteractionEvent e) {
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
          var user = e.getOption("user").getAsMember();
          e.getChannel().asTextChannel().getManager().removePermissionOverride(user.getIdLong())
              .queue();
          e.reply("User `%s` removed from ticket by `%s (%s)`".formatted(user.getAsMention(),
              member.getEffectiveName(), member.getIdLong())).queue();
        }
        default ->
            e.reply("This command can only be used in a text channel").setEphemeral(true).queue();
      }
    }
  }
}
