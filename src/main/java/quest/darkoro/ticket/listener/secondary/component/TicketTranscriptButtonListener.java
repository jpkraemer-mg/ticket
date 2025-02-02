package quest.darkoro.ticket.listener.secondary.component;

import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import quest.darkoro.transcripts.DiscordHtmlTranscripts;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.MissingAccessException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.repository.GuildRepository;

@SecondaryListener
@Slf4j
@RequiredArgsConstructor
@Service
public class TicketTranscriptButtonListener extends ListenerAdapter {

  private final GuildRepository guildRepository;

  @Override
  public void onButtonInteraction(@NonNull ButtonInteractionEvent e) {
    if (e.isAcknowledged() || !e.getButton().getId().equals("transcript")) {
      return;
    }

    var guild = e.getGuild();
    var gid = guild.getIdLong();

    var transcriptExist = guildRepository.findById(gid).isPresent();
    if (!transcriptExist) {
      e.reply("No configuration available!\nSet one using `/configure transcript`").setEphemeral(true).queue();
      return;
    }

    TextChannel transcript;
    var g = guildRepository.findById(gid).orElse(null);
    if (g != null && g.getTranscript() != null) {
      transcript = guild.getTextChannelById(guildRepository.findById(gid).get().getTranscript());
      if (transcript == null) {
        e.reply("Transcript channel not found!").setEphemeral(true).queue();
        return;
      }
    } else {
      e.reply("Transcript channel not found!").setEphemeral(true).queue();
      return;
    }

    var channel = e.getChannel().asTextChannel();

    try {
      transcript.sendFiles(DiscordHtmlTranscripts.getInstance()
          .createTranscript(channel, "transcript_%s.html".formatted(channel.getName().toLowerCase())))
          .queue();
    } catch (IOException ex) {
      e.reply("Error while creating transcript from channel '%s'".formatted(channel.getName())).queue();
      log.error("Error while creating transcript from channel '{}'", channel.getName(), ex);
      return;
    } catch (MissingAccessException ex) {
      e.reply("Missing access to transcript channel '%s'"
              .formatted(transcript.getName()))
          .setEphemeral(true)
          .queue();
      log.error("Missing access to transcript channel '{}'", transcript.getName(), ex);
      return;
    }

    e.reply("Transcript saved to %s!".formatted(transcript.getAsMention())).queue();
  }
}
