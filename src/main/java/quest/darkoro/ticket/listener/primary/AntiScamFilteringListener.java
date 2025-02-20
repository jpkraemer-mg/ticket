package quest.darkoro.ticket.listener.primary;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.PrimaryListener;

@Slf4j
@RequiredArgsConstructor
@PrimaryListener
@Service
public class AntiScamFilteringListener extends ListenerAdapter {

  @Override
  public void onMessageReceived(@NonNull MessageReceivedEvent e) {
    if (e.getAuthor().isBot()) {
      return;
    }

    if (e.getMessage().getContentRaw().contains("https://discord.gg/")) {
      e.getMessage().delete().queue();
      e.getChannel().sendMessage("Scamming is not allowed!").queue();
    }
  }
}
