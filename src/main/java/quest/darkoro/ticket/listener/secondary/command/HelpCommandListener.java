package quest.darkoro.ticket.listener.secondary.command;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.SecondaryListener;

@Service
@SecondaryListener
@Slf4j
@RequiredArgsConstructor
public class HelpCommandListener extends ListenerAdapter {
  @Override
  public void onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent e) {
    if (e.isAcknowledged() || !e.getName().equals("help")) {
      return;
    }
    e.reply(
        """
            /help - This message
            /ticket [user | role] [add | remove] - **Add** or **remove** the specified **user** or **role** to the currently selected ticket
            /ticket admins add - Add the specified role to ticket administrators, allowing them to:
            - Rename, move, close, transcribe and delete tickets
            - Add and remove **users** and **roles** to and from tickets
            - Set or change the transcript channel
            - Set or change the ticket create message channel
            /ticket admins remove - Remove the specified role from ticket administrators
            
            **Start by**:
            - Setting the base channel (`/configure channel`)
            - Setting the transcript channel (`/configure transcript`)
            - Adding ticket administrators (`/ticket admins add`)
            
            **Follow it up with**:
            - Adding ticket categories (`/configure category add`)
            
            **When in tickets:**
            /rename - Change the tickets name to the specified one - Works only in configured categories
            /move - Move the ticket to the specified category - Works only in configured categories
            
            **NOTE: ANY USER WITH ADMINISTRATOR PRIVILEGES CAN MANAGE TICKETS!**
            
            **SECOND NOTE: ANY ROLES ASSIGNED TO CATEGORIES ON CREATION WILL GET PINGED ON CREATION OF TICKETS!**
            """)
        .setEphemeral(true)
        .queue();
  }
}
