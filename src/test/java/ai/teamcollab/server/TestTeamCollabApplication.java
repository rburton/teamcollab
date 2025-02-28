package ai.teamcollab.server;

import org.springframework.boot.SpringApplication;

public class TestTeamCollabApplication {

    public static void main(String[] args) {
        SpringApplication.from(TeamCollabApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
