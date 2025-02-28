package ai.teamcollab.server.api.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PersonaAddedResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("expertises")
    private String expertises;

    public PersonaAddedResponse(Long id, String name, String expertises) {
        this.id = id;
        this.name = name;
        this.expertises = expertises;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpertises() {
        return expertises;
    }

    public void setExpertises(String expertises) {
        this.expertises = expertises;
    }
}
