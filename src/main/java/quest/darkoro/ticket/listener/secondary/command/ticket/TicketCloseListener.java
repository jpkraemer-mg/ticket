package quest.darkoro.ticket.listener.secondary.command.ticket;

import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.DANGER;
import static net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle.PRIMARY;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@SecondaryListener
@Slf4j
@RequiredArgsConstructor
public class TicketCloseListener extends ListenerAdapter {

  private final PermissionUtil permissionUtil;

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("ticket") || !"close".equals(
        e.getSubcommandName())) {
      return;
    }

    if (!permissionUtil.validCategory(e.getGuild().getIdLong(), e.getChannel().asTextChannel()
        .getParentCategoryIdLong())) {
      e.reply("This channel doesn't seem to be a ticket!").setEphemeral(true).queue();
      return;
    }
    var channel = e.getChannel().asTextChannel();
    var embed = new EmbedBuilder().setDescription("```DO NOT DELETE NON-DUPLICATE / NON-SUPPORT TICKETS WITHOUT TRANSCRIPT!```").build();
    channel.getMemberPermissionOverrides().forEach(p -> p.delete().queue());
    channel.getManager().setName("closed-%s".formatted(channel.getName())).queue();
    e.reply("Ticket closed by **%s** and all non-role members removed."
        .formatted(e.getMember().getEffectiveName()))
        .addEmbeds(embed)
        .addActionRow(
            Button.of(DANGER, "delete_ticket", "DELETE", Emoji.fromUnicode("\uD83D\uDDD1\uFE0F")),
            Button.of(PRIMARY, "transcript", "TRANSCRIPT", Emoji.fromUnicode("\uD83D\uDCDD"))
        )
        .queue();
  }
}
