package tn.esprit.pi_back.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress);
            }

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(buildHtmlTemplate(subject, body), true);

            mailSender.send(message);
        } catch (MessagingException ex) {
            throw new RuntimeException("Failed to send email", ex);
        }
    }

    private String buildHtmlTemplate(String subject, String body) {
        String content = toHtmlContent(body);
        return """
                <html>
                  <body style="margin:0;padding:0;background:#f4f8fc;font-family:Arial,sans-serif;color:#0f172a;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f4f8fc;padding:24px 0;">
                      <tr>
                        <td align="center">
                          <table role="presentation" width="640" cellspacing="0" cellpadding="0" style="width:640px;max-width:640px;background:#ffffff;border-radius:20px;overflow:hidden;box-shadow:0 18px 50px rgba(15,23,42,0.08);">
                            <tr>
                              <td style="padding:0;background:linear-gradient(135deg,#0f172a 0%%,#1d4ed8 55%%,#16a34a 100%%);">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0">
                                  <tr>
                                    <td style="padding:24px 28px;">
                                      <table role="presentation" cellspacing="0" cellpadding="0">
                                        <tr>
                                          <td style="width:52px;height:52px;border-radius:16px;background:rgba(255,255,255,0.14);border:1px solid rgba(255,255,255,0.2);text-align:center;vertical-align:middle;font-size:22px;font-weight:700;color:#ffffff;">CG</td>
                                          <td style="padding-left:16px;">
                                            <div style="font-size:24px;font-weight:700;color:#ffffff;letter-spacing:0.2px;">CrediGuard</div>
                                            <div style="font-size:13px;color:rgba(255,255,255,0.78);margin-top:4px;">Crowdfunding intelligence & investor trust</div>
                                          </td>
                                        </tr>
                                      </table>
                                    </td>
                                  </tr>
                                </table>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:32px 32px 18px;">
                                <div style="font-size:26px;line-height:1.25;font-weight:700;color:#0f172a;margin-bottom:14px;">%s</div>
                                <div style="height:4px;width:72px;border-radius:999px;background:linear-gradient(90deg,#3b82f6,#16a34a);margin-bottom:24px;"></div>
                                %s
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:0 32px 32px;">
                                <div style="border-radius:16px;background:#f8fbff;border:1px solid #dbe7f5;padding:18px 20px;color:#475569;font-size:13px;line-height:1.7;">
                                  This message was generated automatically by CrediGuard. Please review the latest project activity in your dashboard for more details.
                                </div>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """.formatted(escapeHtml(subject), content);
    }

    private String toHtmlContent(String body) {
        String[] lines = body.split("\\R");
        List<String> bullets = new ArrayList<>();
        StringBuilder paragraphs = new StringBuilder();

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("- ")) {
                bullets.add("<li style=\"margin:0 0 10px;\">%s</li>".formatted(escapeHtml(line.substring(2))));
            } else {
                paragraphs.append("<p style=\"margin:0 0 14px;font-size:15px;line-height:1.8;color:#334155;\">")
                        .append(escapeHtml(line))
                        .append("</p>");
            }
        }

        if (!bullets.isEmpty()) {
            paragraphs.append("""
                    <div style="margin-top:18px;padding:18px 20px;border-radius:16px;background:linear-gradient(180deg,#f8fbff 0%%,#f1f9f4 100%%);border:1px solid #dbe7f5;">
                      <div style="font-size:14px;font-weight:700;color:#0f172a;margin-bottom:12px;">Investor analysis snapshot</div>
                      <ul style="margin:0;padding-left:18px;color:#334155;font-size:14px;line-height:1.7;">
                        %s
                      </ul>
                    </div>
                    """.formatted(String.join("", bullets)));
        }

        return paragraphs.toString();
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
