package quest.darkoro.ticket.listener.secondary.component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@SecondaryListener
@Slf4j
@RequiredArgsConstructor
public class TicketResolveTicketButtonListener extends ListenerAdapter {

  private final PermissionUtil permissionUtil;

  @Override
  public void onButtonInteraction(@NonNull ButtonInteractionEvent e) {
    if (e.isAcknowledged() || !e.getButton().getId().equals("resolve_ticket")) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var menu = StringSelectMenu.create("resolve_bug")
          .addOption("Resolved / Other", "t0")
          .addOption("Tier 1", "t1")
          .addOption("Tier 2", "t2")
          .addOption("Tier 3", "t3")
          .build();
      e.editButton(e.getButton().withDisabled(true)).queue();
      e.getChannel().asTextChannel().sendMessage("").setActionRow(menu).queue();
    }
  }
}
