package quest.darkoro.ticket.configuration;

import static net.dv8tion.jda.api.entities.Activity.watching;
import static net.dv8tion.jda.api.requests.GatewayIntent.ALL_INTENTS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_PRESENCES;
import static net.dv8tion.jda.api.requests.GatewayIntent.MESSAGE_CONTENT;

import java.util.EnumSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import quest.darkoro.ticket.listener.BotReadyListener;
import quest.darkoro.ticket.listener.primary.GuildListener;

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
    return JDABuilder.createLight(token, EnumSet.allOf(GatewayIntent.class))
        .setChunkingFilter(ChunkingFilter.ALL)
        .setMemberCachePolicy(MemberCachePolicy.DEFAULT)
        .enableCache(CacheFlag.MEMBER_OVERRIDES)
        .setAutoReconnect(true)
        .setActivity(watching(" all tickets..."))
        .addEventListeners(botReadyListener, guildListener)
        .build();
  }
}
