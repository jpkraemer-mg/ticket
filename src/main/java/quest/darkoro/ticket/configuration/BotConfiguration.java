package quest.darkoro.ticket.configuration;

import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES;
import static net.dv8tion.jda.api.requests.GatewayIntent.MESSAGE_CONTENT;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.MEMBER_OVERRIDES;
import static net.dv8tion.jda.api.utils.cache.CacheFlag.ROLE_TAGS;

import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import quest.darkoro.ticket.listeners.BotReadyListener;
import quest.darkoro.ticket.listeners.primary.GuildListener;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class BotConfiguration {

  @Value("${quest.darkoro.token}")
  public String token;
  private final BotReadyListener botReadyListener;
  private final GuildListener guildListener;

  @Bean
  public JDA bot() {
    return JDABuilder.createDefault(token)
        .setChunkingFilter(ChunkingFilter.ALL)
        .enableIntents(EnumSet.of(GUILD_MEMBERS, MESSAGE_CONTENT, GUILD_PRESENCES))
        .enableCache(MEMBER_OVERRIDES, ROLE_TAGS)
        .setAutoReconnect(true)
        .setActivity(Activity.customStatus("Supporting BRs since 1999"))
        .addEventListeners(botReadyListener, guildListener)
        .build();
  }
}
