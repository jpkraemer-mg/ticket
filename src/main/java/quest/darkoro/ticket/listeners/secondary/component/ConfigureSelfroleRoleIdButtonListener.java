package quest.darkoro.ticket.listeners.secondary.component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigureSelfroleRoleIdButtonListener extends ListenerAdapter {

  @Override
  public void onButtonInteraction(@NonNull ButtonInteractionEvent e) {
    if (e.isAcknowledged() || !e.getButton().getId().startsWith("selfrole_")) {
      return;
    }
    if (!e.getGuild().getSelfMember().canInteract(e.getMember())) {
      e.reply(
              "I can't add or remove this role from you!\nThis most likely is because you have a higher role than me!")
          .setEphemeral(true).queue();
      return;
    }
    var rid = e.getButton().getId().substring(e.getButton().getId().lastIndexOf('_') + 1);
    var role = e.getGuild().getRoleById(rid);
    if (role == null) {
      e.reply("This role does not exist! Please open a ticket and contact an Admin about this!")
          .setEphemeral(true).queue();
      return;
    }
    if (e.getMember().getRoles().contains(role)) {
      e.getGuild().removeRoleFromMember(e.getMember(), role).queue();
    } else {
      e.getGuild().addRoleToMember(e.getMember(), role).queue();
    }
    e.reply("Role " + role.getAsMention() + " has been " + (!e.getMember().getRoles().contains(role)
        ? "added" : "removed") + "!").setEphemeral(true).queue();
  }
}
