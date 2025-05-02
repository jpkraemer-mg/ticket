package quest.darkoro.ticket.commands;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.springframework.stereotype.Component;
import quest.darkoro.ticket.interfaces.BaseCommand;

@Component
public class RewardCommand implements BaseCommand {

  @Override
  public CommandData create() {
    return Commands.slash("reward", "Base command for reward related commands")
      .addSubcommandGroups(new SubcommandGroupData("tier", "Tier related commands")
        .addSubcommands(new SubcommandData("create", "Create a new Reward Tier")
          .addOption(STRING, "name", "Name of the Reward Tier", true))
        .addSubcommands(new SubcommandData("delete", "Delete a Reward Tier")))
      .addSubcommands(new SubcommandData("create", "Create a new Reward")
        .addOption(STRING, "name", "Name of the Reward", true))
      .addSubcommands(new SubcommandData("delete", "Delete a Reward"))
      .setGuildOnly(true)
      .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
  }
}
