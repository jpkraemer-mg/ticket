package quest.darkoro.ticket.listener.secondary.command;

import static net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget.ROLE;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.util.PermissionUtil;

@SecondaryListener
@Slf4j
@RequiredArgsConstructor
@Service
public class ConfigureSelfroleRemoveCommandListener extends ListenerAdapter {

  private final PermissionUtil permissionUtil;

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("configure") || !"selfrole".equals(
        e.getSubcommandGroup()) || !"remove".equals(e.getSubcommandName())) {
      return;
    }

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
