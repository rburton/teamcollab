package ai.teamcollab.server.domain;

import lombok.Getter;

import java.util.List;

@Getter
public enum Provider {
    OPENAI("openai"),
    GEMINI("gemini");

    private final String label;

    Provider(String name) {
        this.label = name;
    }

    public GptModel findModelById(String model) {
        return GptModel.findByProvider(this)
                .stream()
                .filter(gptModel -> gptModel.getId().equalsIgnoreCase(model))
                .findFirst()
                .orElseThrow();
    }

    public List<GptModel> getModals() {
        return GptModel.findByProvider(this);
    }

}
