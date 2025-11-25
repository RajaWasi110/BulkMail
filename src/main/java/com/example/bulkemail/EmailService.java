package com.example.bulkemail;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class EmailService {

    private final String SMTP_HOST = "smtp.gmail.com";
    private final int SMTP_PORT = 587;
    private final String SMTP_USER = "viziblex@gmail.com"; // Your email
    private final String SMTP_PASS = "miak rfoj ucdd lnzs"; // App password

    public List<String> sendEmails(String subject, String body, String manualRecipients, MultipartFile file) {
        List<String> allRecipients = new ArrayList<>();

        // Manual recipients
        if (manualRecipients != null && !manualRecipients.isEmpty()) {
            String[] split = manualRecipients.split("[,;\\n]+");
            for (String s : split) {
                if (!s.trim().isEmpty()) allRecipients.add(s.trim());
            }
        }

        // File-based recipients
        if (file != null && !file.isEmpty()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) allRecipients.add(line.trim());
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to read uploaded file: " + e.getMessage());
            }
        }

        if (allRecipients.isEmpty()) {
            throw new RuntimeException("No recipients provided.");
        }

        List<String> failedRecipients = new ArrayList<>();

        // Convert body newlines to HTML <br>
        String formattedBody = body.replace("\n", "<br>");

        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                }
            });

            // Send emails individually to track failures
            for (String recipient : allRecipients) {
                try {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(SMTP_USER, "Viziblex"));
                    message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
                    message.setSubject(subject);
                    message.setContent(formattedBody, "text/html; charset=utf-8");

                    Transport.send(message);
                    Thread.sleep(200); // small delay between emails

                } catch (Exception e) {
                    failedRecipients.add(recipient);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to send emails: " + e.getMessage());
        }

        return failedRecipients;
    }
}
