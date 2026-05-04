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
    comment TEXT,
    suggestions TEXT,
    would_recommend BOOLEAN,
    feedback_date DATETIME NOT NULL,
    verified_attendance BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (event_id) REFERENCES event(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    
    UNIQUE KEY unique_user_event_feedback (event_id, user_id),
    
    INDEX idx_event_feedback (event_id),
    INDEX idx_user_feedback (user_id),
    INDEX idx_feedback_date (feedback_date)
);
