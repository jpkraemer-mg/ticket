package quest.darkoro.ticket.listeners.primary;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;
import quest.darkoro.ticket.annotations.PrimaryListener;
import quest.darkoro.ticket.services.ButtonService;
import quest.darkoro.ticket.services.ModalService;
import quest.darkoro.ticket.services.SelectMenuService;

@PrimaryListener
@Slf4j
@RequiredArgsConstructor
@Service
public class ComponentListener extends ListenerAdapter {

  private final ButtonService buttonService;
  private final ModalService modalService;
  private final SelectMenuService selectMenuService;

  @Override
  public void onButtonInteraction(@NonNull ButtonInteractionEvent e) {
    buttonService.distributeEvent(e);
  }

  @Override
  public void onModalInteraction(@NonNull ModalInteractionEvent e) {
    modalService.distributeEvent(e);
  }

  @Override
  public void onStringSelectInteraction(@NonNull StringSelectInteractionEvent e) {
    selectMenuService.distributeEvent(e);
  }

  @Override
  public void onEntitySelectInteraction(@NonNull EntitySelectInteractionEvent e) {
    selectMenuService.distributeEvent(e);
  }
}
