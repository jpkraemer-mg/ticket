package quest.darkoro.ticket.listeners.secondary.command;

import static net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget.ROLE;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.util.PermissionUtil;

@Slf4j
@RequiredArgsConstructor
@Service
public class ConfigureSelfroleRemoveService {

  private final PermissionUtil permissionUtil;

  public void handleSelfroleRemove(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var menu = EntitySelectMenu.create("selfrole_remove", ROLE)
          .setPlaceholder("Select roles that should no longer be self-assignable")
          .setMinValues(1)
          .setMaxValues(10)
          .build();
      e.reply("")
          .setActionRow(menu)
          .setEphemeral(true)
          .queue();
    }
  }
}
