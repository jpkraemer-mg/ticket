package quest.darkoro.ticket.listener.secondary.command;

import static net.dv8tion.jda.api.entities.channel.ChannelType.CATEGORY;

import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.model.Guild;
import quest.darkoro.ticket.persistence.repository.AdministratorRepository;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.util.MessageUtil;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@SecondaryListener
@Slf4j
@RequiredArgsConstructor
public class ConfigureChannelSetupCommandListener extends ListenerAdapter {

  private final PermissionUtil permissionUtil;
  private final GuildRepository guildRepository;
  private final AdministratorRepository administratorRepository;
  private final MessageUtil messageUtil;

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("configure") || !"channel".equals(e.getSubcommandGroup()) || !"setup".equals(e.getSubcommandName())) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var guild = e.getGuild();
      var g = guildRepository.findById(gid).orElse(new Guild());
      var setupCategory = e.getOption("category").getAsChannel();
      if (setupCategory.getType() != CATEGORY) {
        e.reply("You may only select a **category** type channel to use for setup!").setEphemeral(true).queue();
        return;
      }
      var setupCat = setupCategory.asCategory();
      var log = e.getGuild().createTextChannel("command-logging").setParent(setupCat);
      var ticket = e.getGuild().createTextChannel("create-a-ticket").setParent(setupCat);
      var transcript = e.getGuild().createTextChannel("ticket-transcripts").setParent(setupCat);
      var selfRoles = e.getGuild().createTextChannel("self-assign-roles").setParent(setupCat);
      List<Role> roles = new ArrayList<>();
      administratorRepository.getAllByGuildId(gid).forEach(a -> roles.add(e.getGuild().getRoleById(a.getRoleId())));
      List.of(log, ticket, transcript, selfRoles)
          .forEach(c -> {
            roles.forEach(
                r -> c.addRolePermissionOverride(r.getIdLong(), permissionUtil.getAllow(), null)
            );
            c.addRolePermissionOverride(e.getGuild().getBotRole().getIdLong(), permissionUtil.getBotPermissions(), null);
            c.addRolePermissionOverride(e.getGuild().getPublicRole().getIdLong(), null, permissionUtil.getDeny());
          });
      var logDone = log.complete();
      var ticketDone = ticket.complete();
      var transcriptDone = transcript.complete();
      var selfRolesDone = selfRoles.complete();
      g.setBase(ticketDone.getIdLong()).setLog(logDone.getIdLong()).setRole(
          selfRolesDone.getIdLong()).setTranscript(transcriptDone.getIdLong());
      messageUtil.sendTicketMessage(ticketDone, e.getJDA());
      messageUtil.sendRoleMessage(selfRolesDone, e.getJDA());
      messageUtil.sendLogMessage("Channel setup executed by `%s (%s)`".formatted(member.getEffectiveName(), member.getIdLong()), logDone);
    }
  }
}
