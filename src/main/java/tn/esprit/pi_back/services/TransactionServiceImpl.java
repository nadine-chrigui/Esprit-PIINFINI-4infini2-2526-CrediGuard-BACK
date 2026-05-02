package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.dto.CreateTransactionRequest;
import tn.esprit.pi_back.entities.CompteFinancier;
import tn.esprit.pi_back.entities.Transaction;
import tn.esprit.pi_back.repositories.CompteFinancierRepository;
import tn.esprit.pi_back.repositories.TransactionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CompteFinancierRepository compteFinancierRepository;

    @Override
    public Transaction create(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction update(Long id, Transaction transaction) {
        Transaction existing = getById(id);
        existing.setTypeTransaction(transaction.getTypeTransaction());
        existing.setMontant(transaction.getMontant());
        existing.setDateTransaction(transaction.getDateTransaction());
        existing.setStatut(transaction.getStatut());
        existing.setCompteSource(transaction.getCompteSource());
        existing.setCompteDestination(transaction.getCompteDestination());
        existing.setOrderId(transaction.getOrderId());
        existing.setVoucher(transaction.getVoucher());
        return transactionRepository.save(existing);
    }

    @Override
    public Transaction getById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    @Override
    public List<Transaction> getAll() {
        return transactionRepository.findAll();
    }

    @Override
    public void delete(Long id) {
        transactionRepository.deleteById(id);
    }
    public List<Transaction> getByCompteId(Long compteId) {
        return transactionRepository
                .findByCompteSource_IdCompteOrCompteDestination_IdCompte(compteId, compteId);
    }
    public Transaction createFromRequest(CreateTransactionRequest request) {
        Transaction tx = new Transaction();
        tx.setTypeTransaction(request.getTypeTransaction());
        tx.setMontant(request.getMontant());
        tx.setOrderId(request.getOrderId());

        CompteFinancier source = compteFinancierRepository
                .findById(request.getCompteSourceId())
                .orElseThrow(() -> new RuntimeException("Compte source introuvable"));

        CompteFinancier destination = compteFinancierRepository
                .findById(request.getCompteDestinationId())
                .orElseThrow(() -> new RuntimeException("Compte destination introuvable"));

        tx.setCompteSource(source);
        tx.setCompteDestination(destination);

        return transactionRepository.save(tx);
    }
}
