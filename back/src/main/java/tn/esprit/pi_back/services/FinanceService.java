package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.pi_back.dto.*;
import tn.esprit.pi_back.entities.CompteFinancier;
import tn.esprit.pi_back.entities.Credit;
import tn.esprit.pi_back.entities.RegleRemboursement;
import tn.esprit.pi_back.entities.Remboursement;
import tn.esprit.pi_back.entities.Transaction;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.TransactionStatut;
import tn.esprit.pi_back.entities.enums.TransactionType;
import tn.esprit.pi_back.entities.enums.UserType;
import tn.esprit.pi_back.entities.enums.StatutCredit;
import tn.esprit.pi_back.repositories.CompteFinancierRepository;
import tn.esprit.pi_back.repositories.CreditRepository;
import tn.esprit.pi_back.repositories.RegleRemboursementRepository;
import tn.esprit.pi_back.repositories.RemboursementRepository;
import tn.esprit.pi_back.repositories.TransactionRepository;
import tn.esprit.pi_back.repositories.UserRepository;

import java.util.*;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceService {

    private final CompteFinancierRepository compteFinancierRepository;
    private final TransactionRepository transactionRepository;
    private final RemboursementRepository remboursementRepository;
    private final RegleRemboursementRepository regleRemboursementRepository;
    private final CreditRepository creditRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public List<CompteFinancierResponse> getComptes() {
        return compteFinancierRepository.findAll().stream()
                .map(this::toCompteResponse)
                .toList();
    }

    public CompteFinancierResponse getCompte(Long id) {
        return toCompteResponse(getCompteEntity(id));
    }

    public CompteFinancierResponse getCompteByUtilisateurId(Long utilisateurId) {
        return toCompteResponse(
                compteFinancierRepository.findByUtilisateurId(utilisateurId)
                        .orElseThrow(() -> new RuntimeException("Compte financier not found for user id: " + utilisateurId))
        );
    }

    @Transactional
    public CompteFinancierResponse createCompte(CompteFinancierRequest request) {
        User utilisateur = userRepository.findById(request.getUtilisateurId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUtilisateurId()));

        if (compteFinancierRepository.findByUtilisateurId(utilisateur.getId()).isPresent()) {
            throw new RuntimeException("User already has a financial account");
        }

        CompteFinancier compte = new CompteFinancier();
        compte.setSolde(request.getSolde());
        compte.setTypeCompte(request.getTypeCompte());
        compte.setUtilisateur(utilisateur);

        return toCompteResponse(compteFinancierRepository.save(compte));
    }

    @Transactional
    public CompteFinancierResponse updateCompte(Long id, CompteFinancierRequest request) {
        CompteFinancier compte = getCompteEntity(id);
        User utilisateur = userRepository.findById(request.getUtilisateurId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUtilisateurId()));

        compte.setSolde(request.getSolde());
        compte.setTypeCompte(request.getTypeCompte());
        compte.setUtilisateur(utilisateur);

        return toCompteResponse(compteFinancierRepository.save(compte));
    }

    @Transactional
    public void deleteCompte(Long id) {
        CompteFinancier compte = getCompteEntity(id);

        jdbcTemplate.update(
            "delete from remboursement where id_transaction in (" +
                "select id_transaction from `transaction` where id_compte_source = ? or id_compte_destination = ?)",
            id,
            id
        );

        jdbcTemplate.update(
            "delete from `transaction` where id_compte_source = ? or id_compte_destination = ?",
            id,
            id
        );

        compteFinancierRepository.delete(compte);
        compteFinancierRepository.flush();
    }

    public List<TransactionResponse> getTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    public TransactionResponse getTransaction(Long id) {
        return toTransactionResponse(getTransactionEntity(id));
    }

    public List<TransactionResponse> getTransactionsByCompte(Long compteId) {
        return transactionRepository.findByCompteSourceIdCompteOrCompteDestinationIdCompte(compteId, compteId)
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        CompteFinancier compteSource = getCompteEntity(request.getCompteSourceId());
        CompteFinancier compteDestination = getCompteEntity(request.getCompteDestinationId());

        if (!compteSource.getIdCompte().equals(compteDestination.getIdCompte())) {
            if (compteSource.getSolde() < request.getMontant()) {
                throw new RuntimeException("Insufficient balance on source account");
            }

            compteSource.setSolde(compteSource.getSolde() - request.getMontant());
            compteDestination.setSolde(compteDestination.getSolde() + request.getMontant());

            compteFinancierRepository.save(compteSource);
            compteFinancierRepository.save(compteDestination);
        }

        Transaction transaction = new Transaction();
        transaction.setTypeTransaction(request.getTypeTransaction());
        transaction.setMontant(request.getMontant());
        transaction.setCompteSource(compteSource);
        transaction.setCompteDestination(compteDestination);
        transaction.setOrderId(request.getOrderId() != null ? request.getOrderId() : 0L);
        transaction.setStatut(TransactionStatut.COMPLETED);

        return toTransactionResponse(transactionRepository.save(transaction));
    }

    public List<RemboursementResponse> getRemboursements() {
        return remboursementRepository.findAll().stream()
                .map(this::toRemboursementResponse)
                .toList();
    }

    public RemboursementResponse getRemboursement(Long id) {
        return toRemboursementResponse(getRemboursementEntity(id));
    }

    public List<RemboursementResponse> getRemboursementsByCredit(Long creditId) {
        return remboursementRepository.findByCreditId(creditId).stream()
                .map(this::toRemboursementResponse)
                .toList();
    }

    @Transactional
    public RemboursementResponse createRemboursement(RemboursementRequest request) {
        Credit credit = getCreditEntity(request.getCreditId());

        if (credit.getMontantRestant() < request.getMontant()) {
            throw new RuntimeException("Remboursement amount exceeds remaining credit amount");
        }

        Transaction transaction = null;
        if (request.getTransactionId() != null) {
            transaction = getTransactionEntity(request.getTransactionId());
        }

        double montantRestant = Math.max(0D, credit.getMontantRestant() - request.getMontant());
        credit.setMontantRestant(montantRestant);

        if (montantRestant == 0D) {
            credit.setStatut(StatutCredit.CLOTURE);
        } else if (credit.getStatut() == null) {
            credit.setStatut(StatutCredit.ACTIF);
        }

        creditRepository.save(credit);

        Remboursement remboursement = new Remboursement();
        remboursement.setMontant(request.getMontant());
        remboursement.setMode(request.getMode() != null && !request.getMode().isBlank() ? request.getMode() : "automatique");
        remboursement.setCredit(credit);
        remboursement.setTransaction(transaction);

        return toRemboursementResponse(remboursementRepository.save(remboursement));
    }

    public List<RegleRemboursementResponse> getRegles() {
        return regleRemboursementRepository.findAll().stream()
                .map(this::toRegleResponse)
                .toList();
    }

    public RegleRemboursementResponse getRegle(Long id) {
        return toRegleResponse(getRegleEntity(id));
    }

    public List<RegleRemboursementResponse> getReglesByCredit(Long creditId) {
        return regleRemboursementRepository.findByCreditId(creditId).stream()
                .map(this::toRegleResponse)
                .toList();
    }

    @Transactional
    public RegleRemboursementResponse createRegle(RegleRemboursementRequest request) {
        Credit credit = getCreditEntity(request.getCreditId());

        RegleRemboursement regle = new RegleRemboursement();
        regle.setTypeRegle(request.getTypeRegle());
        regle.setValeur(request.getValeur());
        regle.setCredit(credit);

        return toRegleResponse(regleRemboursementRepository.save(regle));
    }

    @Transactional
    public RegleRemboursementResponse updateRegle(Long id, RegleRemboursementRequest request) {
        RegleRemboursement existing = getRegleEntity(id);
        Credit credit = getCreditEntity(request.getCreditId());

        existing.setTypeRegle(request.getTypeRegle());
        existing.setValeur(request.getValeur());
        existing.setCredit(credit);

        return toRegleResponse(regleRemboursementRepository.save(existing));
    }

    @Transactional
    public void deleteRegle(Long id) {
        regleRemboursementRepository.delete(getRegleEntity(id));
    }

    public FinanceSummaryResponse getFinanceSummary() {
        List<Transaction> transactions = transactionRepository.findAll();

        double totalRevenue = transactions.stream()
                .filter(tx -> tx.getStatut() == TransactionStatut.COMPLETED)
                .filter(tx -> tx.getTypeTransaction() == TransactionType.VENTE)
                .mapToDouble(Transaction::getMontant)
                .sum();

        double totalExpenses = transactions.stream()
                .filter(tx -> tx.getStatut() == TransactionStatut.COMPLETED)
                .filter(tx -> tx.getTypeTransaction() == TransactionType.ACHAT || tx.getTypeTransaction() == TransactionType.REMBOURSEMENT)
                .mapToDouble(Transaction::getMontant)
                .sum();

        Double totalAmountGranted = creditRepository.sumTotalAmountGranted();
        Double totalAmountRemaining = creditRepository.sumTotalAmountRemaining();

        // --- TRENDS & CHARTS ---
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime beginningOfCurrentMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime beginningOfLastMonth = beginningOfCurrentMonth.minusMonths(1);

        double currentMonthRevenue = transactions.stream()
                .filter(tx -> tx.getStatut() == TransactionStatut.COMPLETED)
                .filter(tx -> tx.getTypeTransaction() == TransactionType.VENTE)
                .filter(tx -> tx.getDateTransaction().isAfter(beginningOfCurrentMonth))
                .mapToDouble(Transaction::getMontant).sum();

        double lastMonthRevenue = transactions.stream()
                .filter(tx -> tx.getStatut() == TransactionStatut.COMPLETED)
                .filter(tx -> tx.getTypeTransaction() == TransactionType.VENTE)
                .filter(tx -> tx.getDateTransaction().isAfter(beginningOfLastMonth) && tx.getDateTransaction().isBefore(beginningOfCurrentMonth))
                .mapToDouble(Transaction::getMontant).sum();

        double revenueTrend = lastMonthRevenue == 0 ? (currentMonthRevenue > 0 ? 100.0 : 0.0) : ((currentMonthRevenue - lastMonthRevenue) / lastMonthRevenue) * 100.0;

        double currentMonthExpenses = transactions.stream()
                .filter(tx -> tx.getStatut() == TransactionStatut.COMPLETED)
                .filter(tx -> (tx.getTypeTransaction() == TransactionType.ACHAT || tx.getTypeTransaction() == TransactionType.REMBOURSEMENT))
                .filter(tx -> tx.getDateTransaction().isAfter(beginningOfCurrentMonth))
                .mapToDouble(Transaction::getMontant).sum();

        double lastMonthExpenses = transactions.stream()
                .filter(tx -> tx.getStatut() == TransactionStatut.COMPLETED)
                .filter(tx -> (tx.getTypeTransaction() == TransactionType.ACHAT || tx.getTypeTransaction() == TransactionType.REMBOURSEMENT))
                .filter(tx -> tx.getDateTransaction().isAfter(beginningOfLastMonth) && tx.getDateTransaction().isBefore(beginningOfCurrentMonth))
                .mapToDouble(Transaction::getMontant).sum();

        double expenseTrend = lastMonthExpenses == 0 ? (currentMonthExpenses > 0 ? 100.0 : 0.0) : ((currentMonthExpenses - lastMonthExpenses) / lastMonthExpenses) * 100.0;

        // --- MONTHLY DATA FOR CHARTS (Last 6 Months) ---
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        Map<String, Double> monthlyRevenue = new LinkedHashMap<>();
        Map<String, Double> monthlyExpenses = new LinkedHashMap<>();

        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.from(now).minusMonths(i);
            String label = ym.format(monthFormatter);
            
            double rev = transactions.stream()
                .filter(tx -> tx.getStatut() == TransactionStatut.COMPLETED && tx.getTypeTransaction() == TransactionType.VENTE)
                .filter(tx -> YearMonth.from(tx.getDateTransaction()).equals(ym))
                .mapToDouble(Transaction::getMontant).sum();
            
            double exp = transactions.stream()
                .filter(tx -> tx.getStatut() == TransactionStatut.COMPLETED && (tx.getTypeTransaction() == TransactionType.ACHAT || tx.getTypeTransaction() == TransactionType.REMBOURSEMENT))
                .filter(tx -> YearMonth.from(tx.getDateTransaction()).equals(ym))
                .mapToDouble(Transaction::getMontant).sum();
            
            monthlyRevenue.put(label, rev);
            monthlyExpenses.put(label, exp);
        }

        // --- ALERTS ---
        List<String> alerts = new ArrayList<>();
        if (currentMonthExpenses > (currentMonthRevenue * 0.8) && currentMonthRevenue > 0) {
            alerts.add("ALERTE: Vos dépenses ce mois-ci dépassent 80% de vos revenus.");
        }
        if (transactionRepository.countByStatut(TransactionStatut.PENDING) > 10) {
            alerts.add("Info: Vous avez plus de 10 transactions en attente de validation.");
        }

        // --- FORECAST (Simple next month projection based on last 3 months average) ---
        double avgLast3Months = transactions.stream()
            .filter(tx -> tx.getStatut() == TransactionStatut.COMPLETED && tx.getTypeTransaction() == TransactionType.VENTE)
            .filter(tx -> tx.getDateTransaction().isAfter(now.minusMonths(3)))
            .mapToDouble(Transaction::getMontant).sum() / 3.0;

        return FinanceSummaryResponse.builder()
                .totalTransactions(transactions.size())
                .totalRevenue(totalRevenue)
                .totalExpenses(totalExpenses)
                .totalAccounts(compteFinancierRepository.count())
                .pendingTransactions(transactionRepository.countByStatut(TransactionStatut.PENDING))
                .totalRemboursements(remboursementRepository.count())

                // USER STATS
                .totalUsers(userRepository.count())
                .totalAdmins(userRepository.countByUserType(UserType.ADMIN))
                .totalBeneficiaries(userRepository.countByUserType(UserType.BENEFICIARY))
                .totalPartners(userRepository.countByUserType(UserType.PARTNER))

                // CREDIT STATS
                .totalCredits(creditRepository.count())
                .activeCredits(creditRepository.countByStatut(StatutCredit.ACTIF))
                .closedCredits(creditRepository.countByStatut(StatutCredit.CLOTURE))
                .totalAmountGranted(totalAmountGranted != null ? totalAmountGranted : 0.0)
                .totalAmountRemaining(totalAmountRemaining != null ? totalAmountRemaining : 0.0)

                // ADVANCED
                .revenueTrend(revenueTrend)
                .expenseTrend(expenseTrend)
                .monthlyRevenue(monthlyRevenue)
                .monthlyExpenses(monthlyExpenses)
                .activeAlerts(alerts)
                .forecastedRevenue(avgLast3Months)
                .build();
    }

            public List<CreditLookupResponse> getCreditsLookup() {
            return creditRepository.findAll().stream()
                .map(credit -> CreditLookupResponse.builder()
                    .id(credit.getId())
                    .montantRestant(credit.getMontantRestant())
                    .statut(credit.getStatut())
                    .build())
                .toList();
            }

    private CompteFinancier getCompteEntity(Long id) {
        return compteFinancierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte financier not found with id: " + id));
    }

    private Transaction getTransactionEntity(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    private Credit getCreditEntity(Long id) {
        return creditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credit not found with id: " + id));
    }

    private Remboursement getRemboursementEntity(Long id) {
        return remboursementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Remboursement not found with id: " + id));
    }

    private RegleRemboursement getRegleEntity(Long id) {
        return regleRemboursementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regle remboursement not found with id: " + id));
    }

    private CompteFinancierResponse toCompteResponse(CompteFinancier compte) {
        return CompteFinancierResponse.builder()
                .idCompte(compte.getIdCompte())
                .solde(compte.getSolde())
                .typeCompte(compte.getTypeCompte())
                .utilisateurId(compte.getUtilisateur() != null ? compte.getUtilisateur().getId() : null)
                .build();
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .idTransaction(transaction.getIdTransaction())
                .typeTransaction(transaction.getTypeTransaction())
                .montant(transaction.getMontant())
                .dateTransaction(transaction.getDateTransaction())
                .statut(transaction.getStatut())
                .compteSourceId(transaction.getCompteSource() != null ? transaction.getCompteSource().getIdCompte() : null)
                .compteDestinationId(transaction.getCompteDestination() != null ? transaction.getCompteDestination().getIdCompte() : null)
                .orderId(transaction.getOrderId())
                .build();
    }

    private RemboursementResponse toRemboursementResponse(Remboursement remboursement) {
        return RemboursementResponse.builder()
                .idRemboursement(remboursement.getIdRemboursement())
                .montant(remboursement.getMontant())
                .dateRemboursement(remboursement.getDateRemboursement())
                .mode(remboursement.getMode())
                .creditId(remboursement.getCredit() != null ? remboursement.getCredit().getId() : null)
                .transactionId(remboursement.getTransaction() != null ? remboursement.getTransaction().getIdTransaction() : null)
                .build();
    }

    private RegleRemboursementResponse toRegleResponse(RegleRemboursement regle) {
        return RegleRemboursementResponse.builder()
                .idRegle(regle.getIdRegle())
                .typeRegle(regle.getTypeRegle())
                .valeur(regle.getValeur())
                .creditId(regle.getCredit() != null ? regle.getCredit().getId() : null)
                .build();
    }
}
