package quest.darkoro.ticket.listener.primary;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.TertiaryListener;
import quest.darkoro.ticket.listener.secondary.command.ConfigureCategoryAddService;
import quest.darkoro.ticket.listener.secondary.command.ConfigureCategoryRemoveService;
import quest.darkoro.ticket.listener.secondary.command.ConfigureChannelLogService;
import quest.darkoro.ticket.listener.secondary.command.ConfigureChannelRoleService;
import quest.darkoro.ticket.listener.secondary.command.ConfigureChannelSetupService;
import quest.darkoro.ticket.listener.secondary.command.ConfigureChannelTicketService;
import quest.darkoro.ticket.listener.secondary.command.ConfigureChannelTranscriptService;
import quest.darkoro.ticket.listener.secondary.command.ConfigureSelfroleAddService;
import quest.darkoro.ticket.listener.secondary.command.ConfigureSelfroleRemoveService;
import quest.darkoro.ticket.listener.secondary.command.FilterAddService;
import quest.darkoro.ticket.listener.secondary.command.FilterListService;
import quest.darkoro.ticket.listener.secondary.command.FilterRemoveService;
import quest.darkoro.ticket.listener.secondary.command.HelpService;
import quest.darkoro.ticket.listener.secondary.command.QueryService;
import quest.darkoro.ticket.listener.secondary.command.TicketAdminsAddService;
import quest.darkoro.ticket.listener.secondary.command.TicketAdminsRemoveService;
import quest.darkoro.ticket.listener.secondary.command.TicketMoveService;
import quest.darkoro.ticket.listener.secondary.command.TicketRenameService;
import quest.darkoro.ticket.listener.secondary.command.TicketRoleAddService;
import quest.darkoro.ticket.listener.secondary.command.TicketRoleRemoveService;
import quest.darkoro.ticket.listener.secondary.command.TicketUserAddService;
import quest.darkoro.ticket.listener.secondary.command.TicketUserRemoveService;

@TertiaryListener
@RequiredArgsConstructor
@Slf4j
@Service
public class SlashCommandInteractionListener extends ListenerAdapter {

  private final HelpService helpService;
  private final QueryService queryService;
  private final FilterAddService filterAddService;
  private final FilterListService filterListService;
  private final FilterRemoveService filterRemoveService;
  private final ConfigureCategoryAddService configureCategoryAddService;
  private final ConfigureCategoryRemoveService configureCategoryRemoveService;
  private final ConfigureChannelRoleService configureChannelRoleService;
  private final ConfigureChannelTicketService configureChannelTicketService;
  private final ConfigureChannelTranscriptService configureChannelTranscriptService;
  private final ConfigureChannelLogService configureChannelLogService;
  private final ConfigureChannelSetupService configureChannelSetupService;
  private final ConfigureSelfroleAddService configureSelfroleAddService;
  private final ConfigureSelfroleRemoveService configureSelfroleRemoveService;
  private final TicketAdminsAddService ticketAdminsAddService;
  private final TicketRoleAddService ticketRoleAddService;
  private final TicketUserAddService ticketUserAddService;
  private final TicketRoleRemoveService ticketRoleRemoveService;
  private final TicketAdminsRemoveService ticketAdminsRemoveService;
  private final TicketUserRemoveService ticketUserRemoveService;
  private final TicketMoveService ticketMoveService;
  private final TicketRenameService ticketRenameService;

  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    var command = e.getName();
    var subcommandGroup = e.getSubcommandGroup() != null ? e.getSubcommandGroup() : "";
    var subcommand = e.getSubcommandName() != null ? e.getSubcommandName() : "";

    switch (command) {
      case "help" -> helpService.handleHelp(e);
      case "query" -> queryService.handleQuery(e);
      case "filter" -> {
        switch (subcommand) {
          case "add" -> filterAddService.handleFilterAdd(e);
          case "list" -> filterListService.onSlashCommandInteraction(e);
          case "remove" -> filterRemoveService.onSlashCommandInteraction(e);
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
              case "add" -> configureCategoryAddService.handleConfigureCategoryAdd(e);
              case "remove" -> configureCategoryRemoveService.handleConfigureCategoryRemove(e);
              default -> {
                log.warn("Unknown subcommand {} for subcommandGroup {} of command {}", subcommand, subcommandGroup, command);
                e.reply("Unknown subcommand!").setEphemeral(true).queue();
              }
            }
          }
          case "channel" -> {
            switch (subcommand) {
              case "role" -> configureChannelRoleService.handleConfigureChannelRole(e);
              case "ticket" -> configureChannelTicketService.handleConfigureChannelTicket(e);
              case "transcript" -> configureChannelTranscriptService.handleConfigureChannelTranscript(e);
              case "log" -> configureChannelLogService.handleConfigureChannelLog(e);
              case "setup" -> configureChannelSetupService.handleConfigureChannelSetup(e);
              default -> {
                log.warn("Unknown subcommand {} for subcommandGroup {} of command {}", subcommand, subcommandGroup, command);
                e.reply("Unknown subcommand!").setEphemeral(true).queue();
              }
            }
          }
          case "selfrole" -> {
            switch (subcommand) {
              case "add" -> configureSelfroleAddService.handleSelfroleAdd(e);
              case "remove" -> configureSelfroleRemoveService.handleSelfroleRemove(e);
              default -> {
                log.warn("Unknown subcommand {} for subcommandGroup {} of command {}", subcommand, subcommandGroup, command);
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
              case "admins" -> ticketAdminsAddService.handleTicketAdminsAdd(e);
              case "role" -> ticketRoleAddService.handleTicketRoleAdd(e);
              case "user" -> ticketUserAddService.handleTicketUserAdd(e);
              default -> {
                log.warn("Unknown subcommandGroup {} for subcommand {} of command {}", subcommandGroup, subcommand, command);
                e.reply("Unknown subcommand!").setEphemeral(true).queue();
              }
            }
          }
          case "remove" -> {
            switch (subcommandGroup) {
              case "admins" -> ticketAdminsRemoveService.handleTicketAdminsRemove(e);
              case "role" -> ticketRoleRemoveService.handleTicketRoleRemove(e);
              case "user" -> ticketUserRemoveService.handleTicketUserRemove(e);
              default -> {
                log.warn("Unknown subcommandGroup {} for subcommand {} of command {}", subcommandGroup, subcommand, command);
                e.reply("Unknown subcommand!").setEphemeral(true).queue();
              }
            }
          }
          case "move" -> ticketMoveService.handleTicketMove(e);
          case "rename" -> ticketRenameService.handleTicketRename(e);
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
  }
}
