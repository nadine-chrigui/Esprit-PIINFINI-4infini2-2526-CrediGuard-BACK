package tn.esprit.pi_back.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tn.esprit.pi_back.entities.Credit;
import tn.esprit.pi_back.entities.DemandeCredit;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.entities.enums.ModeRemboursement;
import tn.esprit.pi_back.entities.enums.StatutCredit;
import tn.esprit.pi_back.entities.enums.StatutDemande;
import tn.esprit.pi_back.entities.enums.TypeCredit;
import tn.esprit.pi_back.entities.enums.UserType;
import tn.esprit.pi_back.repositories.CreditRepository;
import tn.esprit.pi_back.repositories.DemandeCreditRepository;
import tn.esprit.pi_back.repositories.UserRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CreditDataInitializer implements CommandLineRunner {

    private final CreditRepository creditRepository;
    private final DemandeCreditRepository demandeCreditRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (creditRepository.count() > 0) {
            return;
        }

        User client = userRepository.findByEmail("credit.client@crediguard.local")
                .orElseGet(() -> {
                    User user = new User();
                    user.setFullName("Credit Test Client");
                    user.setEmail("credit.client@crediguard.local");
                    user.setPassword(passwordEncoder.encode("Client123!"));
                    user.setUserType(UserType.BENEFICIARY);
                    user.setPhone("11112222");
                    user.setEnabled(true);
                    return userRepository.save(user);
                });

        DemandeCredit demande = new DemandeCredit();
        demande.setReference("DC-INIT-001");
        demande.setTypeCredit(TypeCredit.NUMERAIRE);
        demande.setMontantDemande(2000D);
        demande.setDureeMois(12);
        demande.setObjetCredit("Besoin fonds de roulement initial");
        demande.setStatut(StatutDemande.APPROUVEE);
        demande.setDateCreation(LocalDateTime.now());
        demande.setClient(client);
        demande = demandeCreditRepository.save(demande);

        Credit credit = new Credit();
        credit.setMontantAccorde(2000D);
        credit.setMontantTotal(2200D);
        credit.setMontantRestant(1200D);
        credit.setTauxRemboursement(10D);
        credit.setDateDebut(LocalDateTime.now());
        credit.setDateFin(LocalDateTime.now().plusMonths(12));
        credit.setStatut(StatutCredit.ACTIF);
        credit.setModeRemboursement(ModeRemboursement.MIXTE);
        credit.setClient(client);
        credit.setDemandeCredit(demande);

        creditRepository.save(credit);
    }
}
