package quest.darkoro.ticket.listeners.secondary.command;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.persistence.model.Category;
import quest.darkoro.ticket.persistence.repository.CategoryRepository;
import quest.darkoro.ticket.persistence.repository.GuildRepository;
import quest.darkoro.ticket.util.MessageUtil;
import quest.darkoro.ticket.util.PermissionUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigureCategoryAddService {

  private final PermissionUtil permissionUtil;
  private final CategoryRepository categoryRepository;
  private final MessageUtil messageUtil;
  private final GuildRepository guildRepository;

  public void handleConfigureCategoryAdd(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var member = e.getMember();
    var isPermitted = permissionUtil.isPermitted(e, gid, member);

    if (isPermitted) {
      var guild = e.getGuild();

      var roles = Stream.of("role1", "role2", "role3", "role4")
          .map(e::getOption)
          .filter(Objects::nonNull)
          .map(OptionMapping::getAsRole)
          .toList();

      var mentions = roles.stream().map(IMentionable::getAsMention)
          .collect(Collectors.joining(", "));

      roles = Stream.concat(roles.stream(), permissionUtil.getRoles(gid).stream()).distinct()
          .toList();

      var cat = categoryRepository.findByNameAndGuildId(e.getOption("name").getAsString(), gid);
      if (cat != null) {
        e.reply("Category with that name already exists").setEphemeral(true).queue();
        return;
      }

      var category = guild.createCategory(e.getOption("name").getAsString());
      var open = e.getOption("open") != null && e.getOption("open").getAsBoolean();
      if (!open) {
        roles.forEach(
            r -> category.addRolePermissionOverride(r.getIdLong(), permissionUtil.getAllow(),
                permissionUtil.getFilteredDeny()));
        category.addRolePermissionOverride(guild.getBotRole().getIdLong(),
            permissionUtil.getBotPermissions(), null);
        category.addRolePermissionOverride(guild.getPublicRole().getIdLong(), null,
            permissionUtil.getDeny());
      }
      var completeCategory = category.complete();

      categoryRepository.save(new Category()
          .setId(completeCategory.getIdLong())
          .setDescription(e.getOption("description").getAsString())
          .setGuildId(gid)
          .setName(e.getOption("name").getAsString())
          .setMentions(mentions)
      );
      e.reply("Category added with roles\n%s".formatted(mentions)).setEphemeral(true).queue();
      var g = guildRepository.findById(gid).orElse(null);
      if (g != null) {
        if (g.getBase() != null) {
          messageUtil.sendTicketMessage(guild.getTextChannelById(g.getBase()), e.getJDA());
        }
        if (g.getLog() != null) {
          messageUtil.sendLogMessage(
              "Command `%s` executed by `%s (%s)`\nCATEGORY CREATE `%s (%s)`\n`%s`".formatted(
                  "/configure category add",
                  member.getEffectiveName(),
                  member.getIdLong(),
                  completeCategory.getName(),
                  completeCategory.getIdLong(),
                  !roles.isEmpty() ? roles.stream().map(Role::getName)
                      .collect(Collectors.joining(", ")) : "No roles assigned to category"
              ), guild.getTextChannelById(g.getLog()));
        }
      }
    }
  }
}
