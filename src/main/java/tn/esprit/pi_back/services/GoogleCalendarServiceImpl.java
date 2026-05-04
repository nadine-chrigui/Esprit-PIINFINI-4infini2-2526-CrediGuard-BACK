package tn.esprit.pi_back.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import tn.esprit.pi_back.dto.GoogleCalendar.GoogleCalendarAuthUrlResponse;
import tn.esprit.pi_back.dto.GoogleCalendar.GoogleCalendarConnectionStatusResponse;
import tn.esprit.pi_back.dto.GoogleCalendar.GoogleCalendarSyncResponse;
import tn.esprit.pi_back.entities.CrowdfundingProject;
import tn.esprit.pi_back.entities.GoogleCalendarConnection;
import tn.esprit.pi_back.entities.Investment;
import tn.esprit.pi_back.entities.ReturnPayment;
import tn.esprit.pi_back.entities.User;
import tn.esprit.pi_back.repositories.CrowdfundingProjectRepository;
import tn.esprit.pi_back.repositories.GoogleCalendarConnectionRepository;
import tn.esprit.pi_back.repositories.InvestmentRepository;
import tn.esprit.pi_back.repositories.ReturnPaymentRepository;
import tn.esprit.pi_back.repositories.UserRepository;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class GoogleCalendarServiceImpl implements GoogleCalendarService {

    private static final String AUTH_BASE_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";
    private static final String EVENTS_BASE_URL = "https://www.googleapis.com/calendar/v3/calendars";

    private final GoogleCalendarConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final CrowdfundingProjectRepository projectRepository;
    private final InvestmentRepository investmentRepository;
    private final ReturnPaymentRepository returnPaymentRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.calendar.client-id:}")
    private String clientId;

    @Value("${google.calendar.client-secret:}")
    private String clientSecret;

    @Override
    public GoogleCalendarAuthUrlResponse buildAuthorizationUrl(String redirectUri) {
        ensureOAuthConfigured();

        String authorizationUrl = UriComponentsBuilder.fromUriString(AUTH_BASE_URL)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", String.join(" ",
                        "openid",
                        "https://www.googleapis.com/auth/userinfo.email",
                        "https://www.googleapis.com/auth/userinfo.profile",
                        "https://www.googleapis.com/auth/calendar.events"))
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("include_granted_scopes", "true")
                .build()
                .toUriString();

        return new GoogleCalendarAuthUrlResponse(authorizationUrl);
    }

    @Override
    public GoogleCalendarConnectionStatusResponse exchangeAuthorizationCode(Long userId, String code, String redirectUri) {
        ensureOAuthConfigured();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        JsonNode tokenResponse = postForm(TOKEN_URL, Map.of(
                "code", code,
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", redirectUri,
                "grant_type", "authorization_code"
        ));

        String accessToken = requiredText(tokenResponse, "access_token", "Google access token missing");
        String refreshToken = optionalText(tokenResponse, "refresh_token");
        LocalDateTime expiresAt = resolveExpiry(tokenResponse);

        JsonNode userInfo = getJson(USERINFO_URL, accessToken);
        String googleEmail = requiredText(userInfo, "email", "Google email missing");

        GoogleCalendarConnection connection = connectionRepository.findByUserId(userId)
                .orElseGet(GoogleCalendarConnection::new);
        connection.setUser(user);
        connection.setGoogleEmail(googleEmail);
        connection.setAccessToken(accessToken);
        if (refreshToken != null && !refreshToken.isBlank()) {
            connection.setRefreshToken(refreshToken);
        }
        connection.setAccessTokenExpiresAt(expiresAt);
        connectionRepository.save(connection);

        return new GoogleCalendarConnectionStatusResponse(true, googleEmail, expiresAt);
    }

    @Override
    @Transactional(readOnly = true)
    public GoogleCalendarConnectionStatusResponse getConnectionStatus(Long userId) {
        return connectionRepository.findByUserId(userId)
                .map(connection -> new GoogleCalendarConnectionStatusResponse(
                        true,
                        connection.getGoogleEmail(),
                        connection.getAccessTokenExpiresAt()
                ))
                .orElseGet(() -> new GoogleCalendarConnectionStatusResponse(false, null, null));
    }

    @Override
    public GoogleCalendarSyncResponse syncProjectCalendar(Long userId, Long projectId) {
        CrowdfundingProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getOwner().getId().equals(userId)) {
            throw new IllegalStateException("Only the project owner can sync this calendar");
        }

        GoogleCalendarConnection connection = connectionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Google Calendar is not connected"));

        String accessToken = ensureValidAccessToken(connection);
        List<Investment> investments = investmentRepository.findByProjectProjectId(projectId);
        List<ReturnPayment> payments = new ArrayList<>();
        for (Investment investment : investments) {
            payments.addAll(returnPaymentRepository.findByInvestmentInvestmentId(investment.getInvestmentId()));
        }

        int syncedCount = 0;
        for (ReturnPayment payment : payments) {
            syncReturnPaymentEvent(project, payment, accessToken, connection.getCalendarId());
            syncedCount++;
        }

        return new GoogleCalendarSyncResponse(connection.getGoogleEmail(), syncedCount);
    }

    @Override
    public GoogleCalendarSyncResponse syncInvestmentCalendar(Long userId, Long investmentId) {
        Investment investment = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new RuntimeException("Investment not found"));

        if (!investment.getInvestor().getId().equals(userId)) {
            throw new IllegalStateException("Only the investment owner can sync this calendar");
        }

        GoogleCalendarConnection connection = connectionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Google Calendar is not connected"));

        String accessToken = ensureValidAccessToken(connection);
        List<ReturnPayment> payments = returnPaymentRepository.findByInvestmentInvestmentId(investmentId);

        int syncedCount = 0;
        for (ReturnPayment payment : payments) {
            syncReturnPaymentEvent(investment.getProject(), payment, accessToken, connection.getCalendarId());
            syncedCount++;
        }

        return new GoogleCalendarSyncResponse(connection.getGoogleEmail(), syncedCount);
    }

    @Override
    public void syncReturnPaymentStatus(Long returnPaymentId) {
        ReturnPayment payment = returnPaymentRepository.findById(returnPaymentId)
                .orElseThrow(() -> new RuntimeException("Return payment not found"));

        if (payment.getGoogleCalendarEventId() == null || payment.getGoogleCalendarEventId().isBlank()) {
            return;
        }

        CrowdfundingProject project = payment.getInvestment().getProject();
        GoogleCalendarConnection connection = connectionRepository.findByUserId(project.getOwner().getId())
                .orElse(null);

        if (connection == null) {
            return;
        }

        String accessToken = ensureValidAccessToken(connection);
        syncReturnPaymentEvent(project, payment, accessToken, connection.getCalendarId());
    }

    private void syncReturnPaymentEvent(CrowdfundingProject project, ReturnPayment payment, String accessToken, String calendarId) {
        LocalDate paymentDate = payment.getPaymentDate();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("summary", "CrediGuard - [" + payment.getStatus().name() + "] " + payment.getType().name() + " return - " + project.getTitle());
        payload.put("description", String.format(
                "Project: %s%nInvestment ID: %d%nInvestor ID: %d%nAmount: %.2f %s%nStatus: %s%nConfirmed at: %s",
                project.getTitle(),
                payment.getInvestment().getInvestmentId(),
                payment.getInvestment().getInvestor().getId(),
                payment.getAmount(),
                payment.getCurrency() == null ? "usd" : payment.getCurrency(),
                payment.getStatus().name(),
                payment.getConfirmedAt() == null ? "Pending" : payment.getConfirmedAt()
        ));
        payload.put("start", Map.of("date", paymentDate.toString()));
        payload.put("end", Map.of("date", paymentDate.plusDays(1).toString()));
        payload.put("colorId", resolveColorId(payment.getStatus()));

        String calendarPath = URLEncoder.encode(calendarId, StandardCharsets.UTF_8);
        String endpoint = EVENTS_BASE_URL + "/" + calendarPath + "/events";
        JsonNode response;

        if (payment.getGoogleCalendarEventId() != null && !payment.getGoogleCalendarEventId().isBlank()) {
            response = sendJson(endpoint + "/" + payment.getGoogleCalendarEventId(), "PUT", payload, accessToken);
        } else {
            response = sendJson(endpoint, "POST", payload, accessToken);
        }

        payment.setGoogleCalendarEventId(optionalText(response, "id"));
        payment.setGoogleCalendarEventLink(optionalText(response, "htmlLink"));
        payment.setGoogleCalendarSyncedAt(LocalDateTime.now());
        returnPaymentRepository.save(payment);
    }

    private String resolveColorId(ReturnPayment.ReturnStatus status) {
        return switch (status) {
            case PAID -> "2";
            case FAILED -> "11";
            case SCHEDULED -> "5";
        };
    }

    private String ensureValidAccessToken(GoogleCalendarConnection connection) {
        if (connection.getAccessToken() != null
                && connection.getAccessTokenExpiresAt() != null
                && connection.getAccessTokenExpiresAt().isAfter(LocalDateTime.now().plusMinutes(1))) {
            return connection.getAccessToken();
        }

        if (connection.getRefreshToken() == null || connection.getRefreshToken().isBlank()) {
            throw new IllegalStateException("Missing Google refresh token. Please reconnect Google Calendar.");
        }

        JsonNode tokenResponse = postForm(TOKEN_URL, Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "refresh_token", connection.getRefreshToken(),
                "grant_type", "refresh_token"
        ));

        String accessToken = requiredText(tokenResponse, "access_token", "Google access token missing");
        connection.setAccessToken(accessToken);
        connection.setAccessTokenExpiresAt(resolveExpiry(tokenResponse));
        connectionRepository.save(connection);
        return accessToken;
    }

    private JsonNode postForm(String url, Map<String, String> formData) {
        try {
            String encodedForm = formData.entrySet().stream()
                    .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                            + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .reduce((a, b) -> a + "&" + b)
                    .orElse("");

            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(encodedForm))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Google OAuth error: " + response.body());
            }

            return objectMapper.readTree(response.body());
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Unable to reach Google OAuth services", ex);
        }
    }

    private JsonNode getJson(String url, String accessToken) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Google API error: " + response.body());
            }

            return objectMapper.readTree(response.body());
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Unable to call Google API", ex);
        }
    }

    private JsonNode sendJson(String url, String method, Map<String, Object> payload, String accessToken) {
        try {
            HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload));
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json");

            HttpRequest request = switch (method) {
                case "POST" -> builder.POST(body).build();
                case "PUT" -> builder.PUT(body).build();
                default -> throw new IllegalArgumentException("Unsupported method: " + method);
            };

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Google Calendar API error: " + response.body());
            }

            return objectMapper.readTree(response.body());
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Unable to sync Google Calendar event", ex);
        }
    }

    private LocalDateTime resolveExpiry(JsonNode tokenResponse) {
        int expiresIn = tokenResponse.path("expires_in").asInt(3600);
        return LocalDateTime.now().plusSeconds(expiresIn);
    }

    private String requiredText(JsonNode node, String field, String errorMessage) {
        String value = optionalText(node, field);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(errorMessage);
        }
        return value;
    }

    private String optionalText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private void ensureOAuthConfigured() {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalStateException("Google Calendar OAuth is not configured on the backend");
        }
    }
}
