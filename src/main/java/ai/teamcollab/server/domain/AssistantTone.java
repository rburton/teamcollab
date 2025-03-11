package ai.teamcollab.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Represents different communication tones that an assistant can use in a conversation.
 */
@Entity
@Table(name = "assistant_tone")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class AssistantTone {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "prompt")
    private String prompt;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "deleted_time")
    private LocalDateTime deletedTime;

    /**
     * Returns the AssistantTone entity for the given name (case-insensitive).
     * This is a compatibility method to ease transition from enum to entity.
     *
     * @param name the name of the tone
     * @param tones list of all available tones
     * @return the corresponding AssistantTone entity
     * @throws IllegalArgumentException if no matching tone is found
     */
    public static AssistantTone fromName(String name, List<AssistantTone> tones) {
        for (AssistantTone tone : tones) {
            if (tone.getName().equalsIgnoreCase(name)) {
                return tone;
            }
        }
        throw new IllegalArgumentException("No tone found with name: " + name);
    }
}
