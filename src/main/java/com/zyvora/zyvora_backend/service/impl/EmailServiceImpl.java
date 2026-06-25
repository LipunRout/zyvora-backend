package com.zyvora.zyvora_backend.service.impl;

import com.zyvora.zyvora_backend.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${brevo.from.email:onboarding@zyvora.com}")
    private String fromEmail;

    @Value("${brevo.from.name:Zyvora}")
    private String fromName;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String to, String code, long expirationMinutes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject("Your Zyvora code: " + code);
            helper.setText(buildHtml(code, expirationMinutes), true);

            mailSender.send(message);
            log.info("OTP email sent to {}", to);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Failed to send OTP email to {}", to, e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildHtml(String code, long expirationMinutes) {
        String spacedCode = String.join(" ", code.split(""));

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="utf-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>Your Zyvora code</title>
                </head>
                <body style="margin:0; padding:0; background-color:#0a0a0a; -webkit-text-size-adjust:100%%;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#0a0a0a;">
                    <tr>
                      <td align="center" style="padding: 40px 16px;">

                        <table role="presentation" width="480" cellpadding="0" cellspacing="0" style="max-width:480px; width:100%%;">

                          <tr>
                            <td align="center" style="padding-bottom: 32px;">
                              <table role="presentation" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="background-color:#e84118; width:36px; height:36px; border-radius:8px; text-align:center; vertical-align:middle;">
                                    <span style="font-family: Georgia, 'Times New Roman', serif; font-style:italic; font-weight:bold; font-size:20px; color:#ffffff; line-height:36px;">Z</span>
                                  </td>
                                  <td style="padding-left:10px;">
                                    <span style="font-family: Helvetica, Arial, sans-serif; font-weight:700; font-size:18px; color:#f5f0e8; letter-spacing:-0.4px;">Zyvora</span>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>

                          <tr>
                            <td style="background-color:#141414; border:1px solid rgba(255,255,255,0.07); border-radius:14px; padding: 40px 36px;">

                              <p style="margin:0 0 6px 0; font-family: Helvetica, Arial, sans-serif; font-size:12px; font-weight:700; letter-spacing:1.5px; text-transform:uppercase; color:rgba(245,240,232,0.4);">
                                Sign-in code
                              </p>

                              <h1 style="margin:0 0 24px 0; font-family: Helvetica, Arial, sans-serif; font-weight:700; font-size:24px; letter-spacing:-0.6px; color:#f5f0e8;">
                                Here's your code to continue
                              </h1>

                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:24px;">
                                <tr>
                                  <td align="center" style="background-color:#0d0d0d; border:1px solid rgba(232,65,24,0.3); border-radius:10px; padding:22px 16px;">
                                    <span style="font-family: 'Courier New', Courier, monospace; font-weight:700; font-size:34px; letter-spacing:14px; color:#ff5a2c;">%s</span>
                                  </td>
                                </tr>
                              </table>

                              <p style="margin:0 0 28px 0; font-family: Helvetica, Arial, sans-serif; font-size:14px; line-height:21px; font-weight:300; color:rgba(245,240,232,0.55);">
                                This code expires in <strong style="color:#f5f0e8; font-weight:600;">%d minute(s)</strong>. Enter it on the Zyvora sign-in screen to continue building your team.
                              </p>

                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="border-top:1px solid rgba(255,255,255,0.07); padding-top:20px;">
                                    <p style="margin:0; font-family: Helvetica, Arial, sans-serif; font-size:12px; line-height:18px; font-weight:300; color:rgba(245,240,232,0.35);">
                                      Didn't request this? You can safely ignore this email — no account changes were made.
                                    </p>
                                  </td>
                                </tr>
                              </table>

                            </td>
                          </tr>

                          <tr>
                            <td align="center" style="padding-top: 28px;">
                              <p style="margin:0; font-family: Helvetica, Arial, sans-serif; font-size:11px; color:rgba(245,240,232,0.25); letter-spacing:0.3px;">
                                Zyvora · Where Founders Find Founders
                              </p>
                            </td>
                          </tr>

                        </table>

                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(spacedCode, expirationMinutes);
    }
}