package tn.esprit.pi_back.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"investment", "transaction"})
public class ReturnPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long returnId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be > 0")
    @Column(nullable = false)
    private Double amount;

    @NotNull(message = "paymentDate is required")
    @Column(nullable = false)
    private LocalDate paymentDate;

    @NotNull(message = "type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnType type;

    @NotNull(message = "status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReturnStatus status = ReturnStatus.PAID;

    @NotNull(message = "investment is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investment_id", nullable = false)
    private Investment investment;

    @NotNull(message = "transaction is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    public enum ReturnType {
        INTEREST, CAPITAL
    }

    public enum ReturnStatus {
        SCHEDULED, PAID, FAILED
    }
}
