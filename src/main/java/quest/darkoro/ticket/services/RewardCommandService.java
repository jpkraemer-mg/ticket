package quest.darkoro.ticket.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.persistence.GuildRepository;
import quest.darkoro.ticket.persistence.RewardRepository;
import quest.darkoro.ticket.persistence.RewardTierRepository;
import quest.darkoro.ticket.persistence.model.Guild;
import quest.darkoro.ticket.persistence.model.Reward;
import quest.darkoro.ticket.persistence.model.RewardTier;
import quest.darkoro.ticket.util.MessageUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class RewardCommandService extends ListenerAdapter {

  private final RewardRepository rewardRepository;
  private final RewardTierRepository rewardTierRepository;
  private final GuildRepository guildRepository;
  private final MessageUtil messageUtil;

  public void handleCreateRewardTier(SlashCommandInteractionEvent e) {
    var guild = e.getGuild();
    var member = e.getMember();
    var g = guildRepository.findById(guild.getIdLong()).orElse(new Guild());

    var name = e.getOption("name").getAsString();
    rewardTierRepository.save(new RewardTier().setName(name).setGuildId(guild.getIdLong()));

    e.reply("Reward tier created: `%s`".formatted(name)).setEphemeral(true).queue();
    if (g.getLog() != null) {
      messageUtil.sendLogMessage(
          "Command `%s` executed by `%s (%s)`\nCREATE BUG REWARD TIER --> `%s`".formatted(
              "/reward tier create",
              member.getEffectiveName(),
              member.getIdLong(),
              name
      ), guild.getTextChannelById(g.getLog()));
    }
  }

  public void handleCreateReward(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var tiers = rewardTierRepository.findByGuildId(gid);
    if (tiers.isEmpty()) {
      e.reply("No reward tiers have been created yet!").setEphemeral(true).queue();
      return;
    }

    var name = e.getOption("name").getAsString();
    var tempReward = rewardRepository.save(new Reward().setName(name).setGuildId(gid));
    var menu = StringSelectMenu.create("createreward_%s".formatted(tempReward.getId()))
        .setPlaceholder("Choose a Reward Tier");

    for (var tier : rewardTierRepository.findByGuildId(gid)) {
      menu.addOption(tier.getName(), tier.getId().toString());
    }

    e.reply("").addActionRow(menu.build()).setEphemeral(true).queue();
  }

  public void handleDeleteReward(SlashCommandInteractionEvent e) {
    var gid = e.getGuild().getIdLong();
    var tiers = rewardTierRepository.findByGuildId(gid);
    if (tiers.isEmpty()) {
      e.reply("No reward tiers have been created yet!").setEphemeral(true).queue();
      return;
    }

    var menu = StringSelectMenu.create("deletechoosetier").setPlaceholder("What Reward Tier is the Reward in?");

    for (var tier : tiers) {
      menu.addOption(tier.getName(), tier.getId().toString());
    }

    e.reply("").addActionRow(menu.build()).setEphemeral(true).queue();
  }
}
