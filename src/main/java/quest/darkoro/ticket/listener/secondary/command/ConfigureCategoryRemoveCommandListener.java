package quest.darkoro.ticket.listener.secondary.command;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;
import quest.darkoro.ticket.persistence.repository.CategoryRepository;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.util.MessageUtil;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@SecondaryListener
@Slf4j
@RequiredArgsConstructor
public class ConfigureCategoryRemoveCommandListener extends ListenerAdapter {

  private final PermissionUtil permissionUtil;
  private final CategoryRepository categoryRepository;
  private final GuildRepository guildRepository;
  private final MessageUtil messageUtil;

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
      var g = guildRepository.findById(gid).orElse(null);
      if (g != null) {
        if (g.getBase() != null) {
          messageUtil.sendTicketMessage(e.getGuild().getTextChannelById(g.getBase()), e.getJDA());
        }
        if (g.getLog() != null) {
          messageUtil.sendLogMessage("Command `%s` executed by `%s (%s)`\nCATEGORY REMOVE: `%s (%s)`".formatted(
              "/configure category remove",
              e.getMember().getEffectiveName(),
              e.getMember().getIdLong(),
              name,
              category.getId()), e.getGuild().getTextChannelById(g.getLog())
          );
        }
      }
    }

  }
}
