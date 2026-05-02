package tn.esprit.pi_back.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.dto.CreateTransactionRequest;
import tn.esprit.pi_back.dto.TransactionDTO;
import tn.esprit.pi_back.entities.Transaction;
import tn.esprit.pi_back.services.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionDTO> create(
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        Transaction tx = transactionService.createFromRequest(request);
        TransactionDTO dto = new TransactionDTO();
        dto.setIdTransaction(tx.getIdTransaction());
        dto.setTypeTransaction(tx.getTypeTransaction());
        dto.setMontant(tx.getMontant());
        dto.setDateTransaction(tx.getDateTransaction());
        dto.setStatut(tx.getStatut());
        dto.setCompteSourceId(
                tx.getCompteSource() != null ? tx.getCompteSource().getIdCompte() : null
        );
        dto.setCompteDestinationId(
                tx.getCompteDestination() != null ? tx.getCompteDestination().getIdCompte() : null
        );
        dto.setOrderId(tx.getOrderId());
        return ResponseEntity.ok(dto);
    }
    @GetMapping("/compte/{compteId}")
    public ResponseEntity<List<Transaction>> getByCompte(@PathVariable Long compteId) {
        List<Transaction> result = transactionService.getAll()
                .stream()
                .filter(t ->
                        t.getCompteSource().getIdCompte().equals(compteId) ||
                                t.getCompteDestination().getIdCompte().equals(compteId)
                )
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable Long id, @Valid @RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.update(id, transaction));
    }

    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAll() {
        List<TransactionDTO> dtos = transactionService.getAll().stream()
                .map(tx -> {
                    TransactionDTO dto = new TransactionDTO();
                    dto.setIdTransaction(tx.getIdTransaction());
                    dto.setTypeTransaction(tx.getTypeTransaction());
                    dto.setMontant(tx.getMontant());
                    dto.setDateTransaction(tx.getDateTransaction());
                    dto.setStatut(tx.getStatut());
                    dto.setCompteSourceId(
                            tx.getCompteSource() != null ? tx.getCompteSource().getIdCompte() : null
                    );
                    dto.setCompteDestinationId(
                            tx.getCompteDestination() != null ? tx.getCompteDestination().getIdCompte() : null
                    );
                    dto.setOrderId(tx.getOrderId());
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getById(@PathVariable Long id) {
        Transaction tx = transactionService.getById(id);
        TransactionDTO dto = new TransactionDTO();
        dto.setIdTransaction(tx.getIdTransaction());
        dto.setTypeTransaction(tx.getTypeTransaction());
        dto.setMontant(tx.getMontant());
        dto.setDateTransaction(tx.getDateTransaction());
        dto.setStatut(tx.getStatut());
        dto.setCompteSourceId(
                tx.getCompteSource() != null ? tx.getCompteSource().getIdCompte() : null
        );
        dto.setCompteDestinationId(
                tx.getCompteDestination() != null ? tx.getCompteDestination().getIdCompte() : null
        );
        dto.setOrderId(tx.getOrderId());
        return ResponseEntity.ok(dto);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
