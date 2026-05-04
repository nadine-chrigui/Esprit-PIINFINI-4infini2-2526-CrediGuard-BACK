-- Script pour nettoyer la table event_feedback
-- Exécuter dans phpMyAdmin sur la base my_crediguard

USE my_crediguard;

-- Supprimer toutes les données de la table
DELETE FROM event_feedback;

-- Réinitialiser l'auto-increment (optionnel)
ALTER TABLE event_feedback AUTO_INCREMENT = 1;

-- Afficher confirmation
SELECT 'Table event_feedback nettoyée avec succès!' as message;
