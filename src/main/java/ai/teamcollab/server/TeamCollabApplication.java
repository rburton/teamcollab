package ai.teamcollab.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EntityScan(basePackages = "ai.teamcollab.server.domain")
@EnableJpaRepositories(basePackages = "ai.teamcollab.server.repository")
@EnableAsync
public class TeamCollabApplication {

    public static void main(String[] args) {
        SpringApplication.run(TeamCollabApplication.class, args);
    }

}
