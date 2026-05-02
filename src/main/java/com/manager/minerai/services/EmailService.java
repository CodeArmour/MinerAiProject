package com.manager.minerai.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${app.mail.from:noreply@minerai.com}")
    private String fromEmail;

    /**
     * Sends an invitation email to a user
     */
    @Async
    public void sendInvitationEmail(
            String toEmail,
            String projectName,
            String invitedByName,
            String invitationToken,
            boolean userExists
    ) {
        try {
            String acceptUrl = frontendUrl + "/accept-invitation?token=" + invitationToken;
            String subject = "You've been invited to join " + projectName + " on MinerAI";

            String htmlBody;
            if (userExists) {
                htmlBody = buildExistingUserEmail(invitedByName, projectName, acceptUrl);
            } else {
                String signupUrl = frontendUrl + "/register?email=" + toEmail + "&invitation=" + invitationToken;
                htmlBody = buildNewUserEmail(invitedByName, projectName, signupUrl);
            }

            sendHtmlEmail(toEmail, subject, htmlBody);
            log.info("Invitation email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send invitation email to: {}", toEmail, e);
            // Don't throw exception - we don't want to fail the invitation creation if email fails
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true = HTML

        mailSender.send(message);
    }

    private String buildExistingUserEmail(String invitedByName, String projectName, String acceptUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>MinerAI</h1>
                        <p>Project Invitation</p>
                    </div>
                    <div class="content">
                        <h2>You've been invited!</h2>
                        <p>Hello,</p>
                        <p><strong>%s</strong> has invited you to join the project <strong>"%s"</strong> on MinerAI.</p>
                        <p>Click the button below to accept the invitation:</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button">Accept Invitation</a>
                        </div>
                        <p style="color: #666; font-size: 12px;">This invitation will expire in 7 days.</p>
                        <p style="color: #666; font-size: 12px;">If the button doesn't work, copy and paste this link: %s</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 MinerAI. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, invitedByName, projectName, acceptUrl, acceptUrl);
    }

    private String buildNewUserEmail(String invitedByName, String projectName, String signupUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 6px; margin: 20px 0; }
                    .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>MinerAI</h1>
                        <p>Project Invitation</p>
                    </div>
                    <div class="content">
                        <h2>You've been invited!</h2>
                        <p>Hello,</p>
                        <p><strong>%s</strong> has invited you to join the project <strong>"%s"</strong> on MinerAI.</p>
                        <p>You don't have an account yet. Please sign up first to join the project:</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button">Sign Up & Join Project</a>
                        </div>
                        <p>After signing up, you'll automatically join the project.</p>
                        <p style="color: #666; font-size: 12px;">This invitation will expire in 7 days.</p>
                        <p style="color: #666; font-size: 12px;">If the button doesn't work, copy and paste this link: %s</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 MinerAI. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, invitedByName, projectName, signupUrl, signupUrl);
    }
}
