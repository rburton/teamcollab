package ai.teamcollab.server.config;

import ai.teamcollab.server.service.ConversationService;
import ai.teamcollab.server.service.MessageService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class TestConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public ConversationService conversationService() {
        return Mockito.mock(ConversationService.class);
    }

    @Bean
    public MessageService messageService() {
        return Mockito.mock(MessageService.class);
    }
}