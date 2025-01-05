package quest.darkoro.ticket.commands;

import static net.dv8tion.jda.api.interactions.commands.OptionType.ROLE;
import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.springframework.stereotype.Component;
import quest.darkoro.ticket.interfaces.BaseCommand;

@Component
public class ConfigureCommand implements BaseCommand {

  @Override
  public CommandData create() {
    return Commands.slash(
            "configure",
            "Configure the ticket system"
        )
        .addSubcommandGroups(
            new SubcommandGroupData("roles", "Default role configuration")
                .addSubcommands(new SubcommandData(
                    "staff", "Configure the staff role to add to all tickets")
                    .addOption(ROLE, "role", "Staff role", true)
                )
        )
        .addSubcommandGroups(
            new SubcommandGroupData("category", "Ticket category configuration")
                .addSubcommands(new SubcommandData(
                        "add", "Add a ticket category"
                    )
                        .addOptions(
                            new OptionData(STRING, "name", "Name of the category", true),
                            new OptionData(STRING, "description", "Description of the category", true),
                            new OptionData(ROLE, "role1", "Default assigned role for this category",
                                true),
                            new OptionData(ROLE, "role2", "Default assigned role for this category"),
                            new OptionData(ROLE, "role3", "Default assigned role for this category"),
                            new OptionData(ROLE, "role4", "Default assigned role for this category")
                        )
                )
        )
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR,
            Permission.MANAGE_CHANNEL));
  }
}
