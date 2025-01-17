package quest.darkoro.ticket.commands;

import static net.dv8tion.jda.api.interactions.commands.OptionType.CHANNEL;
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
    return Commands.slash("configure", "Configure the ticket system")
        .addSubcommandGroups(
            new SubcommandGroupData("category", "Ticket category configuration")
                .addSubcommands(new SubcommandData("add", "Add a ticket category")
                    .addOptions(
                        new OptionData(STRING, "name", "Name of the category", true),
                        new OptionData(STRING, "description", "Description of the category", true),
                        new OptionData(ROLE, "role1", "Default assigned role for this category"),
                        new OptionData(ROLE, "role2", "Default assigned role for this category"),
                        new OptionData(ROLE, "role3", "Default assigned role for this category"),
                        new OptionData(ROLE, "role4", "Default assigned role for this category")
                    )
                )
                .addSubcommands(new SubcommandData("remove", "Remove a ticket category")
                    .addOption(CHANNEL, "category", "Category to remove", true))
        )
        .addSubcommandGroups(new SubcommandGroupData("channel", "Configure different channels")
            .addSubcommands(new SubcommandData("ticket", "Channel from which to create tickets")
              .addOption(CHANNEL, "channel", "Channel to create tickets in", true))
            .addSubcommands(new SubcommandData("transcript", "Configure a transcript channel")
                .addOption(CHANNEL, "transcript", "Channel to save transcripts in", true))
            .addSubcommands(new SubcommandData("roles", "Configure the self-assignable roles channel")
                .addOption(CHANNEL, "roles", "Channel to self-assign roles from", true))
            .addSubcommands(new SubcommandData("log", "Configure the log channel for all relevant commands")
                .addOption(CHANNEL, "log", "Channel to log commands to", true))
            .addSubcommands(new SubcommandData("setup", "Setup all channels under /configure channel command")
                .addOption(CHANNEL, "category", "Category to set all these channels up in", true))
        )
        .addSubcommandGroups(new SubcommandGroupData("selfrole", "Self-assignable role configuration")
            .addSubcommands(new SubcommandData("add", "Add a self-assignable role"))
            .addSubcommands(new SubcommandData("remove", "Remove a self-assignable role"))
        )
        .setGuildOnly(true)
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR,
            Permission.MANAGE_CHANNEL));
  }
}
