package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import lombok.*;
import tn.esprit.pi_back.entities.enums.NotificationCategory;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;
    private String reference; // e.g. CLM-2026...
    
    @Enumerated(EnumType.STRING)
    private NotificationCategory category;
    
    private String statusTag; // e.g. Validée, Refusée
    private String details; // e.g. Montant couvert: ...
    
    private boolean isRead;
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        isRead = false;
    }
}
