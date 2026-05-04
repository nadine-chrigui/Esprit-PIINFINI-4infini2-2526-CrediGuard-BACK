-- Script pour préparer les features ML
-- Exécuter dans phpMyAdmin sur la base my_crediguard

USE my_crediguard;

-- 1. Ajouter des colonnes de features calculées à la table event
ALTER TABLE event 
ADD COLUMN budget_per_person DECIMAL(10,2),
ADD COLUMN ticket_price_ratio DECIMAL(5,2),
ADD COLUMN season VARCHAR(20),
ADD COLUMN days_duration INT,
ADD COLUMN profit_margin_estimate DECIMAL(5,2);

-- 2. Mettre à jour les features calculées
UPDATE event SET 
    budget_per_person = CASE WHEN capacity > 0 THEN budget_estimated / capacity ELSE 0 END,
    ticket_price_ratio = CASE WHEN budget_estimated > 0 THEN (ticket_price * capacity) / budget_estimated ELSE 0 END,
    days_duration = DATEDIFF(date_end, date_start),
    season = CASE 
        WHEN MONTH(date_start) IN (3,4,5) THEN 'printemps'
        WHEN MONTH(date_start) IN (6,7,8) THEN 'été'
        WHEN MONTH(date_start) IN (9,10,11) THEN 'automne'
        ELSE 'hiver'
    END,
    profit_margin_estimate = CASE WHEN budget_estimated > 0 THEN ((ticket_price * capacity) - budget_estimated) / budget_estimated * 100 ELSE 0 END;

-- 3. Créer une vue pour les données ML
CREATE OR REPLACE VIEW ml_event_data AS
SELECT 
    e.id,
    e.title,
    e.budget_estimated,
    e.capacity,
    e.ticket_price,
    e.equipment_cost,
    e.marketing_cost,
    e.staff_cost,
    e.venue_cost,
    e.budget_per_person,
    e.ticket_price_ratio,
    e.season,
    e.days_duration,
    e.profit_margin_estimate,
    e.event_type,
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

-- Afficher les données préparées
SELECT * FROM ml_event_data LIMIT 10;
