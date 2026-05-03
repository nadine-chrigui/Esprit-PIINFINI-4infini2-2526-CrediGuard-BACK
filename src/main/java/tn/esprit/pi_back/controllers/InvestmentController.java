package tn.esprit.pi_back.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.pi_back.entities.InvestmentOffer;
import tn.esprit.pi_back.entities.PerformanceTracking;
import tn.esprit.pi_back.dto.finance.InvestmentOfferDto;
import tn.esprit.pi_back.dto.finance.PerformanceTrackingDto;
import tn.esprit.pi_back.services.InvestmentService;
import tn.esprit.pi_back.services.ScoringService;
import tn.esprit.pi_back.services.UserService;
import tn.esprit.pi_back.dto.finance.FinancialProfileDto;

import java.util.List;

@RestController
@RequestMapping("/investments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvestmentController {

    private final ScoringService scoringService;
    private final InvestmentService investmentService;
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<FinancialProfileDto> getMyProfile() {
        Long userId = userService.getCurrentUserOrThrow().getId();
        return ResponseEntity.ok(FinancialProfileDto.from(scoringService.getProfile(userId)));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<FinancialProfileDto> getProfileForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(FinancialProfileDto.from(scoringService.getProfile(userId)));
    }

    @PostMapping("/profile/recalculate")
    public ResponseEntity<FinancialProfileDto> recalculateMyProfile() {
        Long userId = userService.getCurrentUserOrThrow().getId();
        return ResponseEntity.ok(FinancialProfileDto.from(scoringService.calculateAndSaveProfile(userId)));
    }

    @GetMapping("/offers")
    public ResponseEntity<List<InvestmentOfferDto>> getMyOffers() {
        Long userId = userService.getCurrentUserOrThrow().getId();
        return ResponseEntity.ok(investmentService.getOffersForUser(userId)
                .stream()
                .map(InvestmentOfferDto::from)
                .toList());
    }

    @PostMapping("/offers/{offerId}/accept")
    public ResponseEntity<InvestmentOfferDto> acceptOffer(@PathVariable Long offerId) {
        return ResponseEntity.ok(InvestmentOfferDto.from(investmentService.acceptOffer(offerId)));
    }

    @GetMapping("/offers/{offerId}/performance")
    public ResponseEntity<List<PerformanceTrackingDto>> getPerformance(@PathVariable Long offerId) {
        return ResponseEntity.ok(investmentService.getPerformance(offerId)
                .stream()
                .map(PerformanceTrackingDto::from)
                .toList());
    }
}
