package quest.darkoro.ticket.listeners.secondary.component;

import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.persistence.model.Selfrole;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.persistence.repository.SelfroleRepository;
import quest.darkoro.ticket.util.MessageUtil;

@Service
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
    var member = e.getMember();
    var existing = selfroleRepository
        .findByGuildId(gid)
        .stream()
        .map(Selfrole::getRoleId)
        .collect(Collectors.toSet());

    var roles = e.getValues()
        .stream()
        .filter(r -> r instanceof Role)
        .map(r -> (Role) r)
        .filter(r -> !r.isPublicRole())
        .filter(r -> existing.contains(r.getIdLong()))
        .filter(r -> e.getGuild().getSelfMember().canInteract(r))
        .map(Role::getName)
        .toList();

    var removed = e.getValues()
        .stream()
        .filter(r -> r instanceof Role)
        .map(r -> (Role) r)
        .filter(r -> !r.isPublicRole())
        .filter(r -> existing.contains(r.getIdLong()))
        .filter(r -> e.getGuild().getSelfMember().canInteract(r))
        .peek(r -> selfroleRepository.deleteByGuildIdAndRoleId(gid, r.getIdLong()))
        .map(Role::getAsMention)
        .toList();

    e.reply("Removed the following roles from self-assignable roles:\n%s".formatted(
        String.join("\n", removed))).setEphemeral(true).queue();
    var guild = guildRepository.findById(gid).orElse(null);
    if (guild != null) {
      if (guild.getRole() != null) {
        messageUtil.sendRoleMessage(e.getGuild().getTextChannelById(guild.getRole()), e.getJDA());
      }
      if (guild.getLog() != null) {
        messageUtil.sendLogMessage(
            "Command `%s` executed by `%s (%s)`\nSELF-ASSIGNABLE ROLE(S) REMOVE `%s`".formatted(
                "/configure selfrole remove",
                member.getEffectiveName(),
                member.getIdLong(),
                roles
            ), e.getGuild().getTextChannelById(guild.getLog()));
      }
    }
  }
}
