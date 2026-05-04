package tn.esprit.pi_back.services;

import tn.esprit.pi_back.dto.GoogleCalendar.GoogleCalendarAuthUrlResponse;
import tn.esprit.pi_back.dto.GoogleCalendar.GoogleCalendarConnectionStatusResponse;
import tn.esprit.pi_back.dto.GoogleCalendar.GoogleCalendarSyncResponse;

public interface GoogleCalendarService {
    GoogleCalendarAuthUrlResponse buildAuthorizationUrl(String redirectUri);
    GoogleCalendarConnectionStatusResponse exchangeAuthorizationCode(Long userId, String code, String redirectUri);
    GoogleCalendarConnectionStatusResponse getConnectionStatus(Long userId);
    GoogleCalendarSyncResponse syncProjectCalendar(Long userId, Long projectId);
    GoogleCalendarSyncResponse syncInvestmentCalendar(Long userId, Long investmentId);
    void syncReturnPaymentStatus(Long returnPaymentId);
}
