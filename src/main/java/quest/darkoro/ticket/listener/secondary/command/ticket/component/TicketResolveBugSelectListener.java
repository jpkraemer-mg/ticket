package quest.darkoro.ticket.listener.secondary.command.ticket.component;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.repository.TicketRepository;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@SecondaryListener
@RequiredArgsConstructor
@Slf4j
public class TicketResolveBugSelectListener extends ListenerAdapter {

  private final TicketRepository ticketRepository;
  private final PermissionUtil permissionUtil;

  @Override
  public void onStringSelectInteraction(@NonNull StringSelectInteractionEvent e) {
    if (e.isAcknowledged() || !e.getComponentId().equals("resolve_bug")) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var selected = e.getSelectedOptions().get(0).getValue();

      e.getMessage().delete().queue();

      switch (selected) {
        case "t0" ->
            e.reply("User won't get pinged, no reward to be given out.").setEphemeral(true).queue();
        case "t1" -> {
          e.reply("User will be asked to choose any one T1 reward").setEphemeral(true).queue();
          var ticket = ticketRepository.getTicketByChannel(e.getChannel().getIdLong());
          e.getChannel()
              .asTextChannel()
              .sendMessage("%s, you may choose any one reward from **T1 Bug Report Rewards**"
                  .formatted(e.getGuild().retrieveMemberById(ticket.getCreator()).complete().getAsMention())
              )
              .queue();
        }
        case "t2" -> {
          e.reply("User will be asked to choose any one T1 or T2 reward").setEphemeral(true)
              .queue();
          var ticket = ticketRepository.getTicketByChannel(e.getChannel().getIdLong());
          e.getChannel()
              .asTextChannel()
              .sendMessage("%s, you may choose any one reward from **T1 or T2 Bug Report Rewards**"
                  .formatted(e.getGuild().retrieveMemberById(ticket.getCreator()).complete().getAsMention())
              )
              .queue();
        }
        case "t3" -> {
          e.reply("User will be asked to choose any one reward").setEphemeral(true).queue();
          var ticket = ticketRepository.getTicketByChannel(e.getChannel().getIdLong());
          e.getChannel()
              .asTextChannel()
              .sendMessage(
                  "%s, you may choose any one reward from **T1, T2 or T3 Bug Report Rewards**"
                      .formatted(e.getGuild().retrieveMemberById(ticket.getCreator()).complete().getAsMention())
              )
              .queue();
        }
      }
    }
  }
}
