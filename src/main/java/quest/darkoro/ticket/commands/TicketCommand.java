package quest.darkoro.ticket.commands;

import static net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions.ENABLED;
import static net.dv8tion.jda.api.interactions.commands.OptionType.CHANNEL;
import static net.dv8tion.jda.api.interactions.commands.OptionType.ROLE;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;
import static net.dv8tion.jda.api.interactions.commands.OptionType.USER;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.springframework.stereotype.Component;
import quest.darkoro.ticket.interfaces.BaseCommand;

@Component
public class TicketCommand implements BaseCommand {

  @Override
  public CommandData create() {
    return Commands.slash("ticket", "Base Command for all ticket commands")
        .addSubcommandGroups(new SubcommandGroupData("user", "User related commands")
            .addSubcommands(
                new SubcommandData("add", "Add a user to the ticket")
                    .addOption(USER, "user", "The user to add", true)
            )
            .addSubcommands(
                new SubcommandData("remove", "Remove a user from the ticket")
                    .addOption(USER, "user", "The user to remove", true)
            )
        )
        .addSubcommandGroups(new SubcommandGroupData("role", "Role related commands")
            .addSubcommands(
                new SubcommandData("add", "Add a role to the ticket")
                    .addOption(ROLE, "role", "The role to add", true)
            )
            .addSubcommands(
                new SubcommandData("remove", "Remove a role from the ticket")
                    .addOption(ROLE, "role", "The role to remove", true)
            )
        )
        .addSubcommandGroups(new SubcommandGroupData("admins", "Admin related commands")
            .addSubcommands(
                new SubcommandData("add", "Add a role to ticket administrators")
                    .addOption(ROLE, "role", "Role to add", true)
            )
            .addSubcommands(
                new SubcommandData("remove", "Remove a role from ticket administrators")
                    .addOption(ROLE, "role", "Role to remove", true)
            )
        )
        .addSubcommands(new SubcommandData("create", "Create a ticket"))
        .addSubcommands(new SubcommandData("rename", "Rename the ticket")
            .addOption(STRING, "new_name", "The new name for the ticket", true))
        //.addSubcommands(new SubcommandData("close", "Close the current ticket"))
        .addSubcommands(new SubcommandData("move", "Move the ticket to another category")
            .addOption(CHANNEL, "category", "The category to move this ticket to", true))
        //.addSubcommands(new SubcommandData("delete", "Delete the ticket"))
        .setGuildOnly(true)
        .setDefaultPermissions(ENABLED);
  }
}
