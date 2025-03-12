package ai.teamcollab.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

/**
 * Configuration for Spring Session with JDBC storage.
 * This enables HTTP sessions to be stored in the database instead of in-memory.
 */
@Configuration
@EnableJdbcHttpSession
public class SessionConfig extends AbstractHttpSessionApplicationInitializer {
    
    // The @EnableJdbcHttpSession annotation creates a Spring Bean with the name of 
    // "springSessionRepositoryFilter" that implements Filter. This filter is what 
    // replaces the HttpSession implementation to be backed by Spring Session.
    
    // Spring Boot automatically configures a DataSource and JdbcTemplate
    // Spring Session JDBC will use that to store sessions in the database
    
    // By default, the following tables will be created in the database:
    // - SPRING_SESSION
    // - SPRING_SESSION_ATTRIBUTES
}