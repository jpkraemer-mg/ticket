package quest.darkoro.ticket.listener.secondary.component;

import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.model.Selfrole;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.persistence.repository.SelfroleRepository;
import quest.darkoro.ticket.util.MessageUtil;

@Service
@SecondaryListener
@Slf4j
@RequiredArgsConstructor
public class ConfigureSelfroleRemoveSelectListener extends ListenerAdapter {

  private final SelfroleRepository selfroleRepository;
  private final MessageUtil messageUtil;
  private final GuildRepository guildRepository;

  @Override
  public void onEntitySelectInteraction(@NonNull EntitySelectInteractionEvent e) {
    if (e.isAcknowledged() || !e.getComponentId().equals("selfrole_remove")) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var existing = selfroleRepository
        .findByGuildId(gid)
        .stream()
        .map(Selfrole::getRoleId)
        .collect(Collectors.toSet());

    var removed = e.getValues()
        .stream()
        .filter(r -> r instanceof Role)
        .map(r -> (Role) r)
        .filter(r -> !r.isPublicRole())
        .filter(r -> existing.contains(r.getIdLong()))
        .peek(r -> selfroleRepository.deleteByGuildIdAndRoleId(gid, r.getIdLong()))
        .map(Role::getAsMention)
        .toList();

    e.reply("Removed the following roles from self-assignable roles:\n%s".formatted(
        String.join("\n", removed))).setEphemeral(true).queue();
    messageUtil.sendRoleMessage(e.getGuild().getTextChannelById(guildRepository.findById(gid).get().getRole()), e.getJDA());
  }
}
