USE my_CrediGuard;

-- 1. Ensure CompteFinancier for Admin (ID: 1)
INSERT INTO compte_financier (id_utilisateur, solde, type_compte)
SELECT 1, 0.0, 'PLATEFORME'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM compte_financier WHERE id_utilisateur = 1)
ON DUPLICATE KEY UPDATE type_compte = 'PLATEFORME';

-- 2. Ensure CompteFinancier for Beneficiary (ID: 2)
INSERT INTO compte_financier (id_utilisateur, solde, type_compte)
SELECT 2, 1000.0, 'BENEFICIAIRE'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM compte_financier WHERE id_utilisateur = 2)
ON DUPLICATE KEY UPDATE type_compte = 'BENEFICIAIRE';

-- 3. Create DemandeCredit (Needed for Credit)
INSERT INTO demande_credit (client_id, montant_demande, objet_credit, reference, statut, type_credit, date_creation, duree_mois)
VALUES (2, 5000.0, 'Achat Stock', 'DEM-TEST-001', 'APPROUVEE', 'NUMERAIRE', NOW(), 12);

-- 4. Create Credit
SET @last_demande_id = LAST_INSERT_ID();
INSERT INTO credit (client_id, demande_credit_id, montant_accorde, montant_total, montant_restant, taux_remboursement, statut, mode_remboursement, date_debut, date_fin)
VALUES (2, @last_demande_id, 5000.0, 5000.0, 5000.0, 5.0, 'ACTIF', 'LIE_AUX_VENTES', NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR));

-- 5. Create RegleRemboursement (10% on each sale)
SET @last_credit_id = LAST_INSERT_ID();
INSERT INTO regle_remboursement (id_credit, type_regle, valeur)
VALUES (@last_credit_id, 'POURCENTAGE_SUR_VENTE', 10.0);

-- 6. Create a Pending Order for the Beneficiary
INSERT INTO orders (user_id, status, total_amount, reference, created_at)
VALUES (2, 'PENDING', 200.0, 'ORD-TEST-001', NOW());

SET @last_order_id = LAST_INSERT_ID();

SELECT 'Environment ready for testing!' as Result;
SELECT id as OrderID, reference, total_amount FROM orders WHERE id = @last_order_id;
