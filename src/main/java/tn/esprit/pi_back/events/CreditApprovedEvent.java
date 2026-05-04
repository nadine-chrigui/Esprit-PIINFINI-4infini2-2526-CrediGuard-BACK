package tn.esprit.pi_back.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import tn.esprit.pi_back.entities.DemandeCredit;

@Getter
public class CreditApprovedEvent extends ApplicationEvent {
    private final DemandeCredit demandeCredit;

    public CreditApprovedEvent(Object source, DemandeCredit demandeCredit) {
        super(source);
        this.demandeCredit = demandeCredit;
    }
}
