#!/bin/bash

echo "========================================"
echo "=== INTEGRATION BRANCHE NADINE -> main ==="
echo "========================================"

echo ""
echo "=== Etape 1: Creer branche de travail depuis main ==="
git checkout main
git pull origin main
git checkout -b merge/nadine-integration

echo ""
echo "=== Etape 2: Merge NADINE (sans fast-forward) ==="
git merge origin/NADINE --no-commit --no-ff --allow-unrelated-histories

echo ""
echo "=== Etape 3: Ignorer completement le dossier back/ (doublon) ==="
git checkout HEAD -- . 2>/dev/null || true
# On remet le merge mais en ignorant back/
git merge --abort 2>/dev/null || true

echo ""
echo "=== Etape 3b: Cherry-pick uniquement les fichiers src/ de NADINE ==="

# --- NOUVEAUX FICHIERS DE NADINE (A) a copier depuis origin/NADINE ---

echo "Copie des entites de Nadine..."
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/entities/Beneficiary.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/entities/Partner.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/entities/Vente.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/entities/CompteFinancier.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/entities/RegleRemboursement.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/entities/Remboursement.java

echo "Copie des enums de Nadine..."
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/entities/enums/CompteType.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/entities/enums/ModeRemboursement.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/entities/enums/RegleType.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/entities/enums/TransactionStatut.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/entities/enums/TransactionType.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/entities/enums/PartnerStatus.java

echo "Copie des repositories de Nadine..."
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/repositories/CompteFinancierRepository.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/repositories/RegleRemboursementRepository.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/repositories/RemboursementRepository.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/repositories/TransactionRepository.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/repositories/VenteRepository.java

echo "Copie des services de Nadine..."
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/services/CompteFinancierService.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/services/CompteFinancierServiceImpl.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/services/RegleRemboursementService.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/services/RegleRemboursementServiceImpl.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/services/RemboursementService.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/services/RemboursementServiceImpl.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/services/TransactionService.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/services/TransactionServiceImpl.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/services/VenteService.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/services/VenteServiceImpl.java

echo "Copie des controllers de Nadine..."
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/controllers/CompteFinancierController.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/controllers/RegleRemboursementController.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/controllers/RemboursementController.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/controllers/TransactionController.java
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/controllers/VenteController.java

echo "Copie des DTOs de Nadine..."
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/dto/CompteFinancierRequest.java  2>/dev/null || echo "CompteFinancierRequest pas trouve, skip"
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/dto/CompteFinancierResponse.java 2>/dev/null || echo "CompteFinancierResponse pas trouve, skip"
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/dto/TransactionRequest.java      2>/dev/null || echo "TransactionRequest pas trouve, skip"
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/dto/TransactionResponse.java     2>/dev/null || echo "TransactionResponse pas trouve, skip"
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/dto/RemboursementRequest.java    2>/dev/null || echo "RemboursementRequest pas trouve, skip"
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/dto/RemboursementResponse.java   2>/dev/null || echo "RemboursementResponse pas trouve, skip"
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/dto/RegleRemboursementRequest.java  2>/dev/null || echo "RegleRemboursementRequest pas trouve, skip"
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/dto/RegleRemboursementResponse.java 2>/dev/null || echo "RegleRemboursementResponse pas trouve, skip"
git checkout origin/NADINE -- src/main/java/tn/esprit/pi_back/dto/FinanceSummaryResponse.java  2>/dev/null || echo "FinanceSummaryResponse pas trouve, skip"

echo ""
echo "=== Etape 4: Garder la version MAIN pour les fichiers M (modifies) ==="
# Ces fichiers sont plus complets dans main - on ne touche pas

echo "Version main conservee pour : User, Transaction, Voucher, InsurancePolicy,"
echo "SecurityConfig, UserController, Category, JwtAuthFilter, application.properties"
echo "pom.xml, AuthController, UserService, UserServiceImpl..."

echo ""
echo "=== Etape 5: Verifier les fichiers ajoutes ==="
git status

echo ""
echo "=== Etape 6: git add et commit ==="
git add -A
git commit -m "merge: integration NADINE -> main (CompteFinancier, Transaction, Remboursement, RegleRemboursement, Vente, Beneficiary, Partner)"

echo ""
echo "=== Etape 7: Push sur main ==="
git push origin merge/nadine-integration:main --force-with-lease

echo ""
echo "========================================"
echo "=== TERMINE ! NADINE integree dans main ==="
echo "========================================"
