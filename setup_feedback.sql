-- Exécuter ce script dans votre base de données MySQL
-- pour créer la table event_feedback

USE my_crediguard;

-- Création de la table event_feedback
CREATE TABLE IF NOT EXISTS event_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    overall_rating INT NOT NULL CHECK (overall_rating BETWEEN 1 AND 5),
    organization_rating INT CHECK (organization_rating BETWEEN 1 AND 5),
    content_rating INT CHECK (content_rating BETWEEN 1 AND 5),
    venue_rating INT CHECK (venue_rating BETWEEN 1 AND 5),
    value_rating INT CHECK (value_rating BETWEEN 1 AND 5),
    participation_rating INT CHECK (participation_rating BETWEEN 1 AND 5),
    comment TEXT,
    suggestions TEXT,
    would_recommend BOOLEAN,
    feedback_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_attendance BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    
    UNIQUE KEY unique_user_event_feedback (event_id, user_id),
    
    INDEX idx_event_feedback (event_id),
    INDEX idx_user_feedback (user_id),
    INDEX idx_feedback_date (feedback_date)
);

-- Insérer quelques données de test
INSERT IGNORE INTO event_feedback (event_id, user_id, overall_rating, organization_rating, content_rating, venue_rating, value_rating, participation_rating, comment, suggestions, would_recommend) VALUES
(1, 1, 5, 4, 5, 4, 5, 5, 'Excellent événement!', 'Continuer comme ça', true),
(1, 2, 4, 5, 4, 5, 4, 4, 'Très bien organisé', 'Plus de activités', true),
(2, 1, 3, 3, 3, 3, 3, 3, 'Correct', 'Améliorer la logistique', false);

-- Afficher un message de confirmation
SELECT 'Table event_feedback créée avec succès!' as message;
