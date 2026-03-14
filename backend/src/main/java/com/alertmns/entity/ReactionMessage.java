package com.alertmns.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "t_reaction_message",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_reaction_message_user_emoji", columnNames = {"id_message", "id_user", "emoji"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ReactionMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reaction")
    private Long idReaction;

    @Column(name = "emoji", nullable = false, length = 16)
    private String emoji;

    @Column(name = "date_reaction", nullable = false)
    private LocalDateTime dateReaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_message", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private Utilisateur utilisateur;

    @PrePersist
    public void prePersist() {
        if (dateReaction == null) {
            dateReaction = LocalDateTime.now();
        }
    }
}
