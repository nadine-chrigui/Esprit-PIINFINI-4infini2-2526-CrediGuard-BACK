package tn.esprit.pi_back.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.pi_back.entities.Transaction;
import tn.esprit.pi_back.repositories.TransactionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

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
}
