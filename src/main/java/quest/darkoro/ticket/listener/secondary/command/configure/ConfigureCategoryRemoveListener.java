package quest.darkoro.ticket.listener.secondary.command.configure;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.repository.CategoryRepository;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@SecondaryListener
@Slf4j
@RequiredArgsConstructor
public class ConfigureCategoryRemoveListener extends ListenerAdapter {

  private final PermissionUtil permissionUtil;
  private final CategoryRepository categoryRepository;

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("configure") || !"category".equals(
        e.getSubcommandGroup()) || !"remove".equals(e.getSubcommandName())) {
      return;
    }

    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var channel = e.getOption("category").getAsChannel();
      if (channel.getType() != ChannelType.CATEGORY) {
        e.reply("You may only select a **category** type channel!").setEphemeral(true).queue();
        return;
      }

      var category = categoryRepository.findById(channel.getIdLong()).orElse(null);

      if (category == null) {
        e.reply("This category is not a configured ticket category!").setEphemeral(true).queue();
        return;
      }
      var name = channel.getName();
      channel.delete().queue();
      categoryRepository.delete(category);
      e.reply("Category %s removed".formatted(name)).setEphemeral(true).queue();
    }

  }
}
