-- Script final pour corriger toutes les erreurs SQL
-- Exécuter dans phpMyAdmin sur la base my_crediguard

USE my_crediguard;

-- D'abord, vérifions les types actuels
DESCRIBE event;

-- Supprimer toutes les colonnes ML problématiques
ALTER TABLE event DROP COLUMN IF EXISTS budget_per_person;
ALTER TABLE event DROP COLUMN IF EXISTS ticket_price_ratio;
ALTER TABLE event DROP COLUMN IF EXISTS profit_margin_estimate;
ALTER TABLE event DROP COLUMN IF EXISTS season;
ALTER TABLE event DROP COLUMN IF EXISTS days_duration;

-- Ajouter les colonnes avec des valeurs par défaut sûres
ALTER TABLE event 
ADD COLUMN budget_per_person DECIMAL(10,2) DEFAULT 0,
ADD COLUMN ticket_price_ratio DECIMAL(5,2) DEFAULT 0,
ADD COLUMN profit_margin_estimate DECIMAL(5,2) DEFAULT 0,
ADD COLUMN season VARCHAR(20) DEFAULT 'printemps',
ADD COLUMN days_duration INT DEFAULT 1;

-- Mettre à jour avec gestion complète des NULL et types
UPDATE event SET 
    budget_per_person = CASE 
        WHEN capacity IS NOT NULL AND capacity > 0 AND budget_estimated IS NOT NULL AND budget_estimated > 0 
        THEN ROUND(CAST(budget_estimated AS DECIMAL(10,2)) / CAST(capacity AS DECIMAL(10,2)), 2) 
        ELSE 0 
    END,
    ticket_price_ratio = CASE 
        WHEN budget_estimated IS NOT NULL AND budget_estimated > 0 AND ticket_price IS NOT NULL AND capacity IS NOT NULL AND capacity > 0
        THEN ROUND(CAST(ticket_price AS DECIMAL(10,2)) * CAST(capacity AS DECIMAL(10,2)) / CAST(budget_estimated AS DECIMAL(10,2)), 2)
        ELSE 0 
    END,
    profit_margin_estimate = CASE 
        WHEN budget_estimated IS NOT NULL AND budget_estimated > 0 AND ticket_price IS NOT NULL AND capacity IS NOT NULL AND capacity > 0
        THEN ROUND((CAST(ticket_price AS DECIMAL(10,2)) * CAST(capacity AS DECIMAL(10,2)) - CAST(budget_estimated AS DECIMAL(10,2))) / CAST(budget_estimated AS DECIMAL(10,2)) * 100, 2)
        ELSE 0 
    END,
    season = CASE 
        WHEN date_start IS NOT NULL 
        THEN CASE MONTH(date_start)
            WHEN 3 THEN 'printemps'
            WHEN 4 THEN 'printemps'
            WHEN 5 THEN 'printemps'
            WHEN 6 THEN 'été'
            WHEN 7 THEN 'été'
            WHEN 8 THEN 'été'
            WHEN 9 THEN 'automne'
            WHEN 10 THEN 'automne'
            WHEN 11 THEN 'automne'
            ELSE 'hiver'
        END
        ELSE 'printemps'
    END,
    days_duration = CASE 
        WHEN date_start IS NOT NULL AND date_end IS NOT NULL 
        THEN DATEDIFF(date_end, date_start)
        ELSE 1
    END;

-- Supprimer et recréer la vue ML
DROP VIEW IF EXISTS ml_event_data;

CREATE OR REPLACE VIEW ml_event_data AS
SELECT 
    e.id,
    e.title,
    COALESCE(e.budget_estimated, 0) as budget_estimated,
    COALESCE(e.capacity, 0) as capacity,
    COALESCE(e.ticket_price, 0) as ticket_price,
    COALESCE(e.equipment_cost, 0) as equipment_cost,
    COALESCE(e.marketing_cost, 0) as marketing_cost,
    COALESCE(e.staff_cost, 0) as staff_cost,
    COALESCE(e.venue_cost, 0) as venue_cost,
    COALESCE(e.budget_per_person, 0) as budget_per_person,
    COALESCE(e.ticket_price_ratio, 0) as ticket_price_ratio,
    COALESCE(e.season, 'printemps') as season,
    COALESCE(e.days_duration, 1) as days_duration,
    COALESCE(e.profit_margin_estimate, 0) as profit_margin_estimate,
    COALESCE(e.event_type, 'conference') as event_type,
    -- Target variables (si feedback existe)
    COALESCE(efs.averageOverallRating, 0) as actual_rating,
    COALESCE(efs.recommendationRate, 0) as actual_recommendation_rate,
    COALESCE(efs.totalFeedbacks, 0) as total_feedbacks,
    -- Target: succès (rating >= 4 ET recommendation >= 80%)
    CASE 
        WHEN COALESCE(efs.averageOverallRating, 0) >= 4.0 
        AND COALESCE(efs.recommendationRate, 0) >= 80.0 
        THEN 1 
        ELSE 0 
    END as is_success
FROM event e
LEFT JOIN (
    SELECT 
        event_id,
        AVG(overall_rating) as averageOverallRating,
        AVG(CASE WHEN would_recommend = 1 THEN 100 ELSE 0 END) as recommendationRate,
        COUNT(*) as totalFeedbacks
    FROM event_feedback 
    GROUP BY event_id
) efs ON e.id = efs.event_id;

-- Afficher les données pour vérification
SELECT 
    id, 
    title,
    budget_per_person,
    ticket_price_ratio,
    profit_margin_estimate,
    season,
    days_duration,
    actual_rating,
    is_success
FROM ml_event_data 
LIMIT 5;
