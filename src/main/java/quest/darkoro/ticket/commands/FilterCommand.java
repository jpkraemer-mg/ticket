package quest.darkoro.ticket.commands;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.stereotype.Component;
import quest.darkoro.ticket.interfaces.BaseCommand;

@Component
public class FilterCommand implements BaseCommand {

  @Override
  public CommandData create() {
    return Commands.slash("filter", "")
        .addSubcommands(new SubcommandData("add", "Add a message content filter")
            .addOption(STRING, "filter", "The filter to add", true))
        .addSubcommands(new SubcommandData("remove", "Remove a message content filter")
            .addOption(STRING, "filter", "The filter to remove", true))
        .addSubcommands(new SubcommandData("list", "List all message content filters"))
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(List.of(Permission.MESSAGE_MANAGE, Permission.MANAGE_SERVER))
        );
  }
}
