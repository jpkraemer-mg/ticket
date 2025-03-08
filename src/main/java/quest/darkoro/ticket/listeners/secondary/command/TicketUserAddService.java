package quest.darkoro.ticket.listeners.secondary.command;

import static net.dv8tion.jda.api.Permission.MESSAGE_HISTORY;
import static net.dv8tion.jda.api.Permission.MESSAGE_SEND;
import static net.dv8tion.jda.api.Permission.VIEW_CHANNEL;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketUserAddService {

  private final PermissionUtil permissionUtil;

  public void handleTicketUserAdd(SlashCommandInteractionEvent e) {
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
          e.getChannel().asTextChannel().getManager().putMemberPermissionOverride(user.getIdLong(),
              List.of(MESSAGE_SEND, VIEW_CHANNEL, MESSAGE_HISTORY), new ArrayList<>()).queue();
          e.reply("User `%s` added to ticket by `%s (%s)`".formatted(user.getAsMention(),
              member.getEffectiveName(), member.getIdLong())).queue();
        }
        default ->
            e.reply("This command can only be used in a text channel").setEphemeral(true).queue();
      }
    }
  }
}
