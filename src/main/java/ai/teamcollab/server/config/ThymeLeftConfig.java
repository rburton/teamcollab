package ai.teamcollab.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
public class ThymeLeftConfig {

    @Bean
    public ThymeleafViewResolver viewResolver(SpringTemplateEngine templateEngine){
        final var viewResolver = new ThymeleafViewResolver();
        viewResolver.setContentType("text/vnd.turbo-stream.html");
        viewResolver.setCharacterEncoding(UTF_8.name());
        viewResolver.setOrder(1);
        viewResolver.setViewNames(new String[] {"*.xhtml"});
        viewResolver.setTemplateEngine(templateEngine);
        return viewResolver;
    }

}
