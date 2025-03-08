package quest.darkoro.ticket.listeners.primary;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.PrimaryListener;
import quest.darkoro.ticket.services.HelpCommandService;
import quest.darkoro.ticket.services.QueryCommandService;
import quest.darkoro.ticket.services.ConfigureCommandService;
import quest.darkoro.ticket.services.FilterCommandService;
import quest.darkoro.ticket.services.TicketCommandService;

@PrimaryListener
@RequiredArgsConstructor
@Slf4j
@Service
public class CommandListener extends ListenerAdapter {

  private final HelpCommandService helpCommandService;
  private final QueryCommandService queryCommandService;
  private final ConfigureCommandService configureCommandService;
  private final TicketCommandService ticketCommandService;
  private final FilterCommandService filterCommandService;

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.getGuild() == null) {
      e.reply("This command may only be used in a guild!").setEphemeral(true).queue();
      return;
    }

    var command = e.getName();
    var subcommandGroup = e.getSubcommandGroup() != null ? e.getSubcommandGroup() : "";
    var subcommand = e.getSubcommandName() != null ? e.getSubcommandName() : "";

    switch (command) {
      case "help" -> helpCommandService.handleHelp(e);
      case "query" -> queryCommandService.handleQuery(e);
      case "filter" -> {
        switch (subcommand) {
          case "add" -> filterCommandService.handleFilterAdd(e);
          case "list" -> filterCommandService.handleFilterList(e);
          case "remove" -> filterCommandService.handleFilterRemove(e);
          default -> {
            log.warn("Unknown subcommand {} for command {}", subcommand, command);
            e.reply("Unknown subcommand!").setEphemeral(true).queue();
          }
        }
      }
      case "configure" -> {
        switch (subcommandGroup) {
          case "category" -> {
            switch (subcommand) {
              case "add" -> configureCommandService.handleConfigureCategoryAdd(e);
              case "remove" -> configureCommandService.handleConfigureCategoryRemove(e);
              default -> {
                log.warn("Unknown subcommand {} for subcommandGroup {} of command {}", subcommand,
                    subcommandGroup, command);
                e.reply("Unknown subcommand!").setEphemeral(true).queue();
              }
            }
          }
          case "channel" -> {
            switch (subcommand) {
              case "roles" -> configureCommandService.handleConfigureChannelRole(e);
              case "ticket" -> configureCommandService.handleConfigureChannelTicket(e);
              case "transcript" -> configureCommandService.handleConfigureChannelTranscript(e);
              case "log" -> configureCommandService.handleConfigureChannelLog(e);
              case "setup" -> configureCommandService.handleConfigureChannelSetup(e);
              default -> {
                log.warn("Unknown subcommand {} for subcommandGroup {} of command {}", subcommand,
                    subcommandGroup, command);
                e.reply("Unknown subcommand!").setEphemeral(true).queue();
              }
            }
          }
          case "selfrole" -> {
            switch (subcommand) {
              case "add" -> configureCommandService.handleSelfroleAdd(e);
              case "remove" -> configureCommandService.handleSelfroleRemove(e);
              default -> {
                log.warn("Unknown subcommand {} for subcommandGroup {} of command {}", subcommand,
                    subcommandGroup, command);
                e.reply("Unknown subcommand!").setEphemeral(true).queue();
              }
            }
          }
          default -> {
            log.warn("Unknown subcommandGroup {}", subcommandGroup);
            e.reply("Unknown subcommand!").setEphemeral(true).queue();
          }
        }
      }
      case "ticket" -> {
        switch (subcommand) {
          case "add" -> {
            switch (subcommandGroup) {
              case "admins" -> ticketCommandService.handleTicketAdminsAdd(e);
              case "role" -> ticketCommandService.handleTicketRoleAdd(e);
              case "user" -> ticketCommandService.handleTicketUserAdd(e);
              default -> {
                log.warn("Unknown subcommandGroup {} for subcommand {} of command {}",
                    subcommandGroup, subcommand, command);
                e.reply("Unknown subcommand!").setEphemeral(true).queue();
              }
            }
          }
          case "remove" -> {
            switch (subcommandGroup) {
              case "admins" -> ticketCommandService.handleTicketAdminsRemove(e);
              case "role" -> ticketCommandService.handleTicketRoleRemove(e);
              case "user" -> ticketCommandService.handleTicketUserRemove(e);
              default -> {
                log.warn("Unknown subcommandGroup {} for subcommand {} of command {}",
                    subcommandGroup, subcommand, command);
                e.reply("Unknown subcommand!").setEphemeral(true).queue();
              }
            }
          }
          case "move" -> ticketCommandService.handleTicketMove(e);
          case "rename" -> ticketCommandService.handleTicketRename(e);
          default -> {
            log.warn("Unknown subcommand {} of command {}", subcommand, command);
            e.reply("Unknown subcommand!").setEphemeral(true).queue();
          }
        }
      }
      default -> {
        log.warn("Unknown command {}", command);
        e.reply("Unknown command!").setEphemeral(true).queue();
      }
    }
    if (e.isAcknowledged()) {
      return;
    }
    e.reply("Unknown (sub)command").setEphemeral(true).queue();
  }
}
