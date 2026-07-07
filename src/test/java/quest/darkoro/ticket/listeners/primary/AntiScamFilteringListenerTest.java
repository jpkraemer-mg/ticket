package quest.darkoro.ticket.listeners.primary;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import quest.darkoro.ticket.persistence.ContentFilterRepository;
import quest.darkoro.ticket.persistence.model.ContentFilter;

class AntiScamFilteringListenerTest {

  private static final Long GID = 1L;

  private ContentFilterRepository contentFilterRepository;
  private AntiScamFilteringListener listener;

  @BeforeEach
  void setUp() {
    contentFilterRepository = mock(ContentFilterRepository.class);
    listener = new AntiScamFilteringListener(contentFilterRepository);
  }

  private MessageReceivedEvent eventWithContent(String content) {
    var e = mock(MessageReceivedEvent.class, RETURNS_DEEP_STUBS);
    when(e.isFromGuild()).thenReturn(true);
    when(e.getAuthor().isBot()).thenReturn(false);
    when(e.getGuild().getIdLong()).thenReturn(GID);
    when(e.getMessage().getContentRaw()).thenReturn(content);
    return e;
  }

  private void configureFilters(String... filters) {
    var rows = List.of(filters).stream()
        .map(f -> new ContentFilter().setGuildId(GID).setContent(f))
        .toList();
    when(contentFilterRepository.findByGuildId(GID)).thenReturn(rows);
  }

  @Test
  void directMessageIsIgnoredWithoutTouchingGuildOrRepository() {
    var e = mock(MessageReceivedEvent.class, RETURNS_DEEP_STUBS);
    when(e.isFromGuild()).thenReturn(false);

    listener.onMessageReceived(e);

    verifyNoInteractions(contentFilterRepository);
    verify(e, never()).getGuild();
  }

  @Test
  void botMessageIsIgnored() {
    var e = mock(MessageReceivedEvent.class, RETURNS_DEEP_STUBS);
    when(e.isFromGuild()).thenReturn(true);
    when(e.getAuthor().isBot()).thenReturn(true);

    listener.onMessageReceived(e);

    verifyNoInteractions(contentFilterRepository);
  }

  @Test
  void matchingFilterDeletesMessageAndWarns() {
    configureFilters("scam");
    var e = eventWithContent("buy scam now");

    listener.onMessageReceived(e);

    verify(e.getMessage(), times(1)).delete();
    verify(e.getChannel(), times(1)).sendMessage("Fuck off, no scamming");
  }

  @Test
  void twoMatchingFiltersOnlyPunishOnce() {
    configureFilters("scam", "buy");
    var e = eventWithContent("buy scam now");

    listener.onMessageReceived(e);

    verify(e.getMessage(), times(1)).delete();
    verify(e.getChannel(), times(1)).sendMessage("Fuck off, no scamming");
  }

  @Test
  void specialCharEvasionIsCaught() {
    configureFilters("scam");
    var e = eventWithContent("free s-c-a-m here");

    listener.onMessageReceived(e);

    verify(e.getMessage(), times(1)).delete();
  }

  @Test
  void nonMatchingMessageIsLeftAlone() {
    configureFilters("scam");
    var e = eventWithContent("hello there");

    listener.onMessageReceived(e);

    verify(e.getMessage(), never()).delete();
    verify(e.getChannel(), never()).sendMessage(anyString());
  }

  @Test
  void allSpecialCharFilterDoesNotMatchEverything() {
    configureFilters("$$$");
    var e = eventWithContent("perfectly normal message");

    listener.onMessageReceived(e);

    verify(e.getMessage(), never()).delete();
  }

  @Test
  void filtersAreCachedPerGuildAndInvalidatedOnDemand() {
    configureFilters("scam");

    listener.onMessageReceived(eventWithContent("first message"));
    listener.onMessageReceived(eventWithContent("second message"));
    verify(contentFilterRepository, times(1)).findByGuildId(GID);

    listener.invalidateFilters(GID);
    listener.onMessageReceived(eventWithContent("third message"));
    verify(contentFilterRepository, times(2)).findByGuildId(GID);
  }
}
