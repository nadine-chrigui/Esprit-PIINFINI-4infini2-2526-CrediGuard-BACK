package tn.esprit.pi_back.services;

import tn.esprit.pi_back.entities.Transaction;
import java.util.List;

public interface TransactionService {
    Transaction create(Transaction transaction);

    Transaction update(Long id, Transaction transaction);

    Transaction getById(Long id);

    List<Transaction> getAll();

    void delete(Long id);
}
