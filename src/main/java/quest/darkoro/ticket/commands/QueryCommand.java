package quest.darkoro.ticket.commands;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import quest.darkoro.ticket.interfaces.BaseCommand;

@Component
public class QueryCommand implements BaseCommand {

  @Override
  public CommandData create() {
    return Commands.slash("query", "Query the Minecraft API for a player's UUID")
        .addOption(STRING, "username", "Username of the person to query", true)
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.ENABLED);
  }
}
