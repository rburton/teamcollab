package ai.teamcollab.server.domain;

import lombok.Getter;

/**
 * Represents different communication tones that an assistant can use in a conversation.
 */
@Getter
public enum AssistantTone {
    FORMAL("Formal"),
    CASUAL("Casual"),
    TECHNICAL("Technical"),
    SIMPLIFIED("Simplified");

    private final String displayName;

    AssistantTone(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the AssistantTone enum value for the given name (case-insensitive).
     *
     * @param name the name of the tone
     * @return the corresponding AssistantTone enum value
     * @throws IllegalArgumentException if no matching tone is found
     */
    public static AssistantTone fromName(String name) {
        for (AssistantTone tone : AssistantTone.values()) {
            if (tone.name().equalsIgnoreCase(name)) {
                return tone;
            }
        }
        throw new IllegalArgumentException("No tone found with name: " + name);
    }
}